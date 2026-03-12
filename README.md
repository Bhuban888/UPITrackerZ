# UPI Tracker 📱💸

A modern Android app to automatically track UPI transactions from SMS messages.

## Features

- 📩 **Auto SMS Parsing** — Detects UPI transactions from bank SMS (HDFC, SBI, ICICI, Axis, Kotak, Paytm, PhonePe, Google Pay, and more)
- 📊 **Dashboard** — Monthly income vs spending summary
- 📋 **Transaction List** — Search, filter by Credit/Debit
- 📈 **Analytics** — Category-wise spending breakdown with visual bars
- 💰 **Budget Manager** — Set monthly limits per category with progress tracking
- ➕ **Manual Entry** — Add transactions manually

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Hilt** for Dependency Injection
- **Room** for local database
- **MVVM** architecture with StateFlow
- **WorkManager** ready for background tasks

## Build Instructions

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 35

### Steps
```bash
git clone https://github.com/YOUR_USERNAME/UPITracker.git
cd UPITracker
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions
Push to `main` — the CI will automatically build a debug APK available as a workflow artifact.

## Permissions Required

| Permission | Purpose |
|---|---|
| `RECEIVE_SMS` | Auto-detect new UPI SMS |
| `READ_SMS` | Read existing SMS for import |
| `POST_NOTIFICATIONS` | Transaction alerts |

## Supported Banks/Apps

HDFC, SBI, ICICI, Axis, Kotak, Yes Bank, PNB, Paytm, PhonePe, Google Pay, and most other Indian banks via generic UPI SMS pattern matching.

## License

MIT
