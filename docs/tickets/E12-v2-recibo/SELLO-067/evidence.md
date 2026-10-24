# SELLO-067 — Add the Ajustar presupuesto action to an overspent Recibo

- **Revision:** tested snapshot is the `sello-067-adjust-budget-link` working tree on
  its merge of `main` at `4c12c86` (bringing in `61b7792`, CI system-image caching,
  with no overlap), with only the ticket and epic status uncommitted; it is
  identified exactly by the retained input fingerprint below. The commit and pull
  request follow this evidence; no claim is made that the gate tested a clean commit.
- **Requirement mapping:** `ReciboSummary.adjustable` is true only when the overall
  budget's standing is `Current` with `Verdict.Over`: never for a finished month, a
  month within or at its limit, or one without a limit. `ReciboScreen` then ends the
  answer slip with **Ajustar presupuesto ›** (brand ink, chevron, 48dp row, one button
  read "Ajustar presupuesto, ir a tus categorías"), after the per-day lines as in the
  v2 mockup. Tapping it scrolls Recibo's own list by the categories card's distance
  from the list's top, at once with reduced motion and animated otherwise, which puts
  the card at the top or the list at its end, then requests focus on the focusable
  **Tus categorías** heading. It sends no action: nothing navigates, and each category
  opens its detail and limit editor under its current/past-month rules as before.
  The link lives only in the Month state, where the card is always drawn, stale
  figures included.
- **Red / Green:** with `adjustable` always false, `ReciboRenderModelTest`'s
  Excedido-only, over-without-an-overspent-category and overspent-month tests failed;
  they passed after the change. `ReciboScreenTest` covers the link's place, role,
  label and target; its absence for Al día, Al límite, a finished Excedido month and
  no limit; the card brought to the top with its heading focused and no action sent;
  a card already in view; older figures at font scale 2.0; and, in a 900 × 700dp
  window with the detail beside the list, focus moving with the detail kept.
- **Gate results:** `./scripts/verify-ticket SELLO-067 --retain` passed all six checks.
  Host: 529 tests; device: 570 tests; zero failures, errors or skips. Lint had zero
  errors and 32 existing warnings. After merging `main` the gate was run again and
  passed with the same counts.
- **Quality run:** run 20261020T212841Z-1910a6d1; SELLO-067 G2 passed; HEAD 4c12c86, inputs sha256 d6c528ac6200; report docs/tickets/E12-v2-recibo/SELLO-067/quality-report.json sha256 67c698c458a3a2b0b8899e73b477f7d6b6d6355d9497d68dbeda51213753eb60
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp, animations off), serial `emulator-5582`. On the same AVD a debug build
  with one synthetic category (limit 100.000) and an expense of 143.700 showed
  −43.700 Excedido, 0 por día and the link; tapping it scrolled the short list to its
  end. Focus is checked through Compose semantics; whether TalkBack follows it was
  not checked by hand. No personal history or physical device was used; no manual
  TalkBack session or hosted CI run is claimed.
- **Epic:** with this ticket every E12 ticket (SELLO-061–069) is Done: Recibo's
  status, answer slip, categories, month bar, dock, tabs and save feedback follow the
  v2 Recibo states in today's scope. This pull request proposes E12 Done.
- **Review:** executor self-review covered the current-month-only rule, overspending
  through categories without a limit, the scroll on short and long lists, reduced
  motion and the two-column layout. Done is proposed by this ticket's pull request;
  its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No owner
  approval, merge or hosted CI pass is claimed.

