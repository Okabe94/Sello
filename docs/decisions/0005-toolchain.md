# 0005 — Supported bootstrap toolchain

**Ticket:** SELLO-002 · **Date:** 2026-10-08
**Status:** Selected and execution-verified for the scaffold/bootstrap
**Decision owner:** technical executor; approved product floor/identities are D02.

## Decision

Retain AGP/Gradle/JDK/SDK/BOM pins; align Kotlin and its Compose compiler to 2.4.20
for the future JVM module's Gradle compatibility. Do not upgrade unrelated AndroidX libraries.
The catalog/wrapper own versions; this table records the verified decision.

| Boundary | Selected | Evidence / constraint |
| --- | --- | --- |
| Android Gradle plugin | 9.3.3 | Official 9.3 compatibility: Gradle 9.5.0, build tools 36.0.0, JDK minimum 17, maximum API 37 |
| Gradle wrapper | 9.5.0 | Official distribution SHA-256 matches wrapper; fresh Gradle user home downloaded/built successfully |
| Gradle daemon | JDK 25; tested JetBrains 25.0.2 | Gradle 9.5 supports Java 25; daemon criteria, not JAVA_HOME alone, select the daemon |
| App Kotlin / Compose compiler | 2.4.20 / 2.4.20 | Paired catalog pin overrides AGP's default KGP 2.2.10; actual compiler/plugin reports and complete final build verify the selected version |
| Future JVM module plugin | 2.4.20 | Published Gradle range includes 9.5; separate temporary JVM module compiled; declare the plugin at the root before applying it in children |
| Java/Kotlin output | JVM 11 | Android compileOptions and fresh app/JVM-probe class major version 55; no JDK-25 bytecode requirement on phones |
| Compose BOM | 2026.02.01 | Retained; resolves UI 1.10.4; actual scaffold launch/instrumentation passed on API 30 |
| Compile / target / minimum API | 37 / 37 / 30 | Installed platform `platforms/android-37.0`; manifest/lint and API-30 runtime smoke confirm the existing floor |
| SDK build tools | 36.0.0 | AGP default inspected at execution; installed prerequisites documented |
| KSP | 2.3.12, KSP2 | Separate Room 2.8.4 generation/schema/export/compile/lint probe passed with built-in Kotlin |

KGP 2.2.10's published fully supported Gradle range ends at **8.14**. Its successful
initial JVM probe did not remove that compatibility gap. KGP 2.4.20 covers Gradle
through 9.7 and AGP through 9.3.1 in its published tested range; retained AGP 9.3.3
is a later patch in that minor, independently execution-tested here, not claimed
as an exact version certified by Kotlin's table. This bounded patch difference is
preferable to retaining the older KGP's major Gradle gap or downgrading AGP fixes.
The coordinated Kotlin/Compose change is the only existing dependency upgrade.

Gradle's `--version` also reports embedded Kotlin **2.3.20**. That is the language
used by Gradle's Kotlin DSL, not Sello's app compiler. JVM 11 is an output target,
not an instruction to run Gradle on JDK 11.

## Alternatives and integration rules

- **MUST** retain AGP built-in Kotlin for Android modules. Do not add
  `org.jetbrains.kotlin.android`, disable built-in Kotlin or suppress its source-set
  validation to make an obsolete processor plugin work.
- **MUST** use the catalog's Compose/JVM/KSP plugin aliases. SELLO-003 declares
  versioned plugins once at the root (`apply false`) before child application;
  declare the JVM plugin before AGP/Compose classpath loading. A temporary child-only
  version request failed because AGP had already loaded KGP with an unknown version.
- **MUST** use KSP2. The old candidate `2.2.10-2.0.2` failed with built-in Kotlin's
  prohibition on `kotlin.sourceSets` additions. No validation bypass was added.
  KSP's 2.3.1 release introduced built-in Kotlin support; 2.3.12 includes subsequent
  integration/processor fixes and requires AGP at least 8.12, satisfied here.
  This is a new, tested KSP pin, not permission for blanket dependency upgrades.
- **MUST** keep financial schemas/Room wiring in their owning tickets. Room 2.8.4
  was used only as a compatibility-probe processor; no production Room dependency,
  schema, database or new module ships in SELLO-002. Pin adopted Room artifacts
  centrally when introduced, and rerun generation/real-Room acceptance then.
- **SHOULD** retain JDK-25 daemon criteria because they already work on this host;
  changing daemon major/vendor requires fresh compatibility evidence. Another host
  needs a suitable installed JDK 25 or working configured toolchain provisioning.
- **MUST** preserve API 30 and approved identities. Today only customer namespace/
  application ID `com.software.sello` exists. `.debug`/`.catalog` and launcher labels
  remain SELLO-003 deliverables; coexistence was not tested or implemented here.

Release assembly produces an **unsigned** APK without distribution secrets. Debug
and instrumentation use development signing only. The owner controls release
build/signing/key custody; no distribution key, password or signing configuration
is introduced. Private APK installation/recovery/signing acceptance remains later.

## Verification and limits

See [reproducible setup](../development/setup.md) and
[execution evidence](../testing/SELLO-002-toolchain-verification.md).
An isolated source export and initially empty Gradle user home passed G0 plus
instrumented APK assembly; missing-JDK/SDK failures were exercised. A separately
created API-30 emulator launched the greeting and passed the one existing context
test. This is scaffold evidence, not financial/UI completion, an exhaustive API
matrix, physical-device acceptance, or independent human technical review.

## Primary sources consulted

Consulted on 2026-10-08; exact artifact metadata and execution were checked in
addition to guides. Do not substitute a future “latest” release for these pins.

- [AGP 9.3 compatibility and patch notes](https://developer.android.com/build/releases/agp-9-3-0-release-notes)
- [AGP 9.3.3 published POM](https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/9.3.3/gradle-9.3.3.pom)
- [Gradle 9.5 compatibility](https://docs.gradle.org/9.5.0/userguide/compatibility.html)
- [Gradle 9.5 daemon criteria](https://docs.gradle.org/9.5.0/userguide/gradle_daemon.html#sec:daemon_jvm_criteria)
- [Official wrapper checksum](https://services.gradle.org/distributions/gradle-9.5.0-bin.zip.sha256)
- [Android built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [Compose compiler integration](https://developer.android.com/develop/ui/compose/compiler)
- [Kotlin Gradle plugin compatibility](https://kotlinlang.org/docs/gradle-configure-project.html)
- [KSP 2.3.1 built-in Kotlin support](https://github.com/google/ksp/releases/tag/2.3.1)
- [KSP 2.3.12 release](https://github.com/google/ksp/releases/tag/2.3.12) and [versioned README](https://github.com/google/ksp/blob/2.3.12/README.md)
- [Room release requirements](https://developer.android.com/jetpack/androidx/releases/room)
