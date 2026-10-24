# SELLO-070 — Bring expense entry and edit to v2

- **Revision:** tested snapshot is the `sello-070-v2-expense-entry` working tree on
  `main` at `9bcb195`, with only the ticket status uncommitted; it is identified
  exactly by the retained input fingerprint below. The commit and pull request follow
  this evidence; no claim is made that the gate tested a clean commit.
- **Decisions:** the owner chose, on 2026-10-20, to drop the live preview, as v2 has
  none ("Vista previa, si lo anotas" and its strings are gone; `ExpensePreviewPolicy`
  stays in the domain, unused by the form). The amount paste button, which v2 does not
  show, is kept as an existing feature: `EntryAmountSlip` gains `rejectedText` and
  `onPaste`, tested in `V2ComponentTest`, as E13 asks for gaps in shared components.
- **Requirement mapping:** `ExpenseEntryScreen` is rebuilt on `V2EntryLayout`: the
  Gasto/Ingreso switch (not when correcting), `EntryAmountSlip` ("Gasto en pesos", the
  date pill, the chosen category's icon, the stepped figure), `V2ChipRows` with
  `SelloChip`s carrying category icons and, for a new expense, `AddChip` **Nueva**, the
  `EntryLine` note row and `NoteComposer`, then **Anotar gasto** above the compact
  `AmountKeypad`. Only the choices scroll; a short window scrolls the amount with
  them, and a wide or sideways one puts the button and pad beside the rest. No
  category is chosen at opening unless a caller asked for one ("Anotar aquí", a link,
  the first category made for an entry): the last-used and only-category defaults are
  gone. **Nueva** opens the category editor; the entry destination reads the editor's
  `CREATED_CATEGORY_RESULT` from its own navigation entry and sends
  `ExpenseEntryAction.CategoryCreated`, which chooses it once the month offers it and
  keeps the draft. The composer draft lives in `SavedStateHandle`, counts code points,
  keeps text over 60 with a red counter, "Máximo 60 caracteres. Tu texto sigue aquí."
  and a disabled ✓, opens with the cursor at the end, and survives process death. A
  13th digit sets `atLimit`: **Máximo 12 dígitos** replaces the label until the next
  key that changes the amount. What is missing or went wrong sits just above the
  button, 8dp above the pad, so it is always in view and never moves a key; saving
  says "Guardando…", a definite failure "No se guardó. Tu borrador sigue aquí." and an
  unknown outcome "Aún no sabemos si se guardó…" with **Comprobar si se guardó**.
  Recovery, restored drafts, past-month edits, their announcement and the edit-only
  delete keep their rules.
- **Red / Green:** with the new actions and fields in place but doing nothing, the
  no-preselection, Nueva, composer (open/finish/cancel, 61 code points kept and
  refused, restored after process death) and 12-digit message tests in
  `ExpenseFormViewModelTest` failed; they passed after the change. Twelve older tests
  that relied on the only category being chosen now pick it themselves. The geometry
  tests now hold the v2 order: the button 8dp above the pad in one column and sideways,
  nothing moving whatever is said, and no key able to record. The new
  `nuevaCreatesACategoryAndComesBackWithItChosenAndTheDraftKept` journey first failed
  because the editor's result was read from the view model's saved state, not the
  navigation entry's; it passed once the destination passed it on.
- **Gate results:** `./scripts/verify-ticket SELLO-070 --retain` passed all six checks.
  Host: 529 tests; device: 576 tests; zero failures, errors or skips. Lint had zero
  errors and 32 existing warnings.
- **Quality run:** run 20261020T234226Z-722c30c3; SELLO-070 G2 passed; HEAD 9bcb195, inputs sha256 f9d30a26a0d4; report docs/tickets/E13-v2-current-app/SELLO-070/quality-report.json sha256 b432711d78ab0c8575cb6b5f08aaccfc52a181bef657d33cbaf9bdf22d9c0de9
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`. On the same AVD a debug build
  showed the v2 form with Nueva and the pinned button and pad; the composer docked
  above the keyboard without moving the form ("Gasto en Comida · 42.700", 8/60); saving
  returned to Recibo with "Gasto anotado: 42.700 en …" and 357.300 left of 400.000. No
  personal history or physical device was used; no manual TalkBack session or hosted
  CI run is claimed.
- **Review:** executor self-review covered every kept rule, the restored composer, the
  editor handback, edits without switch or Nueva, and the short, sideways and 2.0
  layouts. Done is proposed by this ticket's pull request; its merge is the designated
  owner acceptance under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass
  is claimed.

