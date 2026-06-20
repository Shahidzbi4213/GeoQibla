# Styling and Localization

`GeoQiblaScreen` accepts styling and text models directly.

```kotlin
GeoQiblaScreen(
    controller = controller,
    style = QiblaStyle.default(),
    strings = QiblaStrings.arabic(),
)
```

## QiblaStyle

`QiblaStyle` groups visual tokens:

- `QiblaColors`
- `QiblaDimensions`
- `QiblaTypography`
- `QiblaShapes`
- `QiblaAnimationTimings`

```kotlin
val style = QiblaStyle.default().copy(
    colors = QiblaColors(
        primary = Color(0xFF0A7C66),
        qibla = Color(0xFF0A7C66),
        aligned = Color(0xFF167D38),
    ),
)
```

Use a full `QiblaColors` value when replacing colors because the class has concrete defaults for every token.

## Dimensions

`QiblaDimensions` controls screen padding, compact padding, content gap, panel padding, compass minimum and maximum size, and status row minimum height.

## Typography and Shapes

`QiblaTypography` controls title, subtitle, status title, body, label, and numeric text styles.

`QiblaShapes` controls panel, button, and badge shapes.

## Animations

`QiblaAnimationTimings` controls compass rotation animation and state-change timing tokens.

## QiblaStrings

`QiblaStrings.default()` returns English strings. The library also provides:

- `QiblaStrings.english()`
- `QiblaStrings.arabic()`

The Arabic strings set `layoutDirection = LayoutDirection.Rtl`. `GeoQiblaScreen` applies that layout direction around the whole screen.

## Custom Copy

```kotlin
val strings = QiblaStrings.english().copy(
    title = "Prayer direction",
    subtitle = "Turn until the marker points forward",
    alignedTitle = "Ready",
    alignedMessage = "You are facing the Qibla.",
)
```

Every visible default UI label and action is supplied by `QiblaStrings`.
