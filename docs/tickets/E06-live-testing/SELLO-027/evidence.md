# SELLO-027 — Establish the growing temporal regression scenario suite

- **Revision:** tested snapshot is the `sello-027-temporal-corpus` working tree on `main`
  at `def067f`, uncommitted; it is identified exactly by the retained input fingerprint
  below. The commit and pull request follow this evidence; no claim is made that the
  gate tested a clean commit. With this ticket the E06 epic is proposed Done.
- **Decisions:** the owner chose, on 2026-10-22, that a finished live run compares its
  expected figures with the real snapshots and shows each one, and that the leap-year
  scenario is included with hand-derived figures labelled as not approved.
- **Requirement mapping:** `docs/testing/sandbox-scenarios.md` lists nine scenarios
  (ID, version, seed, steps, start and zone, expected figures with sources, replay from
  fresh and from a checkpoint, owning test), the extension process and the recurrence
  obligation (production run-due/catch-up use case with created/skipped/failed/pending
  results and separate WorkManager tests; no scheduling dependency added). New steps
  `SetDefaultLimit`, `SetMonthLimit`, `Archive`, backdated `Expense` and `Verify`
  (reads the real `MonthlySnapshots`/`ResumenSnapshots`). New scenarios: leap year
  (by hand), skipped months (H03/H04), past limits (H05, H01, H02), archived category
  (AR01/AR02) and income allocation (I01/I02); G01 and month/year ends gained checks
  (versions 2). Restart and monotonic undo under time stepping stay covered by
  `TimeControlsJourneyTest`.
- **Red / Green:** `TemporalScenarioCorpusTest` passed all 37 figures across nine
  scenarios, fresh and resumed, with identical receipts, expenses, incomes, categories
  and limits; setting I02's expected unassigned value to 0 (a clamped result) turned
  it red with `✗ sin asignar 2026-11: esperado 0, real -50.000 (I02)`. Host tests check
  every scenario is in the corpus document, ends with a sourced check and scripts the
  right category versions.
- **Gate results:** `./scripts/verify-ticket SELLO-027 --retain` passed all six checks.
  Host: 570 tests (3 new); device: 617 tests (1 new); zero failures, errors or skips.
  Lint had zero errors and the same 33 warnings as before, none in changed code; the
  release check still finds no developer tools in the release APK.
- **Quality run:** run 20261022T142158Z-e862852d; SELLO-027 G2 passed; HEAD def067f, inputs sha256 d30317bed5f7; report docs/tickets/E06-live-testing/SELLO-027/quality-report.json sha256 71cbce24fa3461093ca295d7c3331ecc26bbdf644e09e9f7d6aceee9be63e3f9
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp), serial `emulator-5582`. Every check line of the corpus run is in
  [corpus-run.txt](artifacts/corpus-run.txt); a live G01 run in
  the tools showed "4 de 4 cifras coinciden"
  ([screenshot](artifacts/g01-live.png)). No physical device or
  personal data was used; no hosted CI run is claimed.
- **Review:** executor self-review covered independent expected values, fresh-versus-
  resumed equality, the recurrence boundary and release exclusion. Done is proposed by
  this ticket's pull request, together with the E06 epic; its merge is the designated
  owner acceptance under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass
  is claimed.
- **Acceptance migrated:** 4c8ec04e6b95952db6a2fbd461472a6948986d31f9d7ab4e28bf250dd58cfaf4 → e1dd66061fbb472ab12f9df622662ba6d657f40eea5bc0a2af435b597abf717a on 2026-10-24; only link paths changed when the ticket moved into its folder.
