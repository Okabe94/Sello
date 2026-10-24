# SELLO-086 — Approve goal rules and worked examples

- **Revision:** the gate tested commit `bc9d45f` on `sello-086-goal-rules`, on `main` at
  `4b2bbc5`; the only uncommitted file was the
  retained report. The status and this section are added to that commit afterwards.
- **Requirement mapping:** every ADR 0017 and ADR 0013 Metas decision has an example:
  contribution (MT01–MT06), withdrawal never above saved (MT07–MT10), stamps at 25, 50,
  75 % and Meta (MT01, MT04, MT05, MT19), target edits (MT17–MT19), corrections with
  undo (MT13–MT16), past dates (MT20–MT22), completion with Deshacer (MT23–MT26),
  deletion (MT27) and Recibo's goal (MT28–MT33). Milestone replay is rule 4 with MT11 and
  MT12. Open points were put to the owner; four smaller ones are listed as proposals.
- **Red / Green:** not applicable: no behaviour changed and no test was added; the
  examples are rules for SELLO-087–094 (G1).
- **Tests:** none changed. Every figure was worked out by hand and rechecked with a
  throwaway calculation outside the repository.
- **Gate results:** `./scripts/verify-ticket SELLO-086 --retain` passed all six G1
  checks. Host: 750 tests, zero failures, errors or skips; lint zero errors and 34
  warnings, as on `main`.
- **Quality run:** run 20261024T030500Z-52414ebf; SELLO-086 G1 passed; HEAD bc9d45f, inputs sha256 5bb9ff012656; report docs/tickets/E17-goals/SELLO-086/quality-report.json sha256 e57f4c7079466c7def4481c1bbeea4394618c353c06832d3cdab55c1eedc68b7
- **Device / Artifact:** none.
- **Review:** the owner answered the open points on 2026-10-23 and approves the examples
  by merging; executor self-review of every figure.

