# SELLO-035 — Validate performance and optimized release composition

- **Revision:** the gate tested commit `bc0767f` on `sello-035-signing`, on `main` at
  `ea65c33`; the only uncommitted file was the retained report. The status and this
  section are added to that commit afterwards. Parts 1 and 2 merged as #89 and #90.
- **Decisions:** the owner approved, on 2026-10-23: `adb` measurements with no new
  library, the API-30 emulator as reference, the targets in the
  [performance profile](../../../testing/performance-profile.md) before any result; the
  owner as device-testing owner and signing custodian; signing from their existing
  `sello.signing.*` properties; ADR 0016 (option 1, key off CI); one network step for
  advisories and published licences. No independent technical reviewer is assigned:
  D10's reviewer remains SELLO-036's blocker, not this ticket's.
- **Requirement mapping:** large history: 20 categories, 10,000 expenses, 2,000 incomes
  and 12 past months, seeded and restored through the app's own validation; every
  target met on the shrunk release (cold start to figures 314 ms of 1.5 s, warm 68 ms,
  Recibo 0% slow frames, Resumen 402 ms of 700, backup 1.25 s of 5, restore 2.22 s of
  10), device, build and dataset stated. Reads stay off the main thread
  (`MainThreadWorkTest`); Resumen reads only the 12 months it charts; money stays exact.
  R8 with no keep rule of Sello's; the shrunk build saved, edited, deleted with undo,
  recorded income, created a category, kept a theme and a draft across restart and
  process death, showed a past month, backed up, restored and erased, with the
  developer-tools check clean. The release APK is signed by the owner's key, its digest
  pinned; G3 checks it, keeps R8's mapping beside it and installs and opens it; 143
  libraries reviewed (142 Apache-2.0, 1 BSD-3-Clause), no advisory. The signed APK
  restored and backed up the large history by hand.
- **Red / Green:** `theAppIsReportedFullyDrawnOnceRecibosFiguresAreReadAndNotWhileLoading`
  failed first (never reported) and passed with `ReportDrawnWhen`. `MainThreadWorkTest`
  failed on a disk read planted on the main thread, then passed without it. The G3 tests
  (`test_gates`, `test_selection`, `test_evidence`, `test_distributed`) failed against
  the old refusal before G3 was implemented.
- **Tests:** device `ReciboScreenTest` (+1), `MainThreadWorkTest` (1); Python
  `test_dataset` (9), `test_measure` (9), `test_release` (13), `test_licenses` (6), and
  G3 cases in `test_gates` (3 new, replacing the refusal test), `test_selection` (+1),
  `test_evidence` (+2), `test_distributed` (+2), `test_workflow` and `test_runner`
  (changed).
- **Gate results:** `./scripts/verify-ticket SELLO-035 --retain` passed all eleven G3
  checks, full coverage. Host: 694 tests; device: 728 tests (data 235, design-system
  133, app 339, catalog 21); zero failures, errors or skips. Lint had zero errors and
  the same 33 warnings as before. Release: `sello-1.0-1.apk`, 4,458,299 bytes, sha256
  `ef2ca71d468b342114aed23904c9f279750e8a9a3888227c78fa8579d1b640ee`, APK Signature
  Scheme v2, certificate sha256
  `abf6ccf5b58de43626daea387d5c18c67981e42c302f8a1c7e16ad5cf0b4d952`, mapping sha256
  `1543e4a0d88c350965803f98ef0c00f0280055b7ac41f9006073d70a45619f58`; installed and
  opened on `emulator-5582`. An earlier run failed only ktlint on `app/build.gradle.kts`,
  with every other check passing; it was formatted and the gate run again. The first
  hosted G3 failed `release-ephemeral`: apksigner 37.0.0 on CI names the signer
  "V3.0 Signer:", a form the parser did not read; it now reads all three forms (tests
  from CI's exact output), and the gate was run again.
- **Quality run:** run 20261023T185147Z-a1f077c8; SELLO-035 G3 passed; HEAD bc0767f, inputs sha256 22a6c9eaeb16; report docs/tickets/E08-release/SELLO-035/quality-report.json sha256 7c45b5d0ba4792059c8a424d67d42f358e996e85b2d8adc9e42bd30b5f9a4dd9
- **Device / Artifact:** the gate on the API-30 AVD `ci30` (`emulator-5582`);
  measurements and walks on the isolated API-30 phone emulator `sello022_api30`
  (`emulator-5584`). No physical device or personal data was used; synthetic data only.
- **Review:** executor self-review covered the targets against the results, the money
  path on the shrunk build, the key never leaving the owner's machine and CI never
  completing G3. Found and left: the restore preview and message write "10000" without
  a thousands separator; Resumen shows "2026-10-23" in storage form; the app shows no
  third-party licence notice (an owner decision). Done is proposed by this pull request;
  its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No owner
  approval, merge or hosted CI pass is claimed.

