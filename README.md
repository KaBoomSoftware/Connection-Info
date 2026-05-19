# ConnectionInfo

ConnectionInfo is a small Android portfolio app for checking network details and running a simple upload/download speed test.

The app is intentionally compact on the product side, but the implementation is structured as a modern Kotlin Android codebase:

- Jetpack Compose UI with Material 3 styling
- Unidirectional state flow through `StateFlow`
- Coroutine and Flow based networking, connectivity, and speed test updates
- Repository interfaces split from Android and network implementations
- Hilt dependency injection
- Focused unit tests for presentation state and speed statistics

## Tech Stack

- Kotlin
- Jetpack Compose
- Coroutines and Flow
- Retrofit and OkHttp
- Hilt
- JUnit and kotlinx-coroutines-test

## Build

```bash
./gradlew assembleDebug testDebugUnitTest
```
