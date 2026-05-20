# ConnectionInfo

ConnectionInfo is a small Kotlin Multiplatform portfolio app for checking network details and running a simple upload/download speed test on Android and iOS.

The app is intentionally compact on the product side, but the implementation is structured as a modern Kotlin Multiplatform codebase:

- Shared Compose Multiplatform UI with Material 3 styling
- Unidirectional state flow through `StateFlow`
- Coroutine and Flow based networking, connectivity, and speed test updates
- Repository interfaces split from Android and network implementations
- Thin Android and SwiftUI host apps
- Focused shared tests for presentation state and speed statistics

## Tech Stack

- Kotlin
- Kotlin Multiplatform
- Compose Multiplatform
- Coroutines and Flow
- Ktor Client
- JUnit and kotlinx-coroutines-test

## Build

```bash
./gradlew :app:assembleDebug :shared:compileKotlinIosArm64
```

The iOS project is generated from `iosApp/project.yml`:

```bash
cd iosApp
xcodegen generate
xcodebuild -project ConnectionInfo.xcodeproj -scheme iosApp -configuration Debug -destination generic/platform=iOS build
```
