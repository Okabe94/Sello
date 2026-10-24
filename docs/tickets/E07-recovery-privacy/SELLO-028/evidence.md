# SELLO-028 — Define portable MVP backups and bounded validation

- **Revision:** the gate tested commit `ddd0fa2` on `sello-028-backup-format`, a
  clean tree rebased on `main` at `d2b82ea`. This evidence is added to that commit
  afterwards; status and evidence lines are outside the input fingerprint.
- **Decisions:** the owner chose, on 2026-10-22: `kotlinx-serialization-json` 1.11.0
  (the catalog's existing serialization version) with the serialization plugin in
  `:data`; amounts as bare JSON integers; UTC ISO-8601 moments, `yyyy-MM-dd` dates
  and `yyyy-MM` months; strict keys, so any change is a new version; theme, ink,
  order and direction as the only portable preferences; a backup whose preferences
  cannot be read is still made, with `"preferences": null`; the file name
  `sello-respaldo-AAAA-MM-DD-HHmm.json` in the financial zone.
- **Requirement mapping:** [format](../../../data/backup-format.md). Wire DTOs
  (`BackupWire.kt`) are separate from entities and domain classes; their spellings
  are the file's own (`WireNames`). `BackupCodec.read` checks size (a stream is read
  one byte past 10 MiB at most), then `scanStructure` before parsing (UTF-8, depth 8,
  100,000 array elements, 10,000 characters per string, duplicate keys,
  truncation), then the header, so another version is named, then the exact v1 keys,
  then `validate`: D05 text, ADR 0003 amounts (maximum + 1 rejects the file; limits
  and totals are not capped), COP only, real dates and canonical moments,
  duplicate IDs/names/sequences/entries, references and the 1,200-entry default
  history. A rejection names a path, never file contents; nothing is written.
  `RoomBackupSnapshots` reads every table in one transaction with its revision;
  `BackupExport` reads preferences afterwards and separately, encodes in a fixed
  order and reads its own output back. Receipts, undo copies, versions, generation,
  jobs, sandbox state and notice dismissals are not exported. No restore or UI
  action exists yet (SELLO-029/030).
- **Red / Green:** mutations each turned tests red: no duplicate-key check (the
  duplicate-key test), the JSON library's own number reading (`"10000"`, `1e4`,
  `010000`, a quoted version and a negative revision accepted), and no amount range
  (seven tests, including the hand-written G01 file and maximum + 1). The JSON
  library was first shown to accept quoted numbers, exponents, leading zeros and
  repeated keys, which is why numbers stay raw until validation.
- **Tests:** `BackupValidatorTest` (35) and `BackupRoundTripTest` (9) on the host,
  against the hand-written [G01 file](../../../../data/src/test/resources/backup/valid-g01.json)
  and five named invalid samples; `BackupSnapshotConsistencyTest` (5) on real Room:
  the commands' G01 history read at revision 8, export → read back equal to storage
  with no row or revision change, missing preferences, identical bytes, and 40
  concurrent saves where every snapshot's expense count matches its revision.
- **Gate results:** `./scripts/verify-ticket SELLO-028 --retain` passed all six checks.
  Host: 614 tests, results reused by Gradle from the earlier run of the identical
  sources in this session, where they ran fresh (44 new backup tests); device: 622
  tests run fresh (5 new), full coverage of data, design-system, app and catalog; zero failures, errors or skips. Lint had zero errors and
  33 warnings, the same app dependency-update and unused-colour notices as SELLO-024
  and 027; none in changed code. Earlier runs failed on an over-long test name
  (ktlint) and on the board check before its views were regenerated; both fixed.
- **Quality run:** run 20261022T185926Z-5304c2a0; SELLO-028 G2 passed; HEAD ddd0fa2, inputs sha256 ce38612f2916; report docs/tickets/E07-recovery-privacy/SELLO-028/quality-report.json sha256 624ef63c98b0843d35a879cd029e13a4f9ba84dc54704550961d609911f0fdee
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`,
  serial `emulator-5582`. All fixtures and test data are synthetic. No physical device
  was used; there is no user-facing change to show.
- **Review:** executor self-review covered hand-written expected values, the
  per-record bound at maximum and maximum + 1, the structural caps at and past their
  limits, and that reading writes nothing. Done is proposed by this ticket's pull
  request; its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No
  owner approval, merge or hosted CI pass is claimed.
- **Acceptance migrated:** 903118a78acefcf40b82f28f6500d2b081945c5afe469ee890ef53459ec69270 → e04c1a4814e1c2bcf69a06ffe6881139f16fd796dbe9dbabab9862b27df8acd3 on 2026-10-24; only link paths changed when the ticket moved into its folder.
