# Expense and income entry brief

**Owner-approved review summary, 2026-10-15 · Pad confirmed; final visual design:
Sello v2.** Recommendations and listed open decisions retain
their stated review boundaries.
Use the [brief workflow and decision labels](README.md). The job is to record a
transaction quickly and confidently, then let the user continue.

> **Designed in [Sello v2](../v2/README.md):** [expense and income entry](../v2/entry.html). Where this brief and v2 differ,
> v2 and [ADR 0013](../../decisions/0013-sello-v2-design.md) apply; the open policies below stay open.

## Confirmed intent

- One tap from Home reaches expense entry, with the amount ready immediately.
- Fast category selection, an easily found optional note, and a bold Add action
  should make ordinary entry a short, predictable journey. Date remains accessible.
- Income follows the same interaction language with income-source controls and
  its own draft. Switching modes must preserve both drafts.
- The owner values the pad's immediate availability and `000` shortcut and would
  include it if it can remain stable without crowding the screen. Its earlier
  rejection was reopened; the final solution is not settled by the HTML's fit.
- Adding a note opens a **dialog**. Keep the main form/pad in place underneath;
  finishing the note returns to the same amount, category and primary action.
- Confirmed ordinary saves give brief feedback and return to the caller, without
  a separate receipt screen. Milestone celebrations belong to goals.

## Hierarchy and preferred interaction direction

Lead with the amount. Keep category/source selection direct and obvious. Present a
clear note affordance; a saved note can show a concise preview with access to edit.
Do not let a financial preview or expanding conditional sections move the amount,
pad or Add. Live financial previews have not earned space in this simplified flow.

**Recommendation:** use a compact four-row pad with digits, `000` and backspace,
plus a persistent Add action. Make the number controls and Add immediately usable
when the screen opens. Avoid simultaneous custom and system number keyboards.
Add above the pad is one explored arrangement, not a required final coordinate.
Landscape, large text and different keyboards need deliberate treatment; do not
shrink touch targets to force the compact-phone arrangement into every window.

v2 confirms the fixed Sello pad for every amount (ADR 0013); the system-keyboard
alternative is retired. Text fields keep the phone keyboard.

## Note dialog

The dialog is an intentionally focused editing step for optional information.
Open with the current note, make text entry easy, provide clear completion and
cancellation, and return to the intact underlying form. The main Add action still
means saving the transaction; finishing the note must not submit it.

**Recommendation:** use Listo to accept note edits and Cancelar to leave the
previous note unchanged. Preserve invalid original text with a visible explanation;
never silently truncate it. The existing note limit is 60 code points. Final labels,
dialog proportions and native keyboard placement belong to design/device review.
Unaccepted note edits on Back/outside dismissal and restoration of an open editor
still need an explicit interaction decision; the prototype is not that policy.

Using the same dialog pattern for an Otro income-source name is an explored
recommendation, not an owner-confirmed extension. Otro still requires its existing
valid 1–24-code-point trimmed name; ordinary income sources remain easy to choose.

## States and scenarios

| Situation | Observable behavior to preserve |
| --- | --- |
| Open from Home | Ready amount entry; no extra tap to summon number controls. |
| Choose another category or add a note | Amount/category draft survives; stable primary controls on return. |
| Switch expense/income | Each mode retains its own amount, category/source, date and note. |
| Paste 1e3 or an out-of-range value | Retain original input and explain the error; no repair, truncation or submit. |
| Note exceeds 60 code points | Keep the full editable text; explain and prevent invalid acceptance. |
| Submit is busy | Prevent duplicates and conflicting edits; do not present another save as available. |
| Confirmed commit | Return to caller with brief success feedback after the result is known. |
| Definite rejection | Explain that it was not saved; retain the draft for correction/retry. |
| Uncertain commit | Offer checking the same operation; never invite a blind second write. |
| Leave a changed transaction draft | Preserve the existing protective discard journey. |
| Restore/recreate | Preserve the draft and operation identity; never submit just because the screen returns. |

Existing strict money grammar, transaction bounds, date rules, edit/delete/undo,
historical-correction behavior and durable operation receipts remain governed by
[architecture](../../../ARCHITECTURE.md). New-entry simplification does not remove
those protections or redesign existing record editing by implication.

## Designer discretion and remaining inputs

The designer determines exact pad dimensions, key styling, amount typography,
chip arrangement, date affordance, note-dialog composition and adaptive layouts.
The HTML's fixed 216px region and side-by-side landscape arrangement are examples.
Do not copy them as acceptance criteria.

The pad's final feasibility and open-dialog Back/dismissal/restoration behavior
remain review work. Verify real Android focus, IME/insets, TalkBack, hardware input,
small screens and large text when implemented. Browser checks establish only the
exploration's behavior, not a native implementation or final design approval.
