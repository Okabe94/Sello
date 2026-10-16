# Sello design philosophy

**Recorded:** 2026-10-15 · **Source:** project-owner interview after reviewing E01–E05
**Status:** owner-confirmed product/design direction; implementation pending.
The motto is working editorial wording, not final approved interface copy.

## Purpose and working motto

**Tu presupuesto al servicio de tus metas.**

Sello helps people build a realistic, predictable budget they can follow and adjust
as their lives change. Progress means a plan that fits, a greater sense of control,
and reaching personally meaningful savings goals. Spending less is not the universal
measure of success; recreation is a legitimate destination for money too.

## Authority and scope

Read this before designing a screen or extending the design system.
[ADR 0011](../decisions/0011-goal-centered-design.md) lists the decisions this
direction replaces and the implementation transition. The
[v1.1 snapshot](sello-spec.html) remains the visual reference for unaffected details;
[ADR 0007](../decisions/0007-no-peso-symbol.md) still governs currency display.
[Architecture](../../ARCHITECTURE.md) owns financial, recovery, privacy and module
contracts. The [current app baseline](../development/current-app-experience.md)
describes delivered behavior, which does not yet implement this direction.

This document records design intent, not a replacement financial algorithm or
expanded MVP scope. Goals, recurrence, advanced insights and investments retain
their delivery increments. Do not expose unfinished destinations or invented data.
Before changing financial meaning, resolve the decisions at the end of this file
and approve independent worked examples. Completed tickets retain their original
acceptance and evidence; subsequent implementation needs explicitly scoped work.

The final concrete design is [Sello v2](v2/README.md): every screen was reviewed and
locked by the owner on 2026-10-15/16 ([ADR 0013](../decisions/0013-sello-v2-design.md)).
It is the visual source of truth; the [briefs](screen-briefs/README.md) keep the intent
and open policy behind it, and the earlier HTML exploration is history only.

## Principles for every screen

1. **Lead with the user's room to act.** Home's largest figure is the budget margin
   after reserving upcoming recurring payments, with the reserved amount shown at
   lower priority. Overall daily allowance is secondary. A category overrun remains visible
   without turning a healthy overall budget into a negative verdict or moving limits
   automatically. Spending history stays easy to inspect. Pace extrapolation from
   large occasional expenses must not determine home's verdict.
2. **Give each screen a clear job.** Home answers the month's position at a glance.
   Resumen explains patterns and supports adjustments. Metas connects the daily
   effort to personal objectives. Inversiones explains portfolio value and return.
3. **Keep essential actions visible and stable.** Reduce clutter through hierarchy,
   compact controls and fewer interruptions. Explanations and occasional management
   actions, such as editing a category, may sit deeper. Main functionality must not
   acquire extra taps just to make a screen look sparse. Conditional content should
   not displace the amount or the primary recording action.
4. **Let ordinary completion keep the user moving.** Expense and income recording
   give brief, clear feedback after a confirmed save and return to the activity.
   They do not require a separate receipt screen and a second dismissal. Durable
   operation receipts, uncertain-outcome recovery and protective confirmations
   remain mandatory. A celebration never establishes whether money was saved.
5. **Guide with evidence and care.** Suggestions explain an observed pattern and
   offer an adaptable next step. They encourage intentional allocation and financial
   learning without prescribing a universal savings ratio. A person chooses their
   own goals. An unassigned amount or an adjusted plan is not a personal failure.
6. **Make progress meaningful.** Give small celebrations for goal milestones and a
   substantial one for completion. Preserve the value of accumulated progress when
   the person adjusts a contribution or target date. Routine entry is brief; reaching
   a goal earns the special moment. Every celebration respects reduced motion.
7. **Use Sello's identity with purpose.** Keep receipt paper, stamps, bold exact
   numbers, category circles, Cobalto/Violeta light/dark schemes and bundled type.
   Paper and ink provide cohesion; their use should help reading and action. Keep
   restrained semantic color, full accessible money, adaptive layouts and visible
   keyboard/large-font controls. Existing tokens and catalog components remain the
   starting point for new work.

## Screen responsibilities

### Home / Recibo

