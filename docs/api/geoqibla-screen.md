# GeoQiblaScreen

```kotlin
@Composable
fun GeoQiblaScreen(
    controller: QiblaController,
    modifier: Modifier = Modifier,
    style: QiblaStyle = QiblaStyle.default(),
    strings: QiblaStrings = QiblaStrings.default(),
    slots: QiblaSlots = QiblaSlots.default(),
)
```

`GeoQiblaScreen` renders the default Qibla compass UI.

## Parameters

| Parameter | Description |
| --- | --- |
| `controller` | Required `QiblaController` that supplies state and actions. |
| `modifier` | Applied to the root surface. |
| `style` | Colors, dimensions, typography, shapes, and animation timings. |
| `strings` | Localized text and layout direction. |
| `slots` | Optional replacement composables for default regions. |

## Lifecycle

The screen starts the controller in a `DisposableEffect` and stops it when the screen leaves composition.

## Layout

The screen chooses compact or wide content based on available width. Both layouts use the same controller state and slot model.
