# Inspected starting point

**Date:** 2026-10-08 · **Initial commit:** `0a31cc2`
(`chore: initialize Sello app and architectural guardrails`) · **Branch:** `main`

## Repository

Git was initialized locally and the existing app, wrapper/build configuration,
resources, sample tests, architecture and agent instructions were committed.
The initial commit contains 41 files. Machine-local `local.properties`, IDE/build/
dependency caches and signing files are excluded. No remote/PR/hosted Jira board
was created. Planning documents and board tooling are subsequent working-tree changes.

## Implemented today

- One `:app` module, namespace/application ID `com.software.sello`.
- Generated Compose greeting/theme, launcher assets and Android manifest.
- One sample JVM arithmetic test and one sample instrumented context test.
- Architecture and future development instructions, not financial implementation.

The catalog reports AGP `9.3.3`, Kotlin `2.2.10`, Compose BOM `2026.02.01`;
wrapper is Gradle `9.5.0`. Configured minimum SDK is 30 and compile/target SDK 37;
daemon toolchain is configured for JDK 25 while Java bytecode target is 11.
These are inspected settings, not a completed compatibility/support decision.
Release optimization is currently disabled; OS backup is currently enabled by
the generated manifest. SELLO-002/032/035 deliberately audit/remediate these choices.

## Fresh commands and results

The following environment prefixes were used for both Gradle invocations:

```bash
ANDROID_HOME=/home/okabe94/Android/Sdk JAVA_HOME=/opt/android-studio/jbr \
  PATH="/opt/android-studio/jbr/bin:$PATH"
```

1. `./gradlew testDebugUnitTest lintDebug assembleDebug --continue`
   — **passed**, 55 seconds, 51 executed tasks. XML reports show **one JVM test**,
   zero failures/errors/skips. Lint passed with **17 warnings**; not a warning-free
   result and not proof of security/accessibility or financial correctness.
2. `./gradlew assembleRelease assembleDebugAndroidTest --continue`
   — **passed**, 22 seconds, 97 actionable tasks (76 executed, 21 up-to-date).
   This proves release/test APK assembly, not production signing or device execution.
3. `/home/okabe94/Android/Sdk/platform-tools/adb devices`
   — **no attached device**. Instrumented tests and interactive app walkthrough
   were **not executed**. Their absence remains explicit in future G2/G3 gates.

Reports: `app/build/test-results/testDebugUnitTest/`,
`app/build/reports/lint-results-debug.xml` and its HTML/SARIF companions.
They are generated/ignored, not committed delivery artifacts.

## Meaning of this baseline

The app compiles and sample tests pass; no ticket's business acceptance is proven.
There is no real expense/income store, receipt recovery, Sello component catalog,
sandbox, backup/restore/reset or CI yet. The first Ready tasks ratify contracts
and the toolchain; every later ticket must leave the project buildable and its
already-completed user journeys usable.
