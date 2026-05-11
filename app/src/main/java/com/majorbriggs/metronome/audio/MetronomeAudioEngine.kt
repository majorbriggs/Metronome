package com.majorbriggs.metronome.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioTimestamp
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.os.Process
import com.majorbriggs.metronome.R
import com.majorbriggs.metronome.data.TimeSignature
import com.majorbriggs.metronome.service.BeatListener
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@ServiceScoped
class MetronomeAudioEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val WAV_HEADER_BYTES = 44
        private const val CLICK_SAMPLES = 352  // (748 bytes - 44 header) / 2 bytes per sample
        private const val CHUNK_SAMPLES = 441  // 10 ms of silence per write chunk
    }

    private val accentPcm: ShortArray = loadPcm(R.raw.beat_accent)
    private val regularPcm: ShortArray = loadPcm(R.raw.beat_regular)
    private val silenceChunk: ShortArray = ShortArray(CHUNK_SAMPLES) // always zeros

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val callbackHandler = Handler(Looper.getMainLooper())
    private val audioTimestamp = AudioTimestamp()  // pre-allocated, reused each beat

    @Volatile private var isRunning = false
    private val pendingBpm = AtomicInteger(-1)
    @Volatile private var pendingTimeSignature: TimeSignature? = null

    private var listener: BeatListener? = null
    private var audioTrack: AudioTrack? = null
    private var audioThread: Thread? = null
    private var focusRequest: AudioFocusRequest? = null

    fun setListener(l: BeatListener) {
        listener = l
    }

    fun start(bpm: Int, timeSignature: TimeSignature) {
        if (isRunning) return

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener({ change ->
                if (change == AudioManager.AUDIOFOCUS_LOSS ||
                    change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    stop()
                }
            }, Handler(Looper.getMainLooper()))
            .build()
        focusRequest = req

        if (audioManager.requestAudioFocus(req) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return

        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(attrs)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minBuf, CHUNK_SAMPLES * 2 * 4))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()

        audioTrack = track
        pendingBpm.set(-1)
        pendingTimeSignature = null
        isRunning = true
        track.play()

        audioThread = Thread(
            { streamingLoop(track, bpm, timeSignature) },
            "MetronomeAudioThread"
        ).also { it.start() }
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        callbackHandler.removeCallbacksAndMessages(null)
        audioTrack?.stop()
        audioThread?.join(500)
        audioTrack?.release()
        audioTrack = null
        audioThread = null
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    fun updateBpm(bpm: Int) {
        pendingBpm.set(bpm)
    }

    fun updateTimeSignature(sig: TimeSignature) {
        pendingTimeSignature = sig
    }

    fun release() {
        stop()
    }

    // Timing is driven entirely by AudioTrack.write(WRITE_BLOCKING), which blocks until the
    // hardware FIFO has room. This means beat positions are controlled at PCM sample resolution
    // (22 µs at 44100 Hz) with no OS-scheduler jitter.
    private fun streamingLoop(track: AudioTrack, initialBpm: Int, initialSig: TimeSignature) {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)

        var writeHead = 0L
        var nextBeatSampleD = 0.0   // double accumulation avoids integer truncation drift
        var nextBeatSample = 0L
        var beatIndex = 0
        var currentBpm = initialBpm
        var currentSig = initialSig
        var samplesPerBeat = SAMPLE_RATE * 60.0 / currentBpm
        var postClickRemaining = 0
        var inSilencePhase = false

        while (isRunning) {
            if (!inSilencePhase) {
                // Apply any BPM/signature changes queued since the last beat
                val newBpm = pendingBpm.getAndSet(-1)
                if (newBpm != -1) {
                    currentBpm = newBpm
                    samplesPerBeat = SAMPLE_RATE * 60.0 / currentBpm
                }
                pendingTimeSignature?.let { newSig ->
                    currentSig = newSig
                    beatIndex = 0
                    pendingTimeSignature = null
                }

                val isAccent = currentSig.accentedBeats.contains(beatIndex)
                val clickPcm = if (isAccent) accentPcm else regularPcm

                // Snapshot write head and playback position before the click lands in the buffer.
                // AudioTimestamp tells us exactly when sample `preWriteHead` will reach the DAC,
                // so we can delay the UI/haptic callback to match the audible beat.
                val preWriteHead = writeHead
                val hasTimestamp = track.getTimestamp(audioTimestamp)
                val nowNs = System.nanoTime()

                val written = track.write(clickPcm, 0, CLICK_SAMPLES, AudioTrack.WRITE_BLOCKING)
                if (written < 0) break
                writeHead += written

                val capturedIndex = beatIndex
                val capturedAccent = isAccent
                val delayMs = if (hasTimestamp && audioTimestamp.framePosition > 0) {
                    // time(N) = ts.nanoTime + (N - ts.framePosition) / SAMPLE_RATE
                    val playNs = audioTimestamp.nanoTime +
                        (preWriteHead - audioTimestamp.framePosition) * 1_000_000_000L / SAMPLE_RATE
                    ((playNs - nowNs) / 1_000_000L).coerceAtLeast(0L)
                } else {
                    0L
                }
                callbackHandler.postDelayed(
                    { listener?.onBeat(capturedIndex, capturedAccent) },
                    delayMs
                )

                beatIndex = (beatIndex + 1) % currentSig.beatsPerBar
                nextBeatSampleD += samplesPerBeat
                nextBeatSample = nextBeatSampleD.toLong()
                postClickRemaining = (nextBeatSample - writeHead).toInt()
                inSilencePhase = true
            } else {
                val toWrite = minOf(postClickRemaining, CHUNK_SAMPLES)
                val written = track.write(silenceChunk, 0, toWrite, AudioTrack.WRITE_BLOCKING)
                if (written < 0) break
                writeHead += written
                postClickRemaining -= written
                if (postClickRemaining == 0) inSilencePhase = false
            }
        }
    }

    private fun loadPcm(resId: Int): ShortArray {
        val bytes = context.resources.openRawResource(resId).use { it.readBytes() }
        val pcmByteCount = bytes.size - WAV_HEADER_BYTES
        val shorts = ShortArray(pcmByteCount / 2)
        ByteBuffer.wrap(bytes, WAV_HEADER_BYTES, pcmByteCount)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
            .get(shorts)
        return shorts
    }
}