Keep this the simplest screen: short, visually appealing and quickly digestible.
Lead with remaining budget after upcoming recurring payments are reserved; inform
about the reserved amount at lower priority. Show one overall daily allowance; do not
split that allowance into prescribed per-category daily amounts. Users can choose
where to spend, while each category retains its own remaining/overrun information.
Upcoming recurring expenses must be reserved in the allowance when recurrence ships.
These home calculations remain independent of recorded or expected income.
Recording an already reserved expense for the same amount leaves the headline
unchanged. Once no reservations remain, their supporting context disappears entirely;
do not retain a “0 reserved” line. Failed reads cannot be presented as no reservations.

When Metas ships, place one compact goal slip between the monthly summary and the
categories. Show the goal's name, concise progress and visible access to **Aportar**
and its detail. The user chooses the featured goal; until they do, default to the
active goal with the highest completion percentage. A later automatic ranking must
not override a user choice. Tie and eligibility details remain implementation inputs.

Early in the month, a small invitation such as **Ver cómo cerraste septiembre** can
lead to Resumen. Keep detailed advice there. The goal slip and review invitation
must fit home's attention budget rather than start an accumulating feed of cards.

### Expense and income entry

One tap from home opens expense entry with the amount immediately ready. Category
selection is fast and the prominent Add action stays in a fixed location. Income
entry follows the same principles with its own source controls. Preserve independent
drafts, strict original-input validation and accessible required/busy/error states.

**Follow-up review:** the owner reopened the custom-pad decision because immediate,
always-available number entry and `000` are valuable. v2 settles it: every amount uses
a compact Sello pad (44dp keys, no live previews), text fields keep the phone keyboard
and the note uses a composer docked to the keyboard
([ADR 0013](../decisions/0013-sello-v2-design.md)).

The owner selected a **dialog for adding a note**. Keep the main form and pad in
place underneath it; the system text keyboard belongs to that focused editor.
Closing the dialog returns to the same amount, category, pad and Add position.
The draft uses the same dialog pattern for an Otro source name; that extension is
proposed presentation. Never display both keyboards as competing main-form panels.
Live previews cannot obscure or shift essential controls.

### Resumen

Help users adjust their budget through understandable questions: is spending rising
or falling across months; which categories repeatedly exceed their limits; and which
weekdays carry more spending? Lead each section with its answer and supporting
evidence, using charts where they make that evidence easier to understand.

Allow one small insights section with a limited number of relevant suggestions.
Repeated category overruns belong here; an isolated overrun in a healthy overall
month does not require an intervention. A suggestion to budget unassigned recorded
income toward saving or investing must use category budgets as the allocation path;
recording goal/investment progress alone does not reduce unassigned income.
Remaining category budget is not automatically spare money. No fixed repeat-offender
threshold, suggestion count or review-invitation schedule was approved in the interview.

### Income and allocation

Help people understand how their recorded income is distributed across category
budgets. A user can create categories for saving, investing, recreation or a reserve.
Encourage giving every peso a purpose, including recreation
or a reserve, while recognizing that complete allocation is an aspiration. Show
unassigned income honestly and offer an optional category-budget adjustment path, without another
routine post-save screen. Avoid duplicating the same advice across multiple surfaces.

Recording a goal contribution counts as progress: Sello accepts what the
user records, with no bank connection, external proof or separate transfer verification.
It does not independently allocate income. Expected recurring income can inform
clearly labeled plans and stays distinct from recorded income; it does not increase
home's category budgets or allowance. Detailed occurrence/month-planning rules remain
future work under ADR 0012.

### Metas and Inversiones

Metas deserves a primary destination. Three main tabs remain preferable where
equally clear, with four the maximum. The accepted working structure as capabilities
ship is **Recibo / Resumen / Metas / Inversiones**. This replaces goals' required
placement behind Patrimonio's switch. Only working destinations appear.

Goals and investments are independent tracking tools. A user may choose to represent
the same money in both; this direction does not introduce linking, automatic
reconciliation or a combined wealth total. Only category budgets allocate income;
goal/investment records never create a second allocation deduction.

