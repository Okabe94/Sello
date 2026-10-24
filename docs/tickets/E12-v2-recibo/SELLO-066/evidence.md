# SELLO-066 — Replace the saved receipt with brief feedback after a save

- **Revision:** tested snapshot is the `sello-066-brief-save-feedback` working tree on
  `main` at `9869337`, with only the ticket status uncommitted; it is identified
  exactly by the retained input fingerprint below. The commit and pull request follow
  this evidence; no claim is made that the gate tested a clean commit.
- **Requirement mapping:** after `Committed` for a new expense or income, the form
  keeps the saved identifier in saved state, reads the record back
  (`ExpenseReads.byId`, `IncomeReads.byId`), sends `CorrectionNotice.ExpenseRecorded`
  (amount and category name) or `IncomeRecorded` (amount) through `ExpenseCorrections`
  and closes; `recorded` also stops any second submit. The shell's
  `CorrectionNoticeBar` words it, "Gasto anotado: 48.700 en Comida" or "Ingreso
  anotado: 150.000", in the SELLO-069 confirmation look (ink background, paper text,
  check, polite live region with the amount in pesos), for 4 seconds, with no undo or
  dismissal, on whichever screen the form was opened from (Recibo, Resumen, a
  category, income history). A read-back that fails still says "Gasto anotado" or
  "Ingreso anotado" and sends nothing more. An unknown outcome keeps the form with
  "Comprobar si se guardó" and no notice; a definite failure keeps the draft; edits
  keep "Gasto actualizado" / "Ingreso actualizado". Haptics run once per saved record,
  before the form closes. A form restored after process death that finds a saved
  identifier says it and closes; one that finds only an operation asks storage about
  it, never sending again. Newest wins, as SELLO-069 decided: the notice replaces an
  offer to undo, whose deletion stands. `ExpenseReceiptView`, `IncomeReceiptView`, the
  receipt panes, the `Done` actions, their tags and ten strings are removed.
- **Red / Green:** with the form keeping the saved identifier but neither closing nor
  saying anything, the closed-after-commit, read-back, unreadable, unknown-outcome and
  process-death tests in `ExpenseFormViewModelTest` (7) and `IncomeFormViewModelTest`
  (6) failed; they passed after the change. `ExpenseCorrectionsTest` covers a save
  replacing an undo offer for 4 seconds; `ConfirmationNoticeShellTest` covers the four
  wordings and what is read aloud; `ExpenseEntryJourneyTest`, `IncomeJourneyTest`,
  `ResumenJourneyTest` and `WalkingSliceJourneyTest` save through the installed app and
  wait for the notice instead of pressing Listo, including after recreation and a
  double press.
- **Gate results:** `./scripts/verify-ticket SELLO-066 --retain` passed all six checks.
  Host: 526 tests; device: 564 tests; zero failures, errors or skips. Lint had zero
  errors and 32 existing warnings.
- **Quality run:** run 20261020T191656Z-d7adf910; SELLO-066 G2 passed; HEAD 9869337, inputs sha256 78739e979890; report docs/tickets/E12-v2-recibo/SELLO-066/quality-report.json sha256 68a1d2588475ae7b0c263c6faad5f9ed32c673f771e33d383bb9766f124a8e62
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`. On the same AVD a debug build
  with one synthetic category (limit 400.000) saved 48.900 from Recibo and returned
  straight to it with "Gasto anotado: 48.900 en Comida" above the dock, 351.100 left
  and 29.250 por día; the notice was gone four seconds later. The unknown outcome and
  process death are covered at the view model with the real recovery path: the
  installed app has no way to force an unknown outcome, and an instrumented test cannot
  kill its own process. No personal history or physical device was used; no manual
  TalkBack session or hosted CI run is claimed.
- **Review:** executor self-review covered the read-back source of the words, a failed
  read-back, double sends, edits, the restored form and precedence over an undo offer.
  Done is proposed by this ticket's pull request; its merge is the designated owner
  acceptance under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is
  claimed.

