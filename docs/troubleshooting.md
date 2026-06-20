# Troubleshooting

## Maven Resolution Fails

Confirm your repositories include Maven Central and the dependency uses the current version:

```kotlin
implementation("io.github.shahidzbi4213:geoqibla:0.0.1")
```

If you use a version catalog, sync after updating `libs.versions.toml`.

## Missing Android Permission

The library declares coarse and fine location permissions. If your app uses custom manifest merge rules, inspect the merged manifest and confirm both permissions remain.

## Permission Denied

If permission is denied, the default UI shows an allow action again. If the platform reports permanent denial, the UI opens app settings.

## Location Disabled

When device location services are off, the default UI shows a location settings action and a retry action.

## Sensor Unavailable

The screen enters `SENSOR_UNAVAILABLE` when the platform cannot produce heading data. Test on a physical device with compass support.

## Calibration Needed

Calibration appears when sensor accuracy is unreliable or low, or when magnetic field strength falls outside the configured range. Move the device in a figure-eight motion and keep it away from magnetic interference.

## Tilted Device

The screen enters `TILTED` when pitch or roll exceeds `QiblaConfig.tiltLimitDegrees`. Hold the device flatter for a more stable reading.

## Heading Accuracy Looks Wrong

Check the location fix, magnetic interference, calibration state, and whether the device has reliable heading sensors. Real compass validation should happen on physical devices.

## iOS Simulator Limitations

The simulator is not a reliable final test for heading behavior. Use it for layout and permission state work, then validate on device.

## Reporting Issues

When filing an issue, include:

- GeoQibla version.
- Platform and OS version.
- Device model.
- Relevant `QiblaState` values.
- Steps to reproduce.
