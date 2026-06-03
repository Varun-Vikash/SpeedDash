# Jazz Dashboard

A premium car dashboard application featuring both a native Android app and a web prototype.

## Project Structure

```
JazzDashboard/
├── android/          # Native Android app (Kotlin + Jetpack Compose)
│   └── app/src/main/java/com/example/jazzdashboard/
│       ├── car/          # Android Auto integration
│       ├── location/     # GPS tracking
│       ├── media/        # Media playback & lyrics sync
│       ├── sensors/      # G-force & accelerometer
│       └── ui/           # Compose UI (screens, components, theme)
└── web/              # Web-based dashboard prototype (HTML/CSS/JS)
```

## Features

- **ECO Mode**: Relaxed driving dashboard with RPM gauge, speed display, gear indicator, and GPS tracking
- **SPORT Mode**: Performance-oriented dashboard with G-force meter, lap timer, and aggressive styling
- **Android Auto**: Companion display for car head units
- **Media Integration**: Now-playing widget with live lyrics sync
- **GPS Tracking**: Real-time speed, location name via geocoding
- **Watch Face**: Omega Speedmaster-inspired analog clock

## Building

### Android App
```bash
cd android
./gradlew assembleDebug
```

### Web Prototype
Open `web/dashboard.html` directly in a browser.

## Tech Stack

- **Android**: Kotlin, Jetpack Compose, Material 3, Google Play Services (Location)
- **Web**: HTML5, Tailwind CSS, vanilla JavaScript
