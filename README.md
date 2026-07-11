# Glucose Records Tracker

Glucose Records Tracker is a simple, offline-first Android app for recording daily blood glucose readings and viewing them in a clear chronological history.

The project began with a real usability problem: my 81-year-old grandfather was recording his blood glucose readings by hand on paper. I wanted to build a digital alternative that remained simple enough to use without introducing accounts, complicated menus, unnecessary features, or medical interpretation.

The result is a focused digital record book designed around ease of use, readability, and privacy.

> **Important:** Glucose Records Tracker is a record-keeping tool only. It does not provide medical advice, diagnose conditions, interpret glucose values, or replace guidance from a qualified healthcare professional.

## Features

- Four consistent daily reading categories:
  - Fasting
  - 2 Hours After Breakfast
  - 2 Hours After Lunch
  - 2 Hours After Dinner
- Large, readable cards for daily readings
- Custom numeric keypad with no QWERTY keyboard required
- Support for marking a reading as `â€”` when it was not taken
- Clear visual indication of the next unrecorded reading
- Edit existing records
- Confirmation before deleting a record
- Navigate between previous dates and today
- Choose a specific past date using the Android date picker
- Date-grouped record history, newest first
- Fully offline local storage
- No account required
- No network access
- No analytics
- No advertising
- No runtime permissions

## Accessibility and design goals

The app was initially designed for an older adult, so the interface prioritizes simplicity over feature density.

Design decisions include:

- Large text and touch targets
- High-contrast colours
- Clear, persistent labels
- No gesture-only core actions
- A custom number keypad for straightforward entry
- Visible date navigation
- Explicit confirmation for destructive actions
- A focused workflow with no medical dashboards or unnecessary statistics

The aim is not to build a full diabetes-management platform. It is to make one everyday taskâ€”recording readingsâ€”simpler.

## Privacy

Glucose Records Tracker is currently local-only.

Records are stored on the device using a Room database. The app does not require an account and does not send readings to a server.

The current version does not include:

- Cloud synchronization
- Analytics or tracking
- Advertising
- Remote data collection
- Medical interpretation

Because records are stored locally, uninstalling the app or clearing its application data may delete stored records. Export and backup functionality are planned future improvements.

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- Room
- Kotlin Coroutines and Flow
- Android ViewModel
- Gradle Kotlin DSL
- KSP

## Data model

Each day can contain one record for each reading category.

A reading can be:

- **Recorded** â€” contains a numeric glucose value
- **Skipped** â€” explicitly marked with `â€”`
- **Empty** â€” no record has been entered yet

The Room database enforces one record per date/category combination using a unique index.

The project includes a non-destructive database migration for the introduction of explicit recorded and skipped states.

## Build and run

### Requirements

- Android Studio
- JDK 17
- Android SDK Platform 35
- Android 8.0 (API 26) or newer

### Android Studio

1. Clone the repository.
2. Open the project folder in Android Studio.
3. Allow Gradle to sync and download the required dependencies.
4. Select an Android device or emulator.
5. Run the `app` configuration.

### Command line

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

On macOS or Linux:

```bash
./gradlew :app:assembleDebug
```

The generated debug APK can be found under:

```text
app/build/outputs/apk/debug/
```

## Project structure

app/
|-- schemas/                 Room database schemas
|-- src/
|   |-- main/
|   |   |-- java/.../data/   Entity, DAO, database, migrations, repository
|   |   |-- java/.../ui/     ViewModel, input logic, and Compose UI
|   |   `-- res/             App icon and Android resources
|   |-- test/                Local unit tests
|   `-- androidTest/         Instrumented and Room repository tests

## Testing

The project includes tests for core record-entry and persistence behaviour, including reading completion logic and repository operations.

Before relying on the app for important records, test the application on the intended physical Android device.

## Current limitations

The current release is intentionally focused and local-only.

It does not currently provide:

- Export to CSV or PDF
- Automatic backups
- Cloud synchronization
- Multiple user profiles
- Reminders or notifications
- Charts or trend analysis
- Medical interpretation

These limitations are deliberate unless a feature can be added without compromising the app's simplicity and privacy-first design.

## Roadmap

Potential future improvements include:

- CSV export and local backup
- Doctor-friendly PDF export
- Improved support for large Android font scaling
- Additional accessibility testing across devices
- Improved automated test coverage
- Optional data import and restore

## Contributing

Contributions are welcome.

Please preserve the core principles of the project:

- Accessibility first
- Offline-first and privacy-conscious
- Simple enough for older adults and users who prefer minimal interfaces
- Record keeping rather than medical interpretation
- No unnecessary tracking, advertising, or account requirements

For substantial changes, please open an issue first to discuss the proposed approach.

## License

This project is available under the MIT License. See `LICENSE` for details.
