# Reproducible Sello setup

**Verified:** 2026-10-08 · **Owner:** SELLO-002
Version decisions and limits: [ADR 0005](../decisions/0005-toolchain.md).
Modules and identities below were bootstrapped by SELLO-003; the app is still the
Compose greeting and the catalog an empty shell, with no financial implementation.

## Prerequisites

- Git; Python 3 standard library for board checks; JDK **25** (tested JetBrains
  25.0.2); Android SDK command-line tools and platform-tools.
- SDK platform **37.0** / API 37 and build tools **36.0.0**. API 30 is the minimum
  supported app runtime, not the SDK platform used to compile it.
- For device checks: emulator plus an API-30 system image or an explicitly selected
  device. Linux emulator verification also needs working KVM/virtualization.
- First provisioning needs Google Maven, Maven Central, Gradle Plugin Portal and
  Gradle distribution access; JDK auto-provisioning may need Foojay. A first build
  is not promised to work offline. Review SDK/JDK terms rather than bypassing them.

Do not install a global Gradle or use an IDE's different wrapper/compiler version.
Use `./gradlew` from the repository root. Android Studio is optional for CLI builds.

## Environment and SDK

On the verified host:

```bash
export ANDROID_HOME=/home/okabe94/Android/Sdk
export JAVA_HOME=/opt/android-studio/jbr
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
./gradlew --version
./gradlew :app:buildEnvironment --no-configuration-cache
```

On another host, replace **both** paths with actual installed SDK/JDK locations.
`JAVA_HOME` selects the wrapper launcher; `gradle/gradle-daemon-jvm.properties`
requires a JDK-25 daemon and overrides JAVA_HOME/`org.gradle.java.home` for that
choice. Confirm the actual daemon using `:app:buildEnvironment`, not assumptions
about the shell or IDE. Foojay configuration can provision missing toolchains when
network/platform support exists; an installed JDK 25 is the simplest tested path.

An SDK location can alternatively be set with ignored root `local.properties`:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

Never commit that file, host paths in build logic, `.gradle`/build caches or signing
material. Do not copy another developer's local.properties into a clean checkout.
Resolve conflicting SDK environment values rather than leaving stale paths.

The tested command-line-tools package is 23.0.0 (installed under `latest`). Its
`android sdk` CLI uses slash-separated package names:

```bash
ANDROID_CLI="$ANDROID_HOME/cmdline-tools/latest/bin/android"
"$ANDROID_CLI" --sdk="$ANDROID_HOME" sdk install platforms/android-37.0
"$ANDROID_CLI" --sdk="$ANDROID_HOME" sdk install build-tools/36.0.0
"$ANDROID_CLI" --sdk="$ANDROID_HOME" sdk list
```

Older SDK managers use `sdkmanager --sdk_root="$ANDROID_HOME" "platforms;android-37.0" "build-tools;36.0.0"`.
The installed sdkmanager wrapper reports deprecation; use the actual installed
CLI's help, not an assumed syntax. Install platform-tools/emulator for device work;
that tooling need not be committed to the project. The new image installed for
this verification was `system-images/android-30/google_apis/x86_64`, revision 16.

## Modules, identities and tasks

| Module | Plugin / namespace | Project dependencies | Host tests | Device tests |
| --- | --- | --- | --- | --- |
| `:domain` | Kotlin/JVM, `com.software.sello.domain` | none (Kotlin stdlib only) | `:domain:test` | — |
| `:data` | Android library + KSP, `com.software.sello.data` | `:domain` | `:data:testDebugUnitTest` | `:data:connectedDebugAndroidTest` |
| `:design-system` | Android library + Compose, `com.software.sello.designsystem` | none | `:design-system:testDebugUnitTest` | `:design-system:connectedDebugAndroidTest` |
| `:app` | Android application, `com.software.sello` | `:domain`, `:design-system`, `:data` | `:app:testDebugUnitTest` | `:app:connectedDebugAndroidTest` |
| `:catalog` | Android application, `com.software.sello.catalog` | `:design-system` | `:catalog:testDebugUnitTest` | `:catalog:connectedDebugAndroidTest` |

| Artifact | Application ID | Launcher label |
| --- | --- | --- |
| `:app` release (customer) | `com.software.sello` | Sello |
| `:app` debug | `com.software.sello.debug` | Sello Debug |
| `:catalog` debug | `com.software.sello.catalog` | Sello Catalog |

- Only `:app` has tests today: one sample JVM test and one debug identity test in
  `app/src/androidTestDebug`; `:catalog` has one identity test in `androidTest`.
  `:domain`, `:data` and `:design-system` report `NO-SOURCE` for their test tasks.
  That is a configured task, not an executed suite.
- `:app`'s Gradle edge to `:data` exists for the composition root only. Gradle
  cannot enforce that package rule; `scripts/quality/architecture.py` does.
- `:data` applies KSP with Room 2.8.4 and `room.schemaLocation` set to
  `data/schemas/`. No database exists yet, so that directory is created by the
  first real schema in SELLO-011. Do not add a placeholder database to fill it.
- The debug label comes from `app/src/debug/res/values/strings.xml`. The class name
  stays `com.software.sello.MainActivity`; only the application ID gains `.debug`.

Inspect the graph and built identities:

```bash
./gradlew projects
./gradlew :app:dependencies --configuration releaseRuntimeClasspath | grep 'project :'
./gradlew :catalog:dependencies --configuration debugRuntimeClasspath | grep 'project :'
./gradlew :domain:dependencies --configuration runtimeClasspath
"$ANDROID_HOME/build-tools/36.0.0/aapt2" dump badging app/build/outputs/apk/debug/app-debug.apk
```

