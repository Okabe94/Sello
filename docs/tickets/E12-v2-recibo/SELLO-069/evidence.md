# SELLO-069 — Wire the confirmation notice into the shell

- **Revision:** tested snapshot is the uncommitted `sello-069-confirmation-notice`
  working tree on `925deb0b1b865bb3e026860c6e21816513882e8a`; it is identified exactly
  by the retained input fingerprint below. The commit and pull request follow this
  evidence; no claim is made that the gate tested a clean commit.
- **Requirement mapping:** `CorrectionNotice.Confirmed(message, spoken)` and
  `ExpenseCorrections.confirmed()` pass a confirmed save through the shell's one notice
  holder, which owns the timing (four seconds, monotonic clock); `SelloNotice` gains
  no timer. `CorrectionNoticeBar` draws it as `SelloNotice(confirmation = true)` in the
  scaffold's existing notice slot. Owner decision (2026-10-19): the newest notice wins,
  so a confirmation takes the place of an offer to undo and that deletion stands.
  `ExpenseCorrectionsTest` proves the four seconds and dismissal, newest-wins over an
  undo offer (undo then restores nothing) and a later notice replacing it.
  `ConfirmationNoticeShellTest` proves, on Recibo and Resumen tabs, in light and dark
  themes and at font scale 2.0, that the notice sits above the dock and tab bar, its
  check is drawn and silent and the message is announced politely with its spoken
  text; beside a rail (1000 × 700dp) it sits over the content. Existing correction
  notices are unchanged: `ExpenseCorrectionJourneyTest`, `IncomeJourneyTest` and the
  shell tests pass untouched. `SelloNotice`'s check gained the `SELLO_NOTICE_CHECK_TAG`
  test tag, nothing else.
- **Red / Green:** with the API stubbed (`confirmed()` doing nothing, the shell drawing
  the plain look), the three new holder tests failed (no notice; an undo offer still
  restored) and every new shell test failed on the missing check while placement
  already passed. All passed after the change.
- **Gate results:** `./scripts/verify-ticket SELLO-069 --retain` passed all six checks.
  Host: 522 tests; device: 533 tests; zero failures, errors or skips. Lint had zero
  errors and 32 existing warnings. The first gate run failed: 20 tests across
  `:catalog`, `:design-system` and `:app` found no Compose hierarchy (the emulator was
  not in the foreground; it was woken and kept awake), and the rail case asserted the
  check visible in a window larger than the 320 × 640 screen; it now asserts existence
  and position, as the other oversized-window shell tests do.
- **Quality run:** run 20261020T042037Z-14ec9578; SELLO-069 G2 passed; HEAD 925deb0, inputs sha256 c8207ee11893; report docs/tickets/E12-v2-recibo/SELLO-069/quality-report.json sha256 f7a234c3258c15bf648f2aa08d6879fb3058158da3167e7dd3a8c7e42fabd43e
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`; focused device tests ran on
  API-31 `test31` (`emulator-5580`). Nothing in the app sends a confirmation until
  SELLO-066, so there is no in-app visual change to check. No personal history or
  physical device was used; no manual TalkBack session or hosted CI run is claimed.
- **Review:** executor self-review covered unchanged existing notices, placement on
  every tab and beside the rail, both themes and large text. Done is proposed by this
  ticket's pull request; its merge is the designated owner acceptance under
  `QUALITY_FLOW.md`. No owner approval, merge or hosted CI pass is claimed.
