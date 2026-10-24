# SELLO-062 — Rebuild the Recibo answer slip to v2

- **Revision:** tested snapshot is the `sello-062-answer-slip` working tree on `main`
  at `2a0a04b`, with only the ticket status uncommitted; it is identified exactly by
  the retained input fingerprint below. The commit and pull request follow this
  evidence; no claim is made that the gate tested a clean commit.
- **Requirement mapping:** `ReciboSummary` now carries one label
  (`ReciboAnswerLabel`: Restante del presupuesto, or Has gastado este mes with no finite
  limit), the signed remainder (`MoneyFormatter.format`, so an overrun is −43.700 and
  is read "menos"), the stamp, `ReciboPerDay` in the current month and
  `ReciboClosedMonth` in a finished one; spent, limit and remainder come from the same
  `LimitedBudget`. `ReciboScreen` draws it with the v2 `AnswerSlip`, which gains an
  optional `description` so its label, stamp and amount are read as one sentence; under
  a tear line the per-day figure ("25.350 por día", 0 included, in loss ink when over,
  as financial example B04 requires) and "N días, incluido hoy" (plural, "1 día" on the
  last day), or "Mes cerrado · gastaste 920.000 de 1.000.000." in a past month. The
  categories and their detail keep their sentences. Debug previews
  (`ReciboPreviews.kt`) show today, an overrun on the last day, no limit and a closed
  month. No goal, reserved-payment line or Ajustar presupuesto link is drawn.
- **Red / Green:** with the render model mapped onto the new summary but keeping the
  unsigned amount and no closing figures, the overrun, finished-month and at-limit
  closed-month tests in `ReciboRenderModelTest` failed; they passed after the change.
  `ReciboScreenTest` covers each state, the one-day and zero per-day cases, spoken
  descriptions and a signed 19-digit remainder at font scale 2.0; `V2ComponentTest`
  covers the slip's single sentence at font scale 2.0 and 320dp.
- **Gate results:** `./scripts/verify-ticket SELLO-062 --retain` passed all six checks.
  Host: 522 tests; device: 544 tests; zero failures, errors or skips. Lint had zero
  errors and 32 existing warnings. The first run failed `ExpenseEntryJourneyTest`,
  which still read the old no-limit sentence; a second passed with one new lint
  warning from the previews reading resources through the context, fixed by using
  `LocalResources`. The final run followed the HANDOFF update with the same counts.
- **Quality run:** run 20261020T140601Z-92492992; SELLO-062 G2 passed; HEAD 2a0a04b, inputs sha256 505e8e51d212; report docs/tickets/E12-v2-recibo/SELLO-062/quality-report.json sha256 b787734d4975b247a4b46251764f6d16a890aab9b3c941d3c819e5f4ec729fa2
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`. On the same AVD a debug build
  with one synthetic category (limit 1.000.000) showed, at font scale 1.0, 83.300 por
  día over 12 days; after an expense of 1.043.700, −43.700 Excedido with 0 por día; and,
  with the emulator clock moved to 3 November, October closed with "Mes cerrado ·
  gastaste 1.043.700 de 1.000.000." and November at 35.700 por día over 28 days. At
  font scale 1.3 and 2.0 the label and stamp stacked and nothing was cut off. No
  personal history or physical device was used; no manual TalkBack session or hosted
  CI run is claimed.
- **Review:** executor self-review covered the shared category wording, the snapshot
  source of the closing line, the sign in speech and 12+ digit amounts at large text.
  Done is proposed by this ticket's pull request; its merge is the designated owner
  acceptance under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is
  claimed.

