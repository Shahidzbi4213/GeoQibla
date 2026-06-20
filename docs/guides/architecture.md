# Architecture

GeoQibla is a small KMP Compose library with a common public API and platform-specific service implementations.

## Common API

The common source set contains:

- `GeoQiblaScreen` for the default UI.
- `QiblaController` and `rememberQiblaController`.
- `QiblaConfig` for runtime thresholds.
- `QiblaState` and related state models.
- `QiblaStyle`, `QiblaStrings`, and `QiblaSlots`.
- Public reusable composables for dial, message, and status display.

## Platform Services

`QiblaPlatformServices` is internal. It abstracts permission requests, location observation, orientation observation, settings actions, and haptics.

Android and iOS provide `actual` implementations for `rememberQiblaPlatformServices()` and `currentTimeMillis()`.

## State Resolver Priority

The controller reduces raw platform inputs into `QiblaUiState` with this priority:

1. Error message.
2. Not started.
3. Location permission.
4. Location services disabled.
5. Missing location fix.
6. Missing heading data.
7. Tilt warning.
8. Calibration warning.
9. Stable alignment.
10. Near Qibla.
11. Ready.

## Bearing and Distance Math

`QiblaMath` calculates:

- Bearing from the current location to the Kaaba.
- Distance to the Kaaba using a spherical earth radius.
- Normalized compass degrees.
- Shortest angular distance for left/right adjustment.
- Magnetic-to-true azimuth conversion.
- Smoothed heading values.

## Alignment Tracker

`QiblaAlignmentTracker` separates near and aligned states. Near alignment is immediate within `nearDegrees`. Stable alignment requires the heading to remain within `alignedDegrees` for `stableAlignedDurationMillis`.

## Tests

The shared tests cover Qibla math, config validation, UI state resolution, and alignment tracking.
