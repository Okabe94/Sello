# SELLO-096 — Approve recurrence rules and worked examples

- **Revision:** the gate tested commit `1716b26` on `sello-096-recurrence-rules`, which
  merges `main` at `6ca03d5`; the only uncommitted file was the retained report. The
  status and this file are added after that commit.
- **Requirement mapping:** every ADR 0017 recurrence decision has examples: schedule
  kinds and month ends (RE01–RE09), twice a month vs every 15 days (RE05–RE07), catch-up
  after weeks away (RE10–RE14), reservations with Sin margen and Revisar (RE25–RE34),
  expected income (RE35–RE37), pause and end (RE09, RE21–RE22, RE32), future-only edits
  (RE17–RE20). The rules the SELLO-095 gap list left open are settled: deleted occurrences
  never return (rule 4, RE15), no matching (rule 5, RE16), the two days (rule 2, RE05–RE06),
  catch-up on a day change (rule 6, RE14), archived categories (rule 9, RE23), what counts
  in `Q` and `F` (rules 11 and 13). Catch-up bounds are rule 6 (RE11–RE13); the notice's
  content is rule 14 (RE38–RE39). Open points: none for the owner; seven Proposed rules
  are approved by merging.
- **Red / Green:** not applicable: no behaviour changed and no test was added; the
  examples are rules for SELLO-097–104 and SELLO-114 (G1).
- **Tests:** none changed. Every date and amount was worked out by hand and rechecked
  with a throwaway calculation outside the repository.
- **Gate results:** `./scripts/verify-ticket SELLO-096 --retain` passed all six G1
  checks. Host: 797 tests, zero failures, errors or skips; lint zero errors and 34
  warnings, as on `main`.
- **Quality run:** run 20261024T182824Z-07eb1881; SELLO-096 G1 passed; HEAD 1716b26, inputs sha256 4c86379049f3, changes sha256 e8e83746f448; report docs/tickets/E18-recurrence/SELLO-096/quality-report.json sha256 4bb517abd729f6718068fa50af5e5062b80184ad7d3f985d44723e3c761b7bb2
- **Device / Artifact:** none: documentation only.
- **Review:** the owner answered the four open points on 2026-10-24 and approves the
  examples by merging; executor self-review of every figure.
