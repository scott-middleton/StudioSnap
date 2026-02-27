# AGENTS.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project overview
- Kotlin Multiplatform (KMP) app targeting **Android + iOS**.
- Primary module: `:composeApp`
  - Android: Gradle **application** target.
  - iOS: produces a `ComposeApp` framework consumed by Xcode (`iosApp/`).
- Shared UI is Compose Multiplatform (`composeApp/src/commonMain`).

## Common commands (Gradle)
Run from repo root.

### Build Android
- Debug APK:
  - `./gradlew :composeApp:assembleDebug`
- Install on a connected device/emulator:
  - `./gradlew :composeApp:installDebug`
- Release build:
  - `./gradlew :composeApp:assembleRelease`

### iOS framework (for Xcode)
- Build debug framework for Apple Silicon simulator:
  - `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
- Other variants:
  - `./gradlew :composeApp:linkDebugFrameworkIosArm64`
  - `./gradlew :composeApp:linkDebugFrameworkIosX64`

### Tests
- Run tests for all targets (aggregated report):
  - `./gradlew :composeApp:allTests`
- Android unit tests (JVM):
  - `./gradlew :composeApp:testDebugUnitTest`
- Run a single Android unit test class:
  - `./gradlew :composeApp:testDebugUnitTest --tests "com.middleton.imagecloneai.SomeTest"`
- Android instrumentation tests (device/emulator required):
  - `./gradlew :composeApp:connectedDebugAndroidTest`

### Lint / checks
- Android lint:
  - `./gradlew :composeApp:lint`
- Full verification task (will run the project’s configured checks):
  - `./gradlew :composeApp:check`

### Clean
- `./gradlew clean`

## Key entry points
- Shared app root + NavHost setup: `composeApp/src/commonMain/kotlin/com/middleton/imagecloneai/App.kt`
- Android entry activity (initializes image picker context + RevenueCat): `composeApp/src/androidMain/kotlin/com/middleton/imagecloneai/MainActivity.kt`
- iOS entry controller (initializes RevenueCat): `composeApp/src/iosMain/kotlin/com/middleton/imagecloneai/MainViewController.kt`
- iOS app shell (Xcode project): `iosApp/iosApp.xcodeproj`

## Repo docs that affect implementation
- `TODO.md`: current engineering backlog / next up items.

## High-level architecture
The shared code follows a Clean-Architecture-ish split plus per-feature modules:

### Core (shared)
Located under `composeApp/src/commonMain/kotlin/com/middleton/imagecloneai/core/`:
- `core/domain/`: domain models + service/repository interfaces (e.g., credits/auth).
- `core/data/`: concrete implementations (Ktor clients/datasources, Room DB/DAOs, mappers).
- `core/presentation/`: shared UI utilities, theming, and navigation abstraction.

### Features (shared)
Located under `composeApp/src/commonMain/kotlin/com/middleton/imagecloneai/feature/` (e.g. `splash`, `onboarding`, `auth`, `mainrestore`, `paywall`).

Common pattern inside a feature:
- `domain/` (models + repository interfaces + use cases when helpful)
- `data/` (repository implementations)
- `presentation/`
  - `ui_state/` a single state model per screen
  - `action/` sealed UI actions/events
  - `viewmodel/` state stored as `StateFlow`, mutations via `handleAction(...)`
  - `navigation/` feature-specific `*NavigationAction` mapping to shared navigation commands
  - `di/` Koin module wiring for that feature

### Dependency injection (Koin)
- App DI composition is centralized in `App.kt` via `KoinApplication { modules(...) }`.
- Platform-specific bindings use `expect/actual`:
  - Expect: `core/data/di/PlatformModule.kt`
  - Actuals:
    - Android: `composeApp/src/androidMain/.../core/data/di/PlatformModule.kt`
    - iOS: `composeApp/src/iosMain/.../core/data/di/PlatformModule.kt`
  - This is where `AppDatabase` and platform `GalleryRepository` implementations are provided.

## Navigation model
- Routes are type-safe and serializable: `core/presentation/navigation/NavigationCommand.kt` (`sealed interface Route`).
- `App.kt` owns the `NavHost` + `NavController` and registers destinations.
- ViewModels don’t touch `NavController` directly. They:
  1) emit a feature `*NavigationAction` (implements `NavigationAction`)
  2) which maps to a `NavigationCommand`
  3) handled by `SharedNavigationManager` -> `NavControllerNavigationHandler`

## Data/integrations overview
- Networking: Ktor `HttpClient` via `core/data/http/HttpClientProvider.kt`.
- Auth: Supabase configured in `core/data/di/SupabaseModule.kt` (includes ComposeAuth for Apple/Google native sign-in).
- Purchases: RevenueCat initialized in platform entrypoints; platform API keys via expect/actual in `composeApp/src/*Main/kotlin/com/middleton/imagecloneai/purchases/`.
- Persistence: Room in shared code (`core/data/database/*`) backed by bundled SQLite driver.
- Image restoration pipeline (Replicate):
  - Remote calls: `core/data/datasource/ReplicateRemoteDataSourceImpl.kt`
  - Orchestration: `feature/mainrestore/domain/usecase/RestoreImageUseCase.kt` (emits progress states consumed by `ProcessingViewModel`).

## Configuration notes (repo-specific)
- BuildKonfig (API keys/URLs) is defined in `composeApp/build.gradle.kts` under `buildkonfig { ... }` and generates `composeapp.BuildKonfig` used by shared code.
- `apple_auth/` contains an Apple Sign-In key (`.p8`) and helper script; avoid copying its contents into logs, issues, or AI prompts.