# SELLO-061 — Replace the pace verdict with the factual budget status

- **Revision:** tested snapshot is the uncommitted `sello-061-factual-status` working
  tree on `7f94c75e2957d310c99d7fa54997a3663e176640`; it is identified exactly by the
  retained input fingerprint below. The commit and pull request follow this evidence;
  no claim is made that the gate tested a clean commit.
- **Requirement mapping:** `MonthlyBudgetPolicy.verdict(limit, spent)` compares
  recorded spending with the limit only; `Verdict` is `OnTrack`/`AtLimit`/`Over`
  (`Tight` removed). `MonthlyBudgetPolicyTest` proves BG-E01 (Al día, 600.000 left,
  60.000 a day), BG-E04 (category Over, overall Al día, 19.000 a day), BG-E05 (Al
  límite with 0 a day, then Excedido by 1), B05 (zero limit Al día, then Excedido),
  B08, a category exactly at its own limit (Al límite), unlimited spending pushing the
  month over with no category over (B01), the 393.700 of 750.000 day-16 case (Al día,
  356.300 left, 22.250 a day), and the BG1 boundaries. The forecast stays exact in
  the snapshot (V05 still 1.425.004/15) for Resumen, which keeps its own words and
  `PastVerdict` mapping; `ResumenSnapshotTest` pins that. Recibo and category detail
  read Al día, Al límite, Excedido and, for past months, Cumplido or Excedido, with
  gain/neutral/loss inks (`ReciboRenderModelTest.eachStampHasTheInkOfWhatItSays`) and
  the same words in spoken descriptions. No Recibo or category-detail stamp, string
  or test still uses Justo, Pasado, Bajo el límite or Sobre el límite. D03, the
  example corpus (B04/B05/B08 words; V and F verdicts marked retired, amounts
  unchanged), ARCHITECTURE.md §4 and the snapshot/Recibo guides describe the new rule.
- **Red / Green:** against the pace rule, six domain tests failed for the intended
  reason (`Over` or `Tight` where recorded spending was below or at the limit) while
  every amount and daily figure passed; `ReciboScreenTest` and
  `CategoryDetailScreenTest` failed on the old words (Pasado, Bajo el límite, Sobre el
  límite, Justo). All passed after the change. One render-model expectation changed
  by design: Transporte with 145.000 of 200.000 is Al día, not the projected Justo.
- **Gate results:** `./scripts/verify-ticket SELLO-061 --retain` passed all six
  checks: board validation/tests, quality-runner tests, architecture, host and API-30
  device gates. Host: 519 tests; device: 528 tests; zero failures, errors or skips.
  Lint had zero errors and 32 existing warnings. The first gate run failed only on
  ktlint line length in the new tests; `ktlintFormat` fixed it before the passing run.
- **Quality run:** run 20261020T034516Z-0e179666; SELLO-061 G2 passed; HEAD 7f94c75, inputs sha256 218faf16f8aa; report docs/tickets/E12-v2-recibo/SELLO-061/quality-report.json sha256 3a43f8194d39ac88ff9ee7404a5c4ae6576a078619bbc97cb8d8c0309eb0e4d1
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off, matching the hosted runner), serial `emulator-5582`.
  Focused device tests and a visual check ran on API-31 `test31` (`emulator-5580`):
  on 19 October, a 100.000 Mercado limit with 80.000 recorded shows Al día, 20.000
  left and 1.500 a day (the pace rule would have said Pasado); 20.000 more shows Al
  límite with 0 a day. `WalkingSliceJourneyTest`'s saved-expense journey failed once
  there on a keypad lookup unrelated to stamps and passed when rerun alone and in the
  gate. No personal history or physical device was used; the sandbox (SELLO-024) does
  not exist yet, so no sandbox scenario was extended; no manual TalkBack session or
  hosted CI run is claimed.
- **Review:** executor self-review covered the BG1 table and examples, the current
  versus past at-limit distinction, Resumen's untouched words and forecast, and the
  failed/stale states. Done is proposed by this ticket's pull request; its merge is
  the designated owner acceptance under `QUALITY_FLOW.md`. No owner approval, merge
  or hosted CI pass is claimed.

