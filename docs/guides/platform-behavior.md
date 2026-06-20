# Platform Behavior

GeoQibla keeps the public API in common Kotlin and implements location and heading services per platform.

## Android

Android uses:

- `LocationManager` for last-known and live location updates.
- `SensorManager` with rotation vector when available.
- Accelerometer plus magnetic field sensors as a fallback heading source.
- `GeomagneticField` to convert magnetic azimuth to true north when location declination is available.
- Activity Result APIs for runtime location permission.
- App settings and location settings intents for recovery actions.
- View haptics for stable alignment feedback.

The library manifest declares coarse and fine location permissions. The default UI requests permission at runtime.

## iOS

iOS uses:

- `CLLocationManager` for location and heading updates.
- `trueHeading` when available, otherwise `magneticHeading`.
- Core Location authorization status mapped to `QiblaLocationAccess`.
- The app settings URL for settings recovery.

`performAlignmentHaptic()` is currently a no-op on iOS.

## Permission States

`QiblaLocationAccess` values are:

- `UNKNOWN`
- `NOT_DETERMINED`
- `GRANTED`
- `DENIED`
- `PERMANENTLY_DENIED`

The default screen maps these to permission-required, denied, or settings states.

## Calibration and Tilt

The controller reports `CALIBRATION_NEEDED` when sensor accuracy is unreliable or low, or when magnetic field strength is outside the configured range.

The controller reports `TILTED` when the device tilt exceeds `QiblaConfig.tiltLimitDegrees`.

## Sensor Availability

If heading data cannot be produced, the UI enters `SENSOR_UNAVAILABLE`. On Android this can happen when neither rotation vector nor accelerometer plus magnetometer data is available. On iOS this can happen when Core Location heading updates are unavailable.
