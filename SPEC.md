# Metronome for Wear OS — Product Specification

## 1. Overview

A free, open-source metronome application for Wear OS, targeting musicians who need reliable rhythmic guidance while playing an instrument. The primary design constraint is that the metronome must function accurately without any screen interaction once started — the device may be on a wrist, the screen asleep, and the user actively playing.

**Distribution:** Google Play, free, no ads, no IAP.  
**Target platform:** Wear OS (min SDK 30, standalone — no companion phone app required).

---

## 2. Core Functionality

### 2.1 Tempo

- BPM range: **40 – 240 BPM**, integer steps.
- Default BPM on first launch: **120**.
- Selected BPM persists across app restarts (stored in DataStore preferences).

### 2.2 Time Signatures

Supported signatures (user-selectable):

| Signature | Beats per bar | Accent on |
|-----------|--------------|-----------|
| 4/4       | 4            | Beat 1    |
| 3/4       | 3            | Beat 1    |
| 6/8       | 6            | Beats 1 and 4 |

Selected signature persists across restarts.

### 2.3 Beat Signalling

Each beat produces a signal. Beat type determines signal intensity:

| Beat type       | Audio cue              | Haptic cue           |
|-----------------|------------------------|----------------------|
| Accented beat   | Higher-pitched click   | Long vibration (~80 ms) |
| Regular beat    | Lower-pitched click    | Short vibration (~25 ms) |

The user selects one of three **feedback modes** (persisted):
- **Vibration only** — no audio output
- **Audio only** — no haptic output
- **Both** — vibration and audio simultaneously

Audio uses `AudioTrack` in streaming mode — the engine writes PCM samples continuously and embeds click sounds at sample-exact beat positions, eliminating OS-scheduler jitter. Vibration uses `VibrationEffect` (API 26+).

### 2.4 Tap Tempo

- A dedicated tap area/button on the main screen.
- Tapping it **2–8 times** in succession calculates BPM from the average inter-tap interval.
- If the gap between taps exceeds **2 seconds**, the sequence resets.
- Calculated BPM is rounded to the nearest integer and clamped to [40, 240].
- Tap tempo updates the BPM selector live and takes effect immediately if the metronome is running.

---

## 3. User Interface

### 3.1 Screen Layout (single screen)

The app has **one main screen**. No navigation between multiple screens — all controls are visible or accessible via scroll on the main screen.

**Layout (top to bottom):**

1. **Beat indicator row** — a row of dots (one per beat in the current bar). The active beat dot "bounces" or pulses on each tick. Accented beats use a distinct color/size.
2. **BPM display** — large numeral showing current BPM.
3. **BPM rolling selector** — a vertical scrollable picker (like Wear OS number picker). Scrolling it changes the BPM. Supports continuous scroll (wraps are not needed; clamps at 40 and 240).
4. **Time signature selector** — a horizontal swipeable or tappable chip row cycling through `4/4 → 3/4 → 6/8 → 4/4`.
5. **Start / Stop button** — prominent center action. Toggles metronome on/off.
6. **Tap Tempo button** — secondary button below start/stop.
7. **Feedback mode toggle** — icon button cycling through `vibration → audio → both`.

> Wear OS rotary crown / bezel support for BPM adjustment can be added in a later version.

### 3.2 Visual Beat Indicator

- While running, the dot corresponding to the current beat scales up and/or changes color on each tick.
- Accented beat dot uses the theme's primary accent color; regular beats use a secondary color.
- While stopped, all dots are shown in their idle (dim) state.

### 3.3 Always-On Display (AOD)

- When the screen enters ambient mode (dim), show a minimal layout: current BPM + beat position indicator only (no interactive controls).
- The metronome continues ticking in the background regardless of ambient state.

---

## 4. Background Operation & System Integration

This is the highest-priority reliability requirement.

### 4.1 Foreground Service

- When the metronome starts, launch a **Foreground Service** (`MetronomeService`) with a persistent notification.
- The notification shows: current BPM, time signature, and a **Stop** action button (so the user can stop without reopening the app).
- The service runs in its own coroutine using a high-priority ticker loop. Use `Handler` with `SystemClock.elapsedRealtime()` for timing, not `Thread.sleep`, to avoid drift.
- When the metronome stops, the service stops and the notification is dismissed.

### 4.2 Wake Lock

- The foreground service acquires a `PARTIAL_WAKE_LOCK` while the metronome is active, ensuring the CPU does not sleep between beats even with the screen off.
- Wake lock is released immediately when the metronome stops.

### 4.3 Wear OS Exercise / Workout Integration

- Request and maintain a **Wear OS `AmbientMode` keepalive** (implement `AmbientModeSupport` in `MainActivity`) so the app is not aggressively lifecycle-managed while in the foreground.
- Register the app as a **Health Services exercise client** (ongoing activity) using the Jetpack `health-services-client` library. This signals to the OS that an active exercise is in progress, suppressing automatic pausing, throttling, or watch-face takeover.
- On exercise session start/stop, mirror the metronome start/stop state.

### 4.4 Timing Accuracy

Beat timing is driven by the audio hardware clock, not the OS scheduler, via `AudioTrack` streaming:

- `MetronomeAudioEngine` runs a dedicated thread at `THREAD_PRIORITY_URGENT_AUDIO` that continuously writes PCM data to an `AudioTrack` (streaming mode, `WRITE_BLOCKING`). The thread sleeps inside the kernel FIFO write — no sleep loops, no `Handler` delays.
- Beat positions are computed in PCM samples using double-precision arithmetic (`SAMPLE_RATE * 60.0 / bpm`), accumulated across beats. This eliminates integer-division drift entirely; timing error is bounded to ±1 sample (±22 µs at 44100 Hz) and never accumulates.
- `AudioTrack` is created with `PERFORMANCE_MODE_LOW_LATENCY`, requesting the hardware's lowest-latency audio path.
- Audio stream uses `USAGE_ALARM`, which is not silenced by Do Not Disturb and is unaffected by Bluetooth audio routing changes on Wear OS.
- Beat callbacks (for UI and haptics) fire from the audio thread immediately before each click write, so the visual indicator updates while the click travels through the hardware FIFO.
- For vibration-only mode (no audio), a `BeatScheduler` (`HandlerThread` + `postAtTime`) drives timing instead; haptic feedback does not require sample-level precision.
- Achieved timing jitter: ±22 µs (audio modes) vs a previous ±10–200 ms with `SoundPool`.

---

## 5. Permissions & Manifest

Required permissions:

```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

`RECEIVE_BOOT_COMPLETED` is used to restore the metronome state if the device restarts while the service was running (restore to stopped state — do not auto-restart, just restore persisted BPM/signature settings).

Hardware feature declarations:

```xml
<uses-feature android:name="android.hardware.type.watch" />
```

Standalone app declaration (no phone companion required):

```xml
<meta-data android:name="com.google.android.wearable.standalone" android:value="true" />
```

---

## 6. Data Persistence

Use **Jetpack DataStore (Preferences)** to persist:

| Key | Type | Default |
|-----|------|---------|
| `bpm` | Int | 120 |
| `time_signature` | String enum (`4_4`, `3_4`, `6_8`) | `4_4` |
| `feedback_mode` | String enum (`vibration`, `audio`, `both`) | `both` |

No other storage required for v1.

---

## 7. Audio Assets

Two short PCM audio clips bundled in `res/raw/`:

| File | Description | Specs |
|------|-------------|-------|
| `beat_accent.wav` | High-pitched click | 44100 Hz, 16-bit mono, 352 samples (~8 ms) |
| `beat_regular.wav` | Low-pitched click | 44100 Hz, 16-bit mono, 352 samples (~8 ms) |

At engine start, both files are decoded in-process by skipping the 44-byte WAV header and reading the remaining bytes as little-endian 16-bit PCM into `ShortArray`. These arrays are embedded directly into the `AudioTrack` stream at beat boundaries. Audio uses the `USAGE_ALARM` stream (alarm volume, bypasses Do Not Disturb).

---

## 8. Architecture

```
com.majorbriggs.metronome
├── presentation/
│   ├── MainActivity.kt          — single activity, Compose host, ambient mode
│   ├── MetronomeScreen.kt       — main Composable screen
│   ├── MetronomeViewModel.kt    — UI state, bridges Service ↔ UI
│   └── theme/
│       └── Theme.kt
├── service/
│   ├── MetronomeService.kt      — ForegroundService, beat loop, wake lock
│   └── BeatScheduler.kt        — timing logic, drift correction
├── audio/
│   └── MetronomeAudioEngine.kt  — AudioTrack streaming engine (timing + playback)
├── haptics/
│   └── BeatVibrator.kt          — VibrationEffect wrapper
├── exercise/
│   └── ExerciseSessionManager.kt — Health Services integration
└── data/
    └── MetronomePreferences.kt   — DataStore read/write
```

**State flow:**
1. `MetronomeViewModel` reads persisted settings from `MetronomePreferences` and exposes UI state as `StateFlow`.
2. User interactions in `MetronomeScreen` call ViewModel methods.
3. Start/Stop commands from ViewModel bind/unbind to `MetronomeService` via `ServiceConnection`.
4. `MetronomeService` starts the appropriate engine: `MetronomeAudioEngine` (AudioTrack streaming) for audio/both modes, `BeatScheduler` (HandlerThread) for vibration-only. Beat callbacks call `BeatVibrator` for haptics and push beat position to the repository.
5. Beat position updates the `MetronomeScreen` beat indicator in real time.

---

## 9. Google Play Requirements

To meet Play store requirements for a first release:

- **Target SDK:** 35+ (enforced by Play as of 2024).
- **App signing:** configure release keystore; document the signing config separately (never commit keys).
- **Store listing assets:** 512×512 icon, feature graphic (1024×500), at least 2 Wear OS screenshots.
- **Privacy policy:** required even for apps with no data collection; host a simple "we collect no data" policy page.
- **Content rating:** complete IARC questionnaire (expected: Everyone).
- **Accessibility:** all interactive elements must have `contentDescription` for TalkBack.

---

## 10. Out of Scope (v1)

- Rotary crown / bezel BPM control (future enhancement)
- Custom BPM presets / named tempo labels
- Multiple accent patterns beyond the defined time signatures
- Phone companion app
- Paid features or ads
- Polyrhythm or subdivision modes
- Visual themes / customization
