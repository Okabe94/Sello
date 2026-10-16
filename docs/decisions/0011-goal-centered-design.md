# 0011 — A budget that serves the user's goals

**Date:** 2026-10-15 · **Owner:** project owner (user) · **Ticket:** none
**Status:** product/design direction confirmed in the owner interview; Home,
entry, Resumen and income review summaries approved. The final visual design is
[Sello v2](../design/v2/README.md) ([ADR 0013](0013-sello-v2-design.md)), which
supersedes the items marked below. Implementation and remaining replacement
financial definitions pending.

## Context

After E01–E05, the owner found routine recording interrupted by confirmation
screens, a large custom keypad and changing layouts. Resumen lacked a clear order
and purpose. Receipt paper, stamps and bold figures remained cohesive and valued.
The interview expanded to income, allocation, goals and investments: success is a
realistic, predictable budget and progress toward personal goals, rather than
universally spending less. The owner requested documenting this direction first.

## Decision

Adopt the [design philosophy](../design/philosophy.md) for subsequent screen and
component design. Its working motto is **Tu presupuesto al servicio de tus metas**;
the wording is editorial, not final interface-copy approval.

- Home leads with remaining monthly budget and secondary overall daily allowance.
  Retire spending-pace extrapolation as the basis for its verdict. Category overruns
  do not make an otherwise healthy overall budget unsuccessful. Future recurring
  commitments must inform allowance; their exact treatment needs reviewed examples.
- Keep primary actions discoverable and stable. Expense entry starts with focused
  amount and the Sello pad (superseded: see ADR 0013); category/note access is easy and Add stays
  reachable. Use brief confirmed-save feedback for ordinary expense/income creation.
- Resumen explains patterns and adjustments, with one small, restrained insights
  section. Advice is factual, gentle, actionable and grounded in relevant history.
- Encourage intentional income allocation and user-defined goals. A recorded goal
  contribution counts as progress without external proof. ADR 0012 subsequently
  confirms category-only allocation and distinct expected/recorded income; goal and
  investment entries do not independently allocate income.
- Give Metas a main destination and one compact home presence below the summary.
  Respect a user's chosen goal; default to the active goal with highest completion
  percentage. Celebrate milestones modestly and completion substantially; encourage
  adjustments without presenting changed plans as personal failure.
- Prefer three clear main tabs when feasible; allow at most four. The working
  structure is Recibo, Resumen, Metas, Inversiones as their capabilities ship.
  Goals and investments remain independent records, even if users track the same
  money in both. Investments lead with portfolio value and clearly identified return.

### Follow-up screen review

> **Superseded by [ADR 0013](0013-sello-v2-design.md):** the pad is confirmed for
> every amount and the note uses a composer docked to the keyboard; final design was
> done in this project as Sello v2; an earned milestone stamp stays (faded) after a
> withdrawal; goals complete with **Marcar como lograda**, not automatically. The
> paragraphs below are kept as the record of the earlier answers.

The owner subsequently reopened the system-keyboard preference: immediate pad
availability and `000` should be retained if a compact, stable layout can support
them. The review proposal now compares a fixed pad with the prior system-keyboard
option. The owner selected a dialog for note entry, preserving the underlying
amount/category/pad layout. This qualifies the earlier default below; the final
pad choice remains conditional on layout/device review, without changing the
implemented components. See the updated philosophy for the current design target.

The owner then accepted the exploration as useful conceptual direction and asked
that final concrete designs be completed by someone else. Continue step 3 through
[written screen/interaction briefs](../design/screen-briefs/README.md), with
confirmed intent separated from recommendations, visual choices and unresolved
behavior. The HTML is optional reference material, not an approved layout or an
implementation specification. This neither assigns/contacts a designer nor blocks
independent product/financial definition on finished mockups. Affected UI work
still needs its reviewed design and applicable technical/financial inputs.

After reviewing the presented Home, entry, Resumen and income notes, the owner
answered **“Approved, let's continue”** on 2026-10-15. Record that as approval of
those product/interaction review summaries, retaining the notes' explicit open
questions and conditional pad feasibility. It does not approve final layouts,
unpresented financial cases or technical implementation. Separate Metas and
Inversiones briefs now consolidate earlier interview direction for the next review;
their new recommendations remain drafts, with investment mechanics still later work.

In the following Metas review, the owner chose **Optional target date** and
**Record a withdrawal**, confirming goals without deadlines and a withdrawal
entry preserving contribution history when savings are used before completion.
The remaining goal ledger, correction, milestone and completed-state policies stay
open in the Metas brief; this is not approval of those unpresented cases or of
automatic expense/income/category operations.

The owner subsequently rejected retaining an earned milestone stamp when a
withdrawal drops current progress below it: reflect true progress without making
a fuss. Preserve contribution/withdrawal history while updating the current earned
display. A separate **Completadas** section was accepted, with fanfare when viewing
and revisiting completed goals. Exact visual/motion treatment remains with the
designer; milestone re-crossing, completion triggers and later withdrawal/correction
effects are not settled by these answers.

The owner next chose **automatic completion** when recorded savings reach the
target, moving the goal into Completadas without a manual completion step. This
settles the ordinary contribution-driven trigger; later reopening, target edits
and corrections remain open lifecycle cases.

