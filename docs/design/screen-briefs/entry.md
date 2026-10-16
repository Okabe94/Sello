# Expense and income entry brief

Designed in [Sello v2](../v2/README.md) as [expense and income entry](../v2/entry.html);
labels and authority are in the [briefs README](README.md). The job is to record a
transaction quickly and confidently, then let the user continue.

## Confirmed intent

- One tap from Home reaches expense entry, with the amount ready immediately on the
  Sello pad.
- Fast category selection, an easily found optional note and a bold Anotar make
  ordinary entry a short, predictable journey. The date stays accessible.
- Income follows the same interaction language with income-source controls and its
  own draft. Switching modes preserves both drafts.
- Nothing moves the amount, the pad or Anotar: no live financial preview and no
  expanding conditional sections.
- The note composer is a focused step for optional information. Finishing it
  returns to the intact form and never submits the transaction. Text over the limit
  (60 code points) is kept and explained, never truncated. An Otro income-source
  name keeps its 1–24-code-point trimmed rule.
- Confirmed ordinary saves give brief feedback and return to the caller, without a
  separate receipt screen. Milestone celebrations belong to goals.

## States to preserve

| Situation | Observable behavior |
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

Strict money grammar, transaction bounds, date rules, edit/delete/undo,
historical-correction behavior and durable operation receipts remain governed by
[architecture](../../../ARCHITECTURE.md). A simpler new entry does not remove those
protections or redesign existing record editing by implication.

## Open

- What Back or an outside tap does to unaccepted note edits, and restoring an open
  note composer.
- Native validation when implemented: focus, IME/insets, TalkBack, hardware input,
  small screens and large text.
