# Contributing

## Development Commands

Common local checks:

```bash
./gradlew :shared:allTests
./gradlew :androidApp:assembleDebug
mkdocs build --strict
```

Use Android Studio or the Android CLI for device deployment and layout inspection.

## Documentation

Docs are hand-written Markdown under `docs/` and configured by `mkdocs.yml`. The GitHub Pages workflow deploys with:

```bash
mkdocs gh-deploy --force
```

## Release Version

The library version source is `VERSION_NAME` in `gradle.properties`. Documentation snippets should match the latest published version.

## Release Automation

The Publish workflow publishes the shared artifact only when Maven Central does not already have the selected version. After that, it creates or updates the matching GitHub Release.

Run the Publish workflow manually with version `0.0.1` to backfill the missing release for the version that is already on Maven Central.

## Pull Requests

Keep changes focused:

- Runtime API changes should include tests and documentation updates.
- Documentation-only changes should pass `mkdocs build --strict`.
- Android behavior changes should be validated on a device when sensors or permissions are involved.

## Issue Reports

Include platform, OS version, device model, GeoQibla version, and reproduction steps.
