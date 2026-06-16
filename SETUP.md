# RidesTracker — Setup Guide

## Prerequisites
- Android Studio Hedgehog (2023.1) or newer
- Android SDK 35
- Java 17+
- **No API key needed** — uses OpenStreetMap (100% free)

## 1. Configure local.properties
Edit `local.properties` in the project root — just set your SDK path:
```
sdk.dir=/path/to/your/Android/sdk
```

## 2. Open in Android Studio
```bash
git clone <repo-url>
cd RidesTracker
# Open in Android Studio: File → Open → select RidesTracker folder
```
Android Studio will sync Gradle automatically.

## 4. Build & Run
- **Debug APK on device**: Click the green Run ▶ button in Android Studio
- **Debug APK via terminal**:
  ```bash
  ./gradlew assembleDebug
  # APK at: app/build/outputs/apk/debug/app-debug.apk
  adb install app/build/outputs/apk/debug/app-debug.apk
  ```
- **Release APK**:
  ```bash
  ./gradlew assembleRelease
  # Configure signing in app/build.gradle.kts first
  ```

## 5. Required Permissions
The app requests at runtime:
- `ACCESS_FINE_LOCATION` — GPS tracking
- `ACCESS_BACKGROUND_LOCATION` — tracking continues when screen is off
- `POST_NOTIFICATIONS` — foreground service notification

## Architecture
```
Kotlin + Jetpack Compose + MVVM
├── Hilt (dependency injection)
├── Room (SQLite database)
├── Google Maps SDK (map + polyline)
├── FusedLocationProviderClient (GPS via Foreground Service)
├── Vico (charts)
└── Retrofit (Open-Meteo weather API — free, no key needed)
```

## Features
- Live GPS tracking with orange polyline on Google Maps
- Distance, speed (current/avg/max), elevation tracking
- Ride history saved to local SQLite database
- Ride summary with full stats after each ride
- Maintenance tracker (oil, chain, tires with km-based reminders)
- Fuel log with L/100km efficiency (motorcycle users)
- Crash detection via accelerometer + emergency SOS
- Weather overlay (Open-Meteo API — free, no key needed)
- GPX export
- Dark theme optimized for outdoor use
