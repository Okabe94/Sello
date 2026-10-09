# Sello

A new local-only Android financial application, modernizing the legacy app through
a deliberately smaller, tested MVP and the Sello visual language.

## Start here

- [Architecture and guardrails](ARCHITECTURE.md): ownership, financial/recovery
  contracts, technologies, MVP scope and developer tooling requirements.
- [MVP Kanban board](docs/planning/mvp/BOARD.md): eight epics and 36 scoped tickets.
- [Post-MVP roadmap board](docs/planning/mvp/ROADMAP.md): signed carryover epic and
  seven dependency-linked tasks, separate from MVP release acceptance.
- [Delivery workflow and gates](docs/planning/mvp/README.md): dependencies,
  definition of done, evidence and Jira handoff.
- [Independent executor guide](docs/planning/mvp/EXECUTION_GUIDE.md): prerequisites,
  file ownership, tools, verification procedures and source library.
- [Local visual reference](docs/design/README.md): Sello snapshot and provenance.
- [Inspected baseline](docs/planning/mvp/BASELINE.md): what actually builds today.

The app is still the generated greeting. SELLO-003 added the module boundaries
(`:domain`, `:data`, `:design-system`, `:app`, `:catalog`) and the debug/catalog
identities; the catalog is an empty shell. Financial features, live-testing
sandbox, catalog components, architecture checks and CI are planned, not implemented.

## Local host verification (G1)

Use the installed Android SDK and Android Studio JBR, or the validated replacement
matrix documented by SELLO-002:

```bash
ANDROID_HOME=/home/okabe94/Android/Sdk JAVA_HOME=/opt/android-studio/jbr \
  PATH="/opt/android-studio/jbr/bin:$PATH" \
  ./gradlew :domain:test :data:testDebugUnitTest :design-system:testDebugUnitTest \
  :app:testDebugUnitTest :catalog:testDebugUnitTest lintDebug \
  :app:assembleDebug :app:assembleRelease :catalog:assembleDebug --continue
```

Build/dependency/IDE caches, machine-local configuration and signing secrets are
not tracked. No Git remote or hosted Jira project is configured by this setup.
