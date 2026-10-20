# Reproducible Sello setup

Versions and their limits: [ADR 0005](../decisions/0005-toolchain.md). Gates and the
quality runner: the [execution guide](../planning/mvp/EXECUTION_GUIDE.md#quality-runner).

## Prerequisites

- Git; Python 3 standard library for board checks; JDK **25**; Android SDK
  command-line tools and platform-tools.
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
CLI's help, not an assumed syntax. Install platform-tools, the emulator and
`system-images/android-30/google_apis/x86_64` for device work; that tooling is not
committed to the project.

## Modules, identities and tasks

| Module | Plugin / namespace | Project dependencies | Host tests | Device tests |
| --- | --- | --- | --- | --- |
| `:domain` | Kotlin/JVM, `com.software.sello.domain` | none (Kotlin stdlib only) | `:domain:test` | — |
| `:data` | Android library + KSP, `com.software.sello.data` | `:domain` | `:data:testDebugUnitTest` | `:data:connectedDebugAndroidTest` |
| `:design-system` | Android library + Compose, `com.software.sello.designsystem` | none | `:design-system:testDebugUnitTest` | `:design-system:connectedDebugAndroidTest` |
| `:app` | Android application, `com.software.sello` | `:domain`, `:design-system`, `:data` | `:app:testDebugUnitTest` | `:app:connectedDebugAndroidTest` |
| `:catalog` | Android application, `com.software.sello.catalog` | `:design-system` | `:catalog:testDebugUnitTest` | `:catalog:connectedDebugAndroidTest` |
| `:prototype` | Android application, `com.software.sello.prototype` | `:design-system` | — | — |

| Artifact | Application ID | Launcher label |
| --- | --- | --- |
| `:app` release (customer) | `com.software.sello` | Sello |
| `:app` debug | `com.software.sello.debug` | Sello Debug |
| `:catalog` debug | `com.software.sello.catalog` | Sello Catalog |
| `:prototype` debug | `com.software.sello.prototype` | Sello Prototipo |

- `:prototype` is the owner's debugging app: v1.1 sample screens with no storage. It
  is never shipped and has no tests; the gate's ktlint and lint still cover it. See its
  [README](../../prototype/README.md).
- `:app`'s Gradle edge to `:data` exists for the composition root only. Gradle
  cannot enforce that package rule; `scripts/quality/architecture.py` does.
- `:data` applies KSP with Room and exports schemas to `data/schemas/`.
- The debug label comes from `app/src/debug/res/values/strings.xml`. The class name
  stays `com.software.sello.MainActivity`; only the application ID gains `.debug`.
- Dependencies are locked per module. After a deliberate dependency change, run
  `./scripts/update-dependency-locks` and review the lockfile diff
  ([ADR 0006](../decisions/0006-quality-tooling.md)).

Inspect the graph and built identities:

```bash
./gradlew projects
./gradlew :app:dependencies --configuration releaseRuntimeClasspath | grep 'project :'
./gradlew :catalog:dependencies --configuration debugRuntimeClasspath | grep 'project :'
./gradlew :domain:dependencies --configuration runtimeClasspath
"$ANDROID_HOME/build-tools/36.0.0/aapt2" dump badging app/build/outputs/apk/debug/app-debug.apk
```

## Build and focused inspection

The host gate runs every module's tests, ktlint, lint and the assemblies; its exact
task list is in the [board README](../planning/mvp/README.md):

```bash
./scripts/verify-ticket SELLO-0XX             # the ticket's own gate
./scripts/verify-ticket SELLO-0XX --gate G1   # the host gate alone
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
do not upgrade or baseline them incidentally.

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

## Gradle caching in CI

The Quality workflow uses commit-pinned `gradle/actions/setup-gradle` v5.0.2
to restore downloaded dependencies, wrapper distributions, compiled build scripts
and Gradle's local task-output cache. Only trusted jobs on `main` save entries;
pull requests and other refs use them read-only. Keep `actions/setup-java` free
of a second Gradle cache configuration so the two actions cannot compete.

The job has one `cache-profile` matrix entry per run: `host` for pushes to `main`,
`device` for PRs, nightly and manual runs. This is a cache preference; the runner
still decides the actual G1/G2 gate, including G1 for document-only PRs. The fixed
job name explicitly references matrix context and resolves to `quality`, so GitHub
does not append a profile suffix; the matrix does not add a second runner.
`setup-gradle` includes matrix context in its immutable cache key, so a host push
cannot occupy the key needed to save a later nightly device build at the same SHA.
Normal cross-profile restore fallback remains enabled for cold profiles and the
transition from the earlier unprofiled cache.

PRs prefer the latest device-profile cache on `main`. That entry may be older than
the latest host-profile cache; Gradle still validates task inputs and rebuilds
affected outputs. Once a profile has saved a key for a SHA, another run of the
same profile/SHA cannot update it. Separating profiles fixes the host/device
collision; it does not make GitHub's cache entries mutable.

`org.gradle.caching=true` enables task-output reuse locally and in CI. Gradle
still selects the full gate task graph and checks each cacheable task's inputs.
Data unit tests also track the complete schema directory and Kotlin storage sources
that `ExportedSchemaTest` reads directly. Changes to those files invalidate
its test-task result even when compiled classes are unchanged, including unexpected
schema filenames that the retention test must reject.

Configuration caching remains enabled within a job. Its project state is not
uploaded: no `cache-encryption-key` secret is configured. No application data,
emulator snapshot, signing material or project output directory is added to the
cache paths. Build Scan publication, dependency submission and PR comments stay off.

Use the action's job summary for cache restore/save information and task outcomes,
and the retained quality logs/XML for gate evidence. Distinguish executed tests
from `FROM-CACHE` and `UP-TO-DATE` task results; cached host results describe prior
execution for matching Gradle inputs. Device results must still be fresh for each
G2 run. To investigate cache behavior, run a focused task with `--info`; use
`--no-build-cache --rerun-tasks` when fresh execution is needed.

The options, baseline and verification live in the
[CI performance plan](ci-performance-plan.md). Hosted speedup is measured after
cache restore/save is observed on GitHub, not inferred from a local cache hit.

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
./scripts/verify-ticket SELLO-0XX --gate G2   # the device gate
```

Wait for boot completion `1` with a bounded condition check; device connectivity
alone is not readiness. Confirm API `30`, that the debug app opens on Recibo and that
the catalog opens, not just successful installs. The unsigned release APK cannot be
installed, so customer/debug coexistence on a device needs the owner's signed build.
Shut down only the emulator you created: `adb -s "$ANDROID_SERIAL" emu kill`.

## Troubleshooting and signing

| Symptom | Meaning / action |
| --- | --- |
| `JAVA_HOME is set to an invalid directory` | Launcher cannot start. Point it at an installed JDK 25; daemon settings cannot repair an invalid launcher path. |
| `SDK location not found` | Set a valid ANDROID_HOME or ignored sdk.dir; install required packages. |
| Different daemon JDK than expected | Inspect daemon criteria and buildEnvironment; don't assume changing JAVA_HOME overrides them. |
| Missing SDK license/platform or repository access | Resolve actual install/license/network failure; don't bypass checks or silently change SDK/versions. |
| KSP adds forbidden kotlin.sourceSets | Use the pinned KSP2 version. Don't disable built-in Kotlin validation. |
| Child Kotlin JVM plugin has an unknown classpath version | Declare the catalog's versioned JVM plugin at root with apply false before AGP/Compose, then apply it in the child. |
| No device or boot not completed | APK assembly is not launch/instrumentation evidence; provide an isolated emulator or record the missing gate. |

Debug/test builds use a local development keystore outside source. `assembleRelease`
needs **no distribution key** and currently yields
`app/build/outputs/apk/release/app-release-unsigned.apk`. An unsigned/debug-signed
artifact is not a production distribution build. The owner handles release builds,
signing and physical-device tests; do not request passwords/keys or create release
keys during setup. Missing signing secrets must not break ordinary local gates.