Goals may have no target date. The owner confirmed optional deadlines and a
withdrawal entry, preserving contribution history, when savings are used before
completion. If a withdrawal drops progress below a milestone, the earned stamp stays,
faded, with one line saying it is still yours; percentage and bar show the honest
figure, without shame or dramatic loss feedback. Keep contribution/withdrawal
history. Completed goals stay in Metas as Lograda rows, with a celebratory revisit.
Reaching 100 % is announced; **Marcar como lograda** completes the goal and earns the
big celebration, with a short Deshacer; a completed goal is read-only. Reaching a
milestone again after progress fell below it lights the stamp back up; reopening or
redrawing the same state does not count as a new crossing (v2,
[ADR 0013](../decisions/0013-sello-v2-design.md)). Detailed withdrawal rules,
corrections, exact replay safeguards and remaining completion lifecycle remain open
in the [Metas brief](screen-briefs/goals.md); no automatic
cross-tool financial operation is approved by those choices.

Inversiones leads with total portfolio value, then gain/loss percentage and a
directional arrow, with semantic color and text equivalents. Distinguish contributed
capital from return: 100 contributed with 10 of gain means value 110 and return +10%
in that simple case. Adding capital alone is not investment gain. Below the answer,
provide accessible distribution, contributions/withdrawals and dividends. This is
an initial future-feature direction, not approval of full return, FX or ledger policy.
The owner also confirmed quiet, visible overview context about the age of recorded
valuations. Its presentation must not imply live values or that mixed/unknown
valuation dates are all current. Exact date sources and stale thresholds remain
future investment policy work; see the [Inversiones brief](screen-briefs/investments.md).

## Tone and feedback examples

Use short, concrete Spanish, neutral facts and a useful next step. For an overall
overrun: **Excedido**, the exact amount above budget, and **Ajustar presupuesto**.
The owner confirmed consistent signed remainders: an actual overall budget overrun
of 80.000 displays **−80.000** in semantic red under **Restante del presupuesto**,
with **Excedido**. Category deficits use the same signed/red treatment;
a category overrun does not change a healthy overall status. Positive remaining
amounts use the normal ink with a small positive stamp. Pair color with exact
signed money and text; keep the rest of the screen neutral. An example suggestion is: “Transporte superó su límite en tres de los
últimos cuatro meses. Podrías revisar su presupuesto.” Its period is illustrative,
not an approved recommendation threshold. If a goal falls behind, offer contribution,
target-date or spending-plan adjustments while recognizing what is already saved.

## Open implementation decisions

The [step 2 financial decision record](../decisions/0012-budget-guidance-contract.md)
collects concrete alternatives, synthetic worked examples and the owner's partial
confirmations. It does not yet define a complete replacement for delivered calculations.

- Complete the D03 replacement and its worked current/past/no-limit/zero/overrun
  examples. BG1 confirms uplifting positive wording (Al día as working copy),
  neutral Al límite, Excedido and Sin límite for the presented states.
  BG2 confirms a positive headline after reservations, lower-priority reserved
  amount and neutral Sin margen when reservations leave zero free margin.
  The presented commitment shortfall uses Revisar, the exact budget gap and
  zero daily allowance, independently of income. An equal reserved-to-recorded
  payment leaves the headline unchanged and removes reservation context when none
  remains. Other precedence and boundary cases still need review; no new
  pace/percentage thresholds are approved.
- Define upcoming-commitment reservation exactly once, its period boundaries and
  relation to recorded expenses; preserve exact money, day inclusion and rounding
  unless separately changed. Expected recurring income informs a clearly labeled
  plan and is replaced once by its recorded occurrence. Define its detailed
  occurrence/month-planning lifecycle without implying a verified account balance.
- Design allocation advice around the confirmed category-only rule: assigning income
  requires a category-budget change. Recording a contribution only updates goal or
  investment tracking. Keep the suggested saving action honest about these effects.
- Specify insight eligibility, repetition and insufficient-history handling; review
  invitation timing; goal-default ties, completed/archived states and removed pins;
  and milestone presentation, replay and reduced-motion behavior.
- Refine investment performance/distribution and the final navigation/layouts when
  their capabilities are scoped. Keep essential detail accessible for experienced
  users and the overview understandable for newcomers.

## Review a new screen

State its purpose, primary answer and obvious next action. Identify which information
supports an actual decision, which controls are essential, and which interruptions
earn their cost. Inspect keyboard-open, large-font and constrained-window layouts,
as well as loading, genuine empty, failed, stale, busy and restored states. Verify
that suggestions are supported by the available history and that exact financial
meaning survives the visual simplification. Extend actual catalog examples when
shared components change.