## Build and focused inspection

```bash
./scripts/verify-ticket SELLO-0XX   # the ticket's whole gate; see the execution guide
./gradlew ktlintCheck :domain:test :data:testDebugUnitTest :design-system:testDebugUnitTest \
  :app:testDebugUnitTest :catalog:testDebugUnitTest lintDebug \
  :app:assembleDebug :app:assembleRelease :catalog:assembleDebug --continue
./gradlew :app:assembleDebugAndroidTest :catalog:assembleDebugAndroidTest \
  :data:assembleDebugAndroidTest :design-system:assembleDebugAndroidTest
python3 -m unittest discover -s docs/planning/mvp -p test_board.py
python3 docs/planning/mvp/board.py --check
```

Inspect actual compiler versions independently of Gradle's embedded Kotlin:

```bash
./gradlew :app:dependencies --configuration kotlinCompilerClasspath
./gradlew :app:dependencies --configuration kotlinCompilerPluginClasspathDebug
./gradlew :app:dependencies --configuration debugRuntimeClasspath
```

`:domain` uses the same Kotlin plugin pin through the root `apply false` declaration.

Expected app compiler and Compose compiler plugin: **2.4.20**. `--version` reports
embedded Kotlin **2.3.20**, which is not the app compiler. Java/Kotlin output remains
JVM 11. Build warning counts and resolved artifacts belong in ticket evidence;
do not upgrade or baseline them incidentally to this setup task.

For independent-output validation in a clean source checkout:

```bash
export GRADLE_USER_HOME="$(mktemp -d)"
./gradlew clean :domain:test testDebugUnitTest lintDebug assembleDebug \
  :app:assembleRelease assembleDebugAndroidTest --no-build-cache \
  --no-configuration-cache --rerun-tasks --continue
```

An empty Gradle user home forces wrapper/dependency provisioning, not just clean
outputs; this can consume substantial disk/network. It still uses the installed
SDK/JDK and accepted SDK licenses. Do not delete the user's existing caches or
use `git clean/reset` to manufacture a clean checkout. A separate source export
via `git archive HEAD` is safe but excludes uncommitted changes; test those
explicitly before claiming the latest snapshot was verified.

## Device smoke

Use a dedicated synthetic AVD; do not wipe/reset an owner's device or reuse their
private data. Example isolated setup for an already installed API-30 image:

```bash
export ANDROID_AVD_HOME="$(mktemp -d)"
printf 'no\n' | "$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager" create avd \
  --name sello_api30 --package 'system-images;android-30;google_apis;x86_64' \
  --path "$ANDROID_AVD_HOME/sello_api30.avd"
"$ANDROID_HOME/emulator/emulator" -avd sello_api30 -port 5580 \
  -no-window -no-audio -no-snapshot -gpu swiftshader -no-boot-anim
```

Run the emulator in its own terminal; first confirm that port/device slot is free.
In another terminal with the same SDK/JDK environment:

```bash
export ANDROID_SERIAL=emulator-5580
timeout 90 adb -s "$ANDROID_SERIAL" wait-for-device
adb -s "$ANDROID_SERIAL" shell getprop sys.boot_completed
adb -s "$ANDROID_SERIAL" shell getprop ro.build.version.sdk
adb -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$ANDROID_SERIAL" install -r catalog/build/outputs/apk/debug/catalog-debug.apk
adb -s "$ANDROID_SERIAL" shell am start -W \
  -n com.software.sello.debug/com.software.sello.MainActivity
adb -s "$ANDROID_SERIAL" shell am start -W -n com.software.sello.catalog/.CatalogActivity
./gradlew :app:connectedDebugAndroidTest :catalog:connectedDebugAndroidTest
```

Wait for boot completion `1` with a bounded condition check; device connectivity
alone is not readiness. Confirm API `30`, visible `Hello Android!` in the debug app
and `Sello Catalog` in the catalog, not just successful installs. The instrumented
tests check package identity and launcher label only. The unsigned release APK
cannot be installed, so customer/debug coexistence on a device needs the owner's
signed build. Shut down only the emulator you created:
`adb -s "$ANDROID_SERIAL" emu kill`.

## Troubleshooting and signing

| Symptom | Meaning / action |
| --- | --- |
| `JAVA_HOME is set to an invalid directory` | Launcher cannot start. Point it at an installed JDK 25; daemon settings cannot repair an invalid launcher path. This failure was exercised. |
| `SDK location not found` | Set a valid ANDROID_HOME or ignored sdk.dir; install required packages. Tested without local.properties and with an invalid SDK path. |
| Different daemon JDK than expected | Inspect daemon criteria and buildEnvironment; don't assume changing JAVA_HOME overrides them. |
| Missing SDK license/platform or repository access | Resolve actual install/license/network failure; don't bypass checks or silently change SDK/versions. |
| KSP adds forbidden kotlin.sourceSets | Use pinned KSP 2.3.12/KSP2, not the rejected older candidate. Don't disable built-in Kotlin validation. |
| Child Kotlin JVM plugin has an unknown classpath version | Declare the catalog's versioned JVM plugin at root with apply false before AGP/Compose, then apply it in the child. |
| No device or boot not completed | APK assembly is not launch/instrumentation evidence; provide an isolated emulator or record the missing gate. |

Debug/test builds use a local development keystore outside source. `assembleRelease`
needs **no distribution key** and currently yields
`app/build/outputs/apk/release/app-release-unsigned.apk`. An unsigned/debug-signed
artifact is not a production distribution build. The owner handles release builds,
signing and physical-device tests; do not request passwords/keys or create release
keys during setup. Missing signing secrets must not break ordinary local gates.
