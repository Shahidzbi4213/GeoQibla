# Default Screen

`GeoQiblaScreen` is the ready-made Compose UI.

```kotlin
GeoQiblaScreen(
    controller = controller,
    style = QiblaStyle.default(),
    strings = QiblaStrings.default(),
    slots = QiblaSlots.default(),
)
```

## Layout

The screen adapts automatically:

- Compact width uses a vertical layout with top bar, compass, message, actions, and status rows.
- Wide width uses a two-column layout with the compass on one side and the status content on the other.
- The content uses safe content padding so it avoids system bars.

## State Messages

The default message panel maps `QiblaUiState` to localized text from `QiblaStrings`.

| State group | What the screen shows |
| --- | --- |
| Permission | Request permission, denied, and permanently denied messaging. |
| Location | Location disabled and locating messaging. |
| Sensor | Sensor unavailable, calibration, and tilted messaging. |
| Direction | Ready, near Qibla, and aligned messaging. |
| Error | The controller error message when one is available. |

## Action Buttons

Default action buttons call the controller:

| UI state                                   | Action                                         |
|--------------------------------------------|------------------------------------------------|
| `IDLE`                                     | `start()`                                      |
| `PERMISSION_REQUIRED`, `PERMISSION_DENIED` | `requestPermission()`                          |
| `PERMISSION_PERMANENTLY_DENIED`            | `openAppSettings()`                            |
| `LOCATION_DISABLED`                        | `openLocationSettings()` and `retryLocation()` |
| `SENSOR_UNAVAILABLE`, `ERROR`              | `retryLocation()`                              |
| `CALIBRATION_NEEDED`                       | `dismissCalibration()`                         |

Use `QiblaSlots.actionButtons` when you need product-specific buttons.

## Built-In Regions

The default screen is composed from public reusable pieces:

- `QiblaCompassDial`
- `QiblaStatusPanel`
- `QiblaStateMessage`

Slots let you replace full regions while the default controller continues to own runtime behavior.
