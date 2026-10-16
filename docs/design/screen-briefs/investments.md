# Inversiones brief

**Initial future-feature brief · Earlier interview direction and overview valuation-age
context confirmed; final visual design: Sello v2; detailed performance/ledger
policies remain open. Recommendations retain their stated review status.**

> **Designed in [Sello v2](../v2/README.md):** [Inversiones](../v2/inversiones.html). Where this brief and v2 differ,
> v2 and [ADR 0013](../../decisions/0013-sello-v2-design.md) apply; the open policies below stay open.

Use the [brief workflow and decision labels](README.md). The first question is
“How are my investments doing?” Newcomers should understand the overview, while
experienced users can reach the supporting detail without an intimidating first
screen. The owner explicitly described this as a starting point needing refinement.

## Confirmed intent from the interview

- Lead with total portfolio value, not only contributed capital or gain.
- Show a gain/loss percentage with a directional arrow and green/red meaning,
  accompanied by a sign and readable text.
- Below that answer, distinguish invested capital from the monetary gain/loss.
- Provide a view of investment distribution, contributions, withdrawals and
  dividends as the initial supporting scope to refine.
- Keep investments independent from goals, including when the user tracks the
  same money in both. Neither record independently allocates income.
- Use user-entered information; do not imply a bank connection or verification
  that the app does not have.
- Make the age of recorded valuations visible on the overview as quiet supporting
  context. Users should be able to distinguish recent values from older ones
  without opening each holding's detail.

Inversiones has a distinct working destination when implemented. Metas does not
have to live behind it. The eventual navigation remains subject to the agreed
maximum of four working main destinations, with three preferred if equally clear.

## Recommended hierarchy and interaction

1. Portfolio value as the dominant exact amount.
2. Clearly identified performance percentage and direction, with its period and
   value date/context once those policies are defined.
3. Contributed capital and monetary gain/loss, so a person can understand what
   produced the headline without doing the calculation themselves.
4. Distribution across investments, with accessible exact values and direct access
   to each holding's detail.
5. Contribution, withdrawal and dividend history in a place appropriate to its
   context, rather than many competing summary cards.

This is a proposed reading order, not a fixed component stack. Keep the main answer
short and useful, then make deeper detail discoverable. The designer can decide
which supporting information is inline and which belongs in investment detail.
Do not hide essential facts behind unexplained gestures or chart-only marks.

**Recommendation:** make adding an investment and recording its relevant movements
obvious within the investment journey. Specific movement forms, editing/deleting,
valuation updates and any accrual/rate workflows need their own scoped definition;
this brief does not import all the reference's controls into the app.

## Financial meaning and truthful presentation

The owner's simple example is **100 contributed → value 110**, with **+10%** and
an upward green arrow. Supporting amounts are contributed 100 and gain 10. For a
loss of 10 under the same no-cash-flow assumptions, the illustrative counterpart
would show value 90 and −10% with a downward red arrow; that is a proposed review
example, not a separately approved investment calculation.

Adding capital alone is not investment gain. A withdrawal must not automatically
be described as poor performance. The percentage must eventually use the reviewed
financial policy; never derive it from a bare change in portfolio value when
contributions or withdrawals also changed that value.

Architecture currently specifies simple gain using current value, withdrawals,
dividends and contributions, not IRR. It also distinguishes recorded value, accrual
and benchmarks. The v1.1 reference contains different dividend treatment and FX
presentation; its formulas/sample numbers are not authority over architecture.
This brief introduces no new return formula, currency/rate source or time horizon.
Review the full dated-cash-flow examples before implementation.

If values, prices or exchange rates cannot support a complete total, explain the
unavailable/stale state. Do not render a partial portfolio as complete or a failed
read as zero. The headline is recorded portfolio value, not independently verified
wealth; goals must not be added to it as another asset class by this redesign.

The owner accepted visible valuation age after being asked specifically about
overview freshness context. The designer chooses the concise presentation. The
meaning/source of valuation dates and any stale threshold remain future policy
work. Mixed or unknown dates must not become a claim that every holding was valued
at the latest update time; recording a movement alone is not proof of a fresh
valuation. This choice does not authorize automatic pricing or a refresh service.

## Tone and visual direction

Keep the main portfolio value in the normal ink. Apply restrained green/red to
signed performance, with the arrow and text carrying the same meaning. A negative
return is factual information rather than an opportunity for a reprimand.

Use plain labels and optional explanations for unfamiliar concepts. Advanced
supporting detail can be deeper while the main answer and essential actions remain
easy to find. No universal investment target or investment recommendation is
introduced by the budgeting philosophy.

## Scenarios for later design review

| Situation | Intended presentation and approval boundary |
| --- | --- |
| Contributed 100, value 110, no other cash flows | Owner example: total 110, +10%, upward green arrow; capital 100 and gain 10 underneath. |
| Capital is added without a change in investment performance | Distinguish the contribution from return; review exact examples with the ledger policy. |
| Performance is negative | Signed negative return, downward direction and red plus readable context; do not invent a classifier from portfolio-value movement alone. |
| Return percentage has no valid denominator | Show an understandable unavailable percentage; architecture treats zero contributions as undefined, not 0%. |
| No investments | Recommend a welcoming empty state with a direct create action and no invented chart. |
| Values are missing/stale or reading fails | Make the limitation explicit; never claim a complete fresh portfolio or substitute zero. |
| The total uses older recorded valuations | Make their age visible on the overview at lower priority; do not imply live values or uniformly recent valuations. |
| One contribution is also recorded against a goal | Keep both tools independent and leave category-only income allocation unchanged. |

## Open decisions and scope

Detailed return periods, valuation entry/source, dated cash flows, dividend handling
in each readout, movement correction, distribution basis and multi-currency/rate
behavior remain scoped future work. No annual-rate or benchmark convention is
chosen. The overview can be refined after Metas without demanding those technical
decisions from the owner in this design interview.

The designer owns exact composition, chart choice, performance layout, explanations
and adaptive presentation within these meanings. Inversiones remains in the Later
increment, separate from goals in Next. No routes, investment ledger, FX services
or dormant data models are authorized by these notes.

Sources: [philosophy](../philosophy.md), [ADR 0011](../../decisions/0011-goal-centered-design.md),
[allocation boundary](../../decisions/0012-budget-guidance-contract.md#bg4--category-only-income-allocation-independent-tracking)
and [architecture](../../../ARCHITECTURE.md).
