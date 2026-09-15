## Summary

- 

## Architectural & Production Sign-Off

- [ ] **Clean Architecture & Boundaries**: `commonMain` remains pure Kotlin and platform agnostic without platform imports.
- [ ] **No God ViewModels / Controllers**: ViewModels and Controllers delegate to domain/use-cases and stay within size limits.
- [ ] **Dependency Injection (DI) Compliance**: Dependencies are provided via constructor injection; no concrete singleton or service instantiations in class bodies.
- [ ] **Composable Decomposition**: Complex screens are broken down into small, modular component files.
- [ ] **Spaghetti Code & Metric Guards**: Line length, nesting depth, and complexity boundaries are observed.

## Automated Verification

- [ ] `.github/scripts/check-code-health.py` passes cleanly
- [ ] `./gradlew :shared:allTests` passes locally
- [ ] `./gradlew :androidApp:assembleDebug` passes locally
- [ ] `mkdocs build --strict` passes locally

## Reviewer Sign-Off

- [ ] Code review completed and approved by maintainer.
- [ ] All conversation threads resolved.