The owner also accepted a small celebration when genuine later progress reaches
the same milestone again after a withdrawal. Re-crossing feedback is therefore
confirmed; reopening/redrawing the same state is not a new crossing or permission
to replay a financial operation. Detailed correction/multi-threshold/recovery
rules remain scoped implementation inputs.

For Inversiones, the owner accepted quiet supporting context on the overview
showing the age of recorded valuations. This confirms that presentation choice
alongside the earlier portfolio-value/return direction, not detailed valuation-date,
return-period, cash-flow, FX or freshness-threshold policy. The written briefs now
cover the discussed screens for a consolidated designer handoff, with remaining
recommendations/policies labeled and the HTML still optional reference material.

## Superseded decisions and transition

This table changes the target direction. It does not rewrite what Done tickets
delivered, alter their historical acceptance/evidence, or claim the target is built.

| Prior authority or delivered choice | Replacement direction | Transition boundary |
| --- | --- | --- |
| v1.1 principles: “paper trail” as the organizing purpose; amount entry through the custom keypad (`#principles`, `#c-amountkeypad`, `#s-anotar`); E02/SELLO-008 input components | A usable budget serves personal goals; essential controls stay visible; amounts keep the Sello pad (ADR 0013). | Preserve paper, stamps, fonts, inks, category circles, exact-money semantics and accessibility. Existing keypad/catalog code is not removed by this record. |
| D03 and architecture §4: projected 95%/100% thresholds drive current-month Recibo judgment; E03/SELLO-014 and E04/SELLO-018 implement it | Retire pace-based home judgment; lead with remaining budget and overall daily guidance. | The replacement classifier, labels and independent examples are pending. Existing D03 arithmetic and tests remain the delivered baseline until explicitly replaced in scoped work; this record does not choose new thresholds or change rounding. |
| v1.1 income receipt (`#s-ingresos`); E04/SELLO-017 and E05/SELLO-021 mandatory expense/income post-save receipt screens | Brief feedback and return after a confirmed ordinary save. | Durable operation receipts and recovery remain mandatory. This change does not remove discard, archive, restore/reset or other protective confirmations. |
| v1.1 Resumen (`#s-resumen`) and E05/SELLO-022 fixed actuals/net-flow hierarchy | Organize around comparisons, repeated category problems and useful adjustments, with restrained suggestions. | Preserve the meanings of net flow, budget remaining and allocation; advanced comparisons/insights retain their scoped delivery prerequisites. |
| v1.1 and architecture §1: three-tab limit; Metas behind Patrimonio's Inversiones/Metas switch (`#s-inversiones`, `#s-metas`) | Metas is primary; prefer three destinations where equally clear, permit a maximum of four with separate Metas and Inversiones. | Only Recibo and Resumen exist today. Future navigation waits for working capabilities; no new routes or release-scope expansion here. |
| v1.1 home (`#s-recibo`): summary and categories without a goal | Add one compact user-selected goal and a small early-month review invitation. | Goal presence waits for Metas. Default is highest completion percentage; ties, invalidated choices and invitation timing remain open. Keep home simple. |
| v1.1 judgmental current-month copy and routine receipt ceremony; v1.1 goal rewards (`#s-metas`) | Neutral exact overrun facts and next steps; milestone/completion celebrations carry the emotional reward. | The accepted tone is factual; v2 names the stamp **Excedido** and specifies the celebrations (ADR 0013); never hide financial failures or amounts. |

ADR 0007's no-peso-symbol rule is retained. D04 budget history/zero/unlimited,
D06 ordering, guarded undo, local-only privacy, exact-money validation, module
boundaries and durable commit recovery remain in force. The new direction does not
impose per-category daily spending caps, automatic category reallocation, universal
savings targets, bank verification or goal/investment linking.

## Alternatives and consequences

Keeping the current journey preserves completed behavior but retains the friction
the owner identified. Hiding main controls would reduce visible density at the
cost of discoverability, which the owner rejected. Replacing the visual identity
would discard a part of Sello the owner values. Treating every income/expense save
as a celebration spends attention reserved for meaningful goal progress.

Record current behavior separately from the target. Keep the v1.1 HTML snapshot
byte-for-byte and make the philosophy and this record discoverable from the design
index, architecture and project README. New design decisions override only the
listed presentation/direction choices; financial contracts remain explicit. No
schema, dependency, release increment or ticket status changes accompany this ADR.

Before implementation, resolve the philosophy's open financial definitions and
scope focused follow-up work. [ADR 0012](0012-budget-guidance-contract.md)
tracks the step 2 choices, examples and partial owner confirmations. Its remaining
proposals do not constitute approved financial rules.
ADR 0012 BG4 supersedes the older future sum of category limits plus goal/investment
contributions with category-only allocation. Independent goal/investment entries
therefore need no overlap deduction or automatic deduplication. The suggestion flow
must not claim that a tracking-only contribution changes unassigned income.

## Verification and acceptance boundary

This is documentation-only work. Verify local document links and snapshot hash,
review the supersession list against actual components/screens and E01–E05, and run
the applicable unticketed documentation G1 gate. No behavioral red/green, device
journey or implemented-design claim follows from this record. Owner interview
decisions are product input, not independent technical review or a hosted approval.
The new philosophy/ADR and authority notices do not retroactively modify the
approved D01–D10 financial examples or completed ticket acceptance.
