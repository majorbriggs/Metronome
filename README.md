# Metronome for Wear OS

A free, open-source metronome for Wear OS. Demo project created almost completely using Claude Code.

## Features

- **40 – 240 BPM** with rotary-crown and ±1 / ±5 button adjustment
- **Audio or haptic feedback** — high/low-pitched clicks or long/short vibrations per beat
- **Tap tempo** — tap 2–8 times to set the beat
- **Time signatures** — 4/4, 3/4, 6/8 (with accent patterns)
- **Always-on display** — ambient screen shows BPM and beat position without interactive controls
- **Background operation** — foreground service + wake lock keeps the beat going with the screen off
- **Persisted settings** — BPM, time signature, and feedback mode survive restarts

No ads. No in-app purchases. No data collection. [Privacy policy](https://majorbriggs.github.io/Metronome/privacy-policy.html).

## Requirements

- Wear OS 2.0+ (min SDK 30)

## Tech stack

Kotlin · Jetpack Compose for Wear OS · Hilt · DataStore · `AudioTrack` streaming · Wear OS Ongoing Activity

## License

MIT
