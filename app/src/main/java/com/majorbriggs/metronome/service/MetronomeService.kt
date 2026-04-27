package com.majorbriggs.metronome.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.majorbriggs.metronome.R
import com.majorbriggs.metronome.audio.MetronomeAudioEngine
import com.majorbriggs.metronome.data.FeedbackMode
import com.majorbriggs.metronome.data.MetronomeRepository
import com.majorbriggs.metronome.data.TimeSignature
import com.majorbriggs.metronome.haptics.BeatVibrator
import com.majorbriggs.metronome.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MetronomeService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "metronome_channel"
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_UPDATE = "UPDATE"
        const val EXTRA_BPM = "bpm"
        const val EXTRA_TIME_SIG = "time_sig"
        const val EXTRA_FEEDBACK_MODE = "feedback_mode"
    }

    @Inject lateinit var beatScheduler: BeatScheduler
    @Inject lateinit var audioEngine: MetronomeAudioEngine
    @Inject lateinit var beatVibrator: BeatVibrator
    @Inject lateinit var repository: MetronomeRepository

    private var wakeLock: PowerManager.WakeLock? = null
    private var bpm = 120
    private var timeSignature = TimeSignature.FOUR_FOUR
    private var feedbackMode = FeedbackMode.BOTH
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        audioEngine.setListener { beatIndex, isAccent -> handleBeat(beatIndex, isAccent) }
        beatScheduler.setListener { beatIndex, isAccent -> handleBeat(beatIndex, isAccent) }
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                bpm = intent.getIntExtra(EXTRA_BPM, 120)
                timeSignature = TimeSignature.fromKey(intent.getStringExtra(EXTRA_TIME_SIG) ?: "4_4")
                feedbackMode = FeedbackMode.fromKey(intent.getStringExtra(EXTRA_FEEDBACK_MODE) ?: "both")
                isRunning = true
                acquireWakeLock()
                startForeground(NOTIFICATION_ID, buildNotification())
                startEngines()
            }
            ACTION_STOP -> {
                stopMetronome()
                repository.onServiceStopped()
                stopSelf()
            }
            ACTION_UPDATE -> {
                val newBpm = intent.getIntExtra(EXTRA_BPM, bpm)
                val newSig = TimeSignature.fromKey(intent.getStringExtra(EXTRA_TIME_SIG) ?: timeSignature.key)
                val newMode = FeedbackMode.fromKey(intent.getStringExtra(EXTRA_FEEDBACK_MODE) ?: feedbackMode.key)
                val modeChanged = newMode != feedbackMode
                bpm = newBpm
                timeSignature = newSig
                feedbackMode = newMode
                if (isRunning) {
                    if (modeChanged) { stopEngines(); startEngines() }
                    else {
                        audioEngine.updateBpm(bpm)
                        audioEngine.updateTimeSignature(timeSignature)
                        beatScheduler.updateBpm(bpm)
                        beatScheduler.updateTimeSignature(timeSignature)
                    }
                    updateNotification()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRunning) {
            stopMetronome()
            repository.onServiceStopped()
        }
        audioEngine.release()
        beatScheduler.release()
    }

    private fun startEngines() {
        when (feedbackMode) {
            FeedbackMode.AUDIO, FeedbackMode.BOTH -> audioEngine.start(bpm, timeSignature)
            FeedbackMode.VIBRATION -> beatScheduler.start(bpm, timeSignature)
        }
    }

    private fun stopEngines() {
        audioEngine.stop()
        beatScheduler.stop()
    }

    private fun stopMetronome() {
        if (!isRunning) return
        isRunning = false
        stopEngines()
        releaseWakeLock()
        @Suppress("DEPRECATION")
        stopForeground(true)
    }

    private fun handleBeat(beatIndex: Int, isAccent: Boolean) {
        if (feedbackMode == FeedbackMode.VIBRATION || feedbackMode == FeedbackMode.BOTH) {
            vibrate(isAccent)
        }
        repository.onBeat(beatIndex)
    }

    private fun vibrate(isAccent: Boolean) {
        if (isAccent) beatVibrator.vibrateAccent() else beatVibrator.vibrateRegular()
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Metronome:BeatLock")
            .apply { acquire(60 * 60 * 1000L) }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Metronome", NotificationManager.IMPORTANCE_LOW)
            .apply { description = "Metronome running indicator" }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openPi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopPi = PendingIntent.getService(
            this, 0,
            Intent(this, MetronomeService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Metronome")
            .setContentText("$bpm BPM · ${timeSignature.displayName}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openPi)
            .addAction(0, "Stop", stopPi)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIFICATION_ID, buildNotification())
    }
}
