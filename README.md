# Sello

A new local-only Android financial application, modernizing the legacy app through
a deliberately smaller, tested MVP and the Sello visual language.

## Start here

- [Architecture and guardrails](ARCHITECTURE.md): ownership, financial/recovery
  contracts, technologies, MVP scope and developer tooling requirements.
- [MVP Kanban board](docs/planning/mvp/BOARD.md): eight epics and 36 scoped tickets.
- [Delivery workflow and gates](docs/planning/mvp/README.md): dependencies,
  definition of done, evidence and Jira handoff.
- [Independent executor guide](docs/planning/mvp/EXECUTION_GUIDE.md): prerequisites,
  file ownership, tools, verification procedures and source library.
- [Local visual reference](docs/design/README.md): Sello snapshot and provenance.
- [Inspected baseline](docs/planning/mvp/BASELINE.md): what actually builds today.

The current app is the generated Android scaffold. Financial features, module
boundaries, live-testing sandbox, UI catalog and CI are planned, not implemented.
Only the initial scaffold/guardrails are in baseline commit `0a31cc2`.

## Local baseline verification

Use the installed Android SDK and Android Studio JBR, or the validated replacement
matrix documented by SELLO-002:

```bash
ANDROID_HOME=/home/okabe94/Android/Sdk JAVA_HOME=/opt/android-studio/jbr \
  PATH="/opt/android-studio/jbr/bin:$PATH" \
  ./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease --continue
```

Build/dependency/IDE caches, machine-local configuration and signing secrets are
not tracked. No Git remote or hosted Jira project is configured by this setup.
