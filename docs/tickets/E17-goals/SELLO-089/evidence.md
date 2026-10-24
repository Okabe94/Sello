# SELLO-089 — Add the Metas tab and goal detail

- **Revision:** the gate tested commit `cb74eba` on `sello-089-metas-tab` (SELLO-089's
  commit `30259ab` merged with `main` at `6ca03d5`), with no dirty files. This file,
  `handoff.md`, the status and Execution progress, `artifacts/` and the retained report
  are added to that commit afterwards.
- **Requirement mapping:** cards ordered by highest exact completion, completed goals
  last with LOGRADA → `GoalPolicy.ordered`, `GoalsRenderModel.metas`, `MetasScreen`;
  `GoalPolicyTest.activeGoalsGoHighestCompletionFirstAndCompletedGoalsLastNewestFirst`
  (incl. 57,9 % before 57,4 %), `GoalsRenderModelTest.metasOrders…`,
  `MetasJourneyTest.goalsAppearBestFirst…`. The band totals active goals only →
  `GoalPolicy.overview`; `theBandAddsActiveGoalsOnlyAndThisMonthsAportesWithoutWithdrawals`,
  `aBandTotalThatDoesNotFitIsReportedNotClipped`, `theBandAddsActiveGoalsAndThisMonthsAportesOnly`;
  goals and investments are separate records, and no investment exists yet. On this tab
  the dock reads **Aportar a una meta** → `MetasDestination` with `SelloDock(tone = Gain,
  icon = Flag)`; `MetasScreenTest.onMetasTheDockReadsAportarAUnaMetaAndNeverAnotarOrAMonth`
  and `DockToneTest`. Per the owner's decision the dock is passed in only once
  SELLO-091's screen exists, so the running app shows no dock on Metas yet
  (`untilTheirScreensExistMetasOffersNoDockAndNoPlus`). No month bar →
  `ShellScreen(titleOnly)`; `withNoGoalsMetasInvitesWithoutAMonthOrAnAnotar…`. Goal
  detail and states → `GoalDetailScreen`, `GoalDetailViewModel`; `GoalsViewModelTest`
  (star, unknown outcome, deletion, restoration), `MetasJourneyTest` (detail, star,
  deletion). Loading, failed, empty and adaptive → `loadingAndAFailedFirstReadAreNeverNoGoals`,
  `GoalBoardObservationTest`, `metasAndTheDetailKeepEveryFigureWholeInEveryThemeSizeAndWindow`.
- **Red / Green:** each new behaviour test failed first on behaviour, not compilation.
  `./gradlew :domain:test --tests '*GoalPolicyTest'`: 13 of 52 failed against stubs
  returning empty results, then 52/52 passed. `GoalBoardObservationTest`: 3 failed
  (only Loading emitted), then passed. `DockToneTest`: the gain dock and rail measured
  brand blue, then green. `CompletedGoalLandingTest`: the stamp was already down on the
  first frame (709 ink pixels), then landed. The band test: equal heights, then shorter
  without aportes. `GoalsRenderModelTest` 12/12 and `GoalsViewModelTest` 8 of 9 failed
  against stubs, then passed; `DateLabelsTest` 2 failed, then passed. The screen tests
  were written after the screens; a deliberate break (month bar and Anotar back on Metas)
  failed two of them. `NavigationStateTest` pinned two tabs and now pins three, Back from
  Metas going to Recibo.
- **Gate results:** `./scripts/verify-ticket SELLO-089 --retain` passed all nine G2
  checks, full coverage. Host: 834 tests; device: 806 tests (data 269, design-system
  143, app 373, catalog 21); zero failures, errors or skips. Lint had zero errors and
  34 warnings, as on `main`.
- **Quality run:** run 20261024T180812Z-7f1389da; SELLO-089 G2 passed; HEAD cb74eba, inputs sha256 803aa742cef3, changes sha256 a6f3f56af284; report docs/tickets/E17-goals/SELLO-089/quality-report.json sha256 a920c2db0b9056bb4aa6d9a868b350e75a539bedcf67ed124b62a5e41ba915a6
- **Device / Artifact:** the dedicated synthetic AVD `sello089_api30` (API 30, 320 × 640),
  deleted afterwards. Screenshots of the debug build with synthetic goals written
  through the real commands: `artifacts/metas.png`, `metas-completed.png`,
  `metas-empty.png`, `goal.png`, `goal-aportes.png`, `goal-movements.png`.
- **Review:** executor self-review; owner review by merging the pull request.
