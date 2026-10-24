# SELLO-077 — Make the sandbox scenario replay tests reliable

- **Revision:** the gate tested commit `a957cd7` on `sello-077-flaky-scenarios`, on
  `main` at `f5864bf`; the only uncommitted file was the retained report. The status
  and this section are added to that commit afterwards.
- **Requirement mapping:** cause stated under Execution progress; the fix is in
  `ScenarioRunner.recover()`, with no retry, wait or sleep; the sandbox and runner stay
  debug-only and no command, record or figure changed.
- **Red / Green:** host `aStepTakenBeforeTheNewProcessHasListedItsHistoryKeepsItsOwnResult`
  fixes the order (step first, then the relisting) and failed first with CI's message,
  `expected:<[Ya estaba g]uardado> but was:<[G]uardado>`; it passes with the fix.
- **Working checkpoint:** `ScenarioReplayTest` and `TemporalScenarioCorpusTest` (8 tests)
  ran 20 times in a row with no failure on the standard 320 × 640 API-30 AVD, and 20
  more on an API-30 AVD with a Pixel 6a profile.
- **Tests:** host `ScenarioRunnerTest` (+1).
- **Gate results:** `./scripts/verify-ticket SELLO-077 --retain` passed all seven G2
  checks, full coverage. Host: 695 tests; device: 729 tests (data 236, design-system
  133, app 339, catalog 21); zero failures, errors or skips. Lint had zero errors and
  the same 33 warnings as before. A first run on the Pixel 6a AVD failed five layout
  tests that expect a scrolling or shorter screen (see Review); it was not the
  required device and is not evidence.
- **Quality run:** run 20261023T220913Z-55181658; SELLO-077 G2 passed; HEAD a957cd7, inputs sha256 7286a313035e; report docs/tickets/E15-release-fixes/SELLO-077/quality-report.json sha256 7c05f53991cbbe69d5d6a6ac3c2c6b8ca6d3db07d8bf34811d060fd20d644008
- **Device / Artifact:** the dedicated synthetic AVD `sello077_api30` (API 30), first
  with a Pixel 6a profile, then recreated at the setup guide's default 320 × 640.
- **Review:** executor self-review; owner review by merging. Not fixed here: on a tall
  screen, `ExpenseEntryScreenTest` (two tests), `IncomeJourneyTest`,
  `EntryCatalogInteractionTest` and `ReciboScreenTest.ajustarPresupuesto…` fail because
  they assume the content scrolls; they pass on the gate's AVD.

