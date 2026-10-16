# Screen and interaction briefs

**Step 3 · Working product handoff · 2026-10-15**

The owner accepted the prototype's conceptual direction and asked to continue
through detailed notes. The final concrete design was then made as
[Sello v2](../v2/README.md) ([ADR 0013](../../decisions/0013-sello-v2-design.md)); these
briefs keep the intent and the open policy behind it.
These briefs carry the intended information, hierarchy, behavior and tone into that
handoff. Approval of written product direction is distinct from final visual design
or implementation approval.

## Review record

On 2026-10-15, after reading the four screen summaries in the conversation, the
owner answered **“Approved, let's continue.”** This approves the presented Home,
expense/income entry, Resumen and income notes as product/interaction direction.
Recommendations remain design guidance and explicitly open questions remain open;
this is not blanket approval of unseen financial policies or final layouts.

| Brief | Review state |
| --- | --- |
| [Home / Recibo](home.md) | Review summary approved; listed open cases remain. |
| [Expense and income entry](entry.md) | Review summary approved, including conditional pad direction and a note dialog; native feasibility and listed open cases remain. |
| [Resumen and income](summary-and-income.md) | Both review summaries approved; detailed comparison/advice/planning policies remain open. |
| [Metas](goals.md) | Earlier interview direction, optional dates, history-preserving withdrawals, current-progress milestones and re-crossing celebrations, manual completion (v2) and celebratory Completadas confirmed; detailed lifecycle questions remain future work. |
| [Inversiones](investments.md) | Earlier interview overview direction and quiet valuation-age context confirmed; initial future-feature brief, with detailed financial mechanics still later work. |

## Consolidated handoff checkpoint

Initial written briefs now cover all the discussed destinations and the shared
recording journey. The product direction and explicitly confirmed answers can be
handed to the designer together. Their recommendations and open cases remain
labeled; coverage is not blanket approval of every paragraph or implementation.

- **Preserve:** the working motto, screen responsibilities, information priorities,
  direct primary actions, stable entry, restrained semantic color, goal celebrations,
  category-only allocation and independent goal/investment tracking.
- **Designer owns:** final layouts, spacing, typography scale, component composition,
  charts, adaptive treatment, final copy and concrete celebration presentation.
- **HTML exploration:** optional context only. Its exact card arrangement, dimensions,
  fixed dates and synthetic fixtures are not the final design.

| Follow-up work | Boundary before affected implementation |
| --- | --- |
| Home and recurring payments | Finish status precedence, zero/past cases, reservation lifecycle and independent boundary examples. |
| Entry | Resolve note-editor Back/dismissal/restoration and validate actual pad/IME/inset/accessibility behavior. |
| Resumen and income | Define comparison coverage, recommendation eligibility/frequency and expected-income planning/matching. |
| Metas | Define withdrawal bounds/corrections, exact milestone thresholds, target edits, completion reopening and Home-pin lifecycle. Preserve the confirmed ordinary completion and re-crossing behavior. |
| Inversiones | Later review of return periods, dated cash flows, valuation dates/sources, movement corrections, dividends and currency/rate policy. Visible valuation age does not settle those mechanics. |

These are follow-up inputs, not permission to reopen the answers already recorded.
Independent product/domain planning can continue while visual design is developed;
final screen implementation needs its applicable reviewed design and policy inputs.

The [HTML exploration](../proposals/index.html) is an optional illustration of
some ideas. Its screenshots, dimensions, card counts, chart types and control
placement are not implementation requirements. Further visual polishing of it is
not a prerequisite for continuing product definition.

## How to continue step by step

1. **Document each screen's job and behavior.** Start with
   [Home](home.md), [expense/income entry](entry.md), then
   [Resumen and income](summary-and-income.md). These drafts consolidate the
   interview; they do not require a new visual sign-off after every answer.
2. **Review remaining product questions one screen at a time.** Add concrete
   scenarios where an answer changes a decision or interaction. Keep confirmed
   intent, recommendations and unanswered questions visibly distinct. Do not
   reopen settled answers because the HTML used a different arrangement.
3. **Carry forward the recorded Metas/Inversiones decisions.** The current briefs
   include the follow-up answers. Preserve their future scope and keep the remaining
   detailed lifecycle/return/FX work explicit rather than forcing it into this
   initial design-direction handoff.
4. **Hand the briefs to the designer.** Done: Sello v2 develops layouts, component
   composition, spacing, final copy and adaptive states. Evaluate those designs
   against the briefs and the existing Sello identity. Product direction approval
   does not approve a mockup, and technical checks do not approve a visual design.
5. **Plan scoped implementation from the agreed behavior.** Financial examples,
   domain work and other independent prerequisites can progress while visual
   design is refined. Affected screen implementation needs its reviewed design
   and applicable financial/technical inputs; the exploratory HTML cannot supply
   missing approval. Existing app journeys stay usable during the transition.

This is a design workflow, not a new ticket board. It creates no tickets, delivery
status changes, assignments, contact with a designer or release-scope expansion.

## What every brief records

| Part | What it settles |
| --- | --- |
| User question and outcome | What the person should understand or accomplish. |
| Information hierarchy | What gets attention first, what supports it, and what can be optional. |
| Interaction | Entry points, primary actions, direct access, completion, Back and draft behavior. |
| Visual and emotional direction | Existing identity, tone, semantic color and what should feel calm or celebratory. |
| States and concrete scenarios | Loading, empty, error, stale, busy and relevant financial examples; these describe desired observable behavior, not executed tests. |
| Designer discretion | Layout/composition choices that remain deliberately open. |
| Open decisions and scope | Unresolved product/financial meaning, dependencies and future capabilities. |

## Authority and decision labels

- **Confirmed intent** records the owner's answers. Preserve it when designing.
- **Recommendation** is a proposed way to achieve that intent; it can change
  during review without being misrepresented as an approved requirement.
- **Open** needs an answer or a worked example before dependent implementation.
- **Designer discretion** covers visual choices within the agreed intent. If a
  layout cannot meet the behavior, raise that tradeoff rather than silently
  changing the product requirement.

The [philosophy](../philosophy.md) owns the common direction.
[ADR 0011](../../decisions/0011-goal-centered-design.md) records supersessions and
this handoff boundary. [ADR 0012](../../decisions/0012-budget-guidance-contract.md)
owns the replacement financial definitions and their approval status;
[architecture](../../../ARCHITECTURE.md) owns safety and technical contracts.
Summarizing them here does not approve unresolved financial cases. The
[current app baseline](../../development/current-app-experience.md) describes
delivered behavior, not the target.

Keep receipt paper, stamps, bold exact numbers, category circles, bundled fonts,
and Cobalto/Violeta light/dark identity. Follow the unchanged
[v1.1 reference](../README.md#preserved-v11-visual-reference) for unaffected visual
foundations. The designer finalizes how those foundations express the new intent.

## Review checkpoints

Use the written scenarios to check whether the proposed design communicates the
right amount, exposes the important actions and preserves the intended journey.
Review small screens, large text, keyboard/dialog states, reduced motion and
non-color equivalents. Do not inherit the HTML's synthetic limits, fixed dates,
four example categories, 216px pad height or illustrative insight threshold.
Native Android keyboard/inset, accessibility and restoration checks remain
implementation/device work. Batch technical gates at meaningful documentation
checkpoints; another interview answer is not itself a request to rerun them.
