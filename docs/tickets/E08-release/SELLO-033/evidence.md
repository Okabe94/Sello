# SELLO-033 — Execute accessibility, adaptive and process-restoration journeys

- **Revision:** the final gate tested commit `8a50ce9` on `sello-033-walks`, on `main`
  at `f67ae77` (part 1, pull request #86); the only uncommitted file was the retained
  report. The Quality run line below is added to that commit afterwards.
- **Decisions:** the owner moved manual TalkBack, Switch Access and keyboard walks to
  post-MVP SELLO-074 on 2026-10-23 (pull request #85), and chose the real app shell
  for the sweep, WCAG AA for contrast and small fixes in the same part.
- **Requirement mapping:** [UI acceptance matrix](../../../testing/ui-acceptance-matrix.md):
  four schemes, scales 1.0/1.3/2.0, compact, short-landscape, medium and expanded
  windows in `UiAcceptanceSweepTest` and real-window walks; insets (system bars,
  keyboard, a side navigation bar); reduced motion; exact spoken amounts; process
  death, rotation and cold start reported separately, each confirmed action one
  receipt. Tab, month, detail and panel Back keep their existing tests; no manual
  assistive-technology walk is claimed.
- **Red / Green:** each fix failed first: `LargeTextFitTest` (titles, month title,
  rail, chips, a title moved under its action), `CategoryEditorViewModelTest` and
  `CategoryManagementViewModelTest` (suggestions read "200.000"), `ChartExactSelectionTest`
  (no spoken limit line), `ComponentContractsTest.aNavigationBarAtTheSideCoversNeitherCancelNorDone`
  (Listo under a 48dp side inset). The sweep's first runs had two faulty checks of its
  own, a width-overflow flag true for whole text and a clipped-node size without
  Compose's touch widening; both were corrected and recorded in the matrix.
- **Tests:** device `UiAcceptanceSweepTest` (5 windows), `LargeTextFitTest` (6),
  `ComponentContractsTest` (+1); assertions added to `ChartExactSelectionTest`,
  `CategoryEditorScreenTest`, `ResumenScreenTest`, the two category view model tests
  and `SelloColorsTest`.
- **Gate results:** `./scripts/verify-ticket SELLO-033 --retain` passed all six checks,
  full coverage. Host: 674 tests; device: 703 tests (data 229, design-system 129, app
  324, catalog 21); zero failures, errors or skips. Lint had zero errors and the same
  33 warnings as before. Part 1 passed its own gate (run 20261023T065031Z-dbb34ab5).
  Its hosted check failed once in `ScenarioReplayTest.aRunInterruptedAfterACommitAndResumedMatchesAFreshRunExactly`,
  unrelated to this ticket, and passed when the workflow was run again; reported, not
  changed here.
- **Quality run:** run 20261023T074006Z-cfef9451; SELLO-033 G2 passed; HEAD 8a50ce9, inputs sha256 ff410882368c; report docs/tickets/E08-release/SELLO-033/quality-report.json sha256 f3c4887e236a3a15243391c3e9378df8ce9f9ca55dd884d674457c53a752024d
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (`emulator-5582`); the walks on the isolated API-30 AVD `sello022_api30`
  (`emulator-5584`), wiped first, with debug builds of the code under test;
  screenshots in [evidence](artifacts). No physical device or
  personal data was used.
- **Review:** executor self-review covered each check's false positives, the test
  windows against real ones and every walk's database result. Done is proposed by this
  ticket's pull request; its merge is the designated owner acceptance under
  `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is claimed.
- **Acceptance migrated:** 5209b4a12e87a6f76468af6c0941ac6e6e67dea2800bd52b1eeea636c1b3549e → 9054c3986d075fa017227160e5d59a6a5ffbbdce4bbe770f86af7d2d0c44fa10 on 2026-10-24; only link paths changed when the ticket moved into its folder.
