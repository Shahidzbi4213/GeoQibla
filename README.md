# GeoQibla

GeoQibla is a Kotlin Multiplatform Compose library for Qibla direction. It owns
foreground location permission, framework location updates, compass/heading
updates, state orchestration, and a default shared UI for Android and iOS KMP
consumers.

Coordinates:

```kotlin
implementation("com.shahid.tech.qibla:geoqibla:<version>")
```

The Maven Central namespace `com.shahid.tech.qibla` must be verified before any
release. If that namespace cannot be verified, change the group ID before
publishing.

## Public API

```kotlin
val controller = rememberQiblaController(
    config = QiblaConfig(
        nearDegrees = 10f,
        alignedDegrees = 3f,
    ),
)

GeoQiblaScreen(
    controller = controller,
    style = QiblaStyle.default(),
    strings = QiblaStrings.default(),
    slots = QiblaSlots.default(),
)
```

Use `controller.state` for a headless/custom UI:

```kotlin
interface QiblaController {
    val state: StateFlow<QiblaState>
    fun start()
    fun stop()
    fun retryLocation()
    fun requestPermission()
    fun openLocationSettings()
    fun openAppSettings()
    fun dismissCalibration()
}
```

The default `GeoQiblaScreen` starts the controller on composition and stops it
when disposed. Custom/headless consumers can call `start()` and `stop()`
directly.

## Platform Setup

Android:

- Minimum SDK: 26.
- The library manifest declares `ACCESS_COARSE_LOCATION` and
  `ACCESS_FINE_LOCATION`.
- The default Android backend uses `LocationManager`, `SensorManager` rotation
  vector, accelerometer/magnetometer fallback, and `GeomagneticField`. It does
  not require Google Play Services.

iOS:

- Minimum supported host app target: iOS 15+.
- Add these keys to the host app `Info.plist`:
  - `NSLocationWhenInUseUsageDescription`
  - `NSLocationAlwaysAndWhenInUseUsageDescription`
  - `CADisableMinimumFrameDurationOnPhone`
- The sample app embeds Compose with `ComposeUIViewController`.

## Customization

- `QiblaStyle` controls colors, dimensions, typography, shapes, and animation
  timings.
- `QiblaStrings.english()` and `QiblaStrings.arabic()` provide built-in EN/AR
  strings.
- `QiblaSlots` can replace top bar, compass dial, target badge, status rows,
  state message, calibration sheet, or action buttons.
- Public component composables include `QiblaCompassDial`, `QiblaStatusPanel`,
  and `QiblaStateMessage`.

## Verification

Useful local checks:

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :shared:compileKotlinIosSimulatorArm64
./gradlew :shared:compileKotlinIosArm64
./gradlew :androidApp:assembleDebug
./gradlew :shared:publishToMavenLocal
```

The broad `:shared:allTests` task may also run iOS simulator tests depending on
the local host setup.

## Publishing

The shared module is configured for a single Maven coordinate:

```text
com.shahid.tech.qibla:geoqibla
```

Kotlin Multiplatform publishes a root publication plus target-specific
publications. Verify after local publish that the root metadata, Android
artifact, and iOS artifacts are present.

Release publishing uses `.github/workflows/publish.yml` on GitHub releases. The
workflow expects these repository secrets:

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `SIGNING_KEY_ID`
- `SIGNING_PASSWORD`
- `GPG_KEY_CONTENTS`

The default local `VERSION_NAME` is `0.1.0-SNAPSHOT`. The workflow strips a
leading `v` from the GitHub release tag and passes that value as the release
version.
