# SELLO-032 — Verify privacy, file boundaries and developer-data isolation

- **Revision:** the gate tested commit `fc83fef` on `sello-032-privacy`, on `main` at
  `7924d95`; the only uncommitted file was the retained report. The Quality run line
  below is added to that commit afterwards.
- **Decisions:** the owner approved, on 2026-10-22: the picker adapters accept only
  provider (`content://`) documents; AndroidX's profile-install receiver stays,
  documented (it requires DUMP); the privacy disclosure is the developer document,
  with the in-app Respaldo and Borrar datos wording unchanged; one pull request. This
  is the last E07 ticket, so its pull request also proposes E07 Done.
- **Requirement mapping:** merged-manifest checks for release and debug
  (`verifyReleaseManifestPrivacy`, `verifyDebugManifestPrivacy` in
  `gradle/privacy.gradle.kts`): `allowBackup="false"`, every storage domain excluded
  from cloud backup, device transfer and Android 11 full backup, only the AndroidX
  signature permission, no exported or URI-granting provider, every exported
  component with a written reason; `verifyReleaseDependencies` refuses test, tooling
  and test-manifest libraries on the release runtime; `verifyReleaseHasNoDeveloperTools`
  now also looks for the debug composition, sandbox storage names and debug previews.
  Gap found and fixed: `AndroidDocumentReader`/`AndroidDocumentWriter` accepted any
  address, so a `file://` path could read or overwrite a private file; `providerDocument`
  now admits only provider documents. No FileProvider, cache use or app logging
  exists; the kept unreadable copy is excluded from backup, never shared and erased
  by reset. [MVP data handling](../../../privacy/mvp-data-handling.md) states what is
  stored, what may leave and the checks; architecture §7 now names them.
- **Red / Green:** `FileBoundaryTest` failed first on the real gap (a `file://`
  address read and overwrote a private canary) and passed after the fix. Mutations each
  failed the matching check with the matching reason: INTERNET permission, a device-
  transfer domain not excluded, an exported FileProvider, JUnit on the release
  classpath (with the strict dependency lock relaxed only for that run, then restored)
  and the reader writing file contents to the log (`LogPrivacyTest`). The six new
  developer-tool markers were each found in the debug APK and none in release.
- **Tests:** device `FileBoundaryTest` (3), `ProfileFileBoundaryTest` (2),
  `LogPrivacyTest` (1, a probe line proves the log is read); build checks above on
  every assemble.
- **Gate results:** `./scripts/verify-ticket SELLO-032 --retain` passed all six checks,
  full coverage. Host: 674 tests; device: 687 tests (data 228, design-system 122, app
  316, catalog 21); zero failures, errors or skips. Lint had zero errors and the same
  33 warnings as before.
- **Quality run:** run 20261023T032312Z-0ebf80cd; SELLO-032 G2 passed; HEAD fc83fef, inputs sha256 44b104955e59; report docs/tickets/E07-recovery-privacy/SELLO-032/quality-report.json sha256 5d5f21f5e63f249b8888aac19da96cde7f96557288ee058760a007044c0a2e29
- **Device / Artifact:** the isolated synthetic API-30 AVD `ci30` (`emulator-5582`).
  Retained inventories: merged release and debug manifests, release libraries and the
  release APK developer-tools check ([evidence](artifacts)).
  Real backup, restore and reset journeys ran under these minimal permissions in
  `LogPrivacyTest`, `RestoreJourneyTest` and `ResetReuseJourneyTest`. No physical
  device or personal data was used.
- **Review:** executor self-review covered the merged manifests, the release
  inventory, profile and document boundaries and the log check. Done for SELLO-032 and
  E07 is proposed by this pull request; its merge is the designated owner acceptance
  under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is claimed.
