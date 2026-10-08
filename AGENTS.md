# Sello project instructions

Read `ARCHITECTURE.md` before planning, implementing, or reviewing project changes.
It defines module boundaries, financial/recovery contracts, technology defaults,
MVP scope, and MUST/SHOULD/COULD rules for this new application.

Implement complete vertical slices within the active increment. Do not introduce
deferred capabilities or legacy scaffolding implicitly. Record architectural
deviations in a short ADR and update the guardrails when a decision changes.

Extend debug-only live-testing scenarios and the separate UI component catalog
with the features/components they cover. Follow `ARCHITECTURE.md` §9: use real
financial paths in an isolated sandbox, reuse actual design-system components,
and keep developer tooling, fixtures, and controllable clocks out of release.
