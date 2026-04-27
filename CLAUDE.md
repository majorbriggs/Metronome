# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires connected device/emulator)
./gradlew lint                   # Run Android Lint
./gradlew clean                  # Clean build artifacts
```

## Architecture

**Platform:** Wear OS (smartwatch) app — minimum SDK 30, target SDK 36.

**Single-module** project under `:app`. All code lives in `com.majorbriggs.metronome.presentation`.

**UI:** Jetpack Compose with Wear Compose Material. Entry point is `MainActivity`, which installs the splash screen and sets the Compose content. `MetronomeTheme` wraps `MaterialTheme` for Wear OS.

**Key tech stack:**
- Kotlin 2.0.21, AGP 9.0.1
- Compose BOM 2024.09.00, Wear Compose Material/Foundation 1.2.1
- Play Services Wearable 19.0.0 for Wear OS APIs
- Dependencies managed via version catalog at `gradle/libs.versions.toml`

**Project state:** Early-stage template. The current UI is a placeholder greeting. No ViewModel, persistence, networking, or DI framework is in place yet.

**Lint:** `app/lint.xml` suppresses `IconLocation` for tile previews.
