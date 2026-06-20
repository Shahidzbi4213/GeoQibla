# Contributing

Thanks for helping improve GeoQibla.

## Development Commands

```bash
./gradlew :shared:allTests
./gradlew :androidApp:assembleDebug
mkdocs build --strict
```

Use Android Studio or the Android CLI for device deployment and layout inspection.

## Documentation

Docs live in `docs/` and are configured by `mkdocs.yml`. Keep README snippets and docs snippets aligned with the latest published version.

## Release Automation

The Publish workflow reads `VERSION_NAME` from `gradle.properties` by default, publishes the shared artifact when Maven Central does not already have that version, and then creates or updates the matching GitHub Release.

To backfill a GitHub Release for an already-published Maven Central version, run the Publish workflow manually and pass that version, for example `0.0.1`.

## Pull Requests

- Open an issue first for broad API or behavior changes.
- Keep changes focused and include tests for runtime behavior.
- Update documentation when public APIs, platform setup, or examples change.
- Validate sensor and permission behavior on a physical device when relevant.

## Issue Reports

Include platform, OS version, device model, GeoQibla version, expected behavior, actual behavior, and reproduction steps.
