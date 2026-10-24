# 0006 — Quality tooling and evidence retention

**Owner:** project owner (user) · **Updated:** 2026-10-16
**Status:** approved tooling choices.

## Tools

| Purpose | Choice |
| --- | --- |
| Local and CI gate runner | `scripts/verify-ticket`, Python standard library |
| Ticket evidence | The passed report in each ticket folder, `docs/tickets/<epic>/<ticket>/quality-report.json` (since 2026-10-24, [ADR 0018](0018-ticket-folders.md)); CI artifacts kept 90 days |
| Architecture boundaries | `scripts/quality/architecture.py`, with no rule-engine dependency |
| Formatting | ktlint 1.8.0 through ktlint-gradle 14.2.0; `.editorconfig`, Android Studio style |
| Static analysis | Android Lint and ktlint; Detekt deferred pending stable toolchain compatibility |
| Dependency versions | Strict Gradle locking, one `gradle.lockfile` per module |
| Hosted checks | GitHub Actions for the public `Okabe94/Sello` repository |

Committed ticket reports keep completed work verifiable after CI artifacts expire.
Reports contain tool versions, revision hashes and file names, excluding machine
paths, user names and secrets. CI reruns checks on the merge candidate; a committed
report alone is not trusted proof. Follow the [quality gates guide](../development/quality-gates.md)
for report freshness, retention and completion evidence.

## Limits and maintenance

- The architecture checker reads source text; it is not compiler proof. Its header
  documents coverage and limits, and tests exercise both. Gradle enforces module
  dependencies at compile time. Register new modules in the checker explicitly.
- Lockfiles expose changes to direct and indirect dependency versions, not whether
  those versions are safe. Plugin classpaths are catalog-pinned but not locked.
  After an approved dependency change, run `./scripts/update-dependency-locks` and
  review the diff. Adopting Detekt needs a separate compatibility check.
- Keep gate tasks in `scripts/quality/gates.py` and the tickets README aligned;
  a test checks they agree. G3 is unavailable until SELLO-035 delivers its checks;
  the runner refuses it rather than substituting a lower gate.

## Hosted checks and verification

[ADR 0009](0009-hosted-gate-scope.md), amended by
[ADR 0014](0014-change-aware-quality-gates.md), owns gate selection: G0 for
eligible documentation, G2 with explicit affected device scope for PR code,
G1 for code pushes to main, and full G2 nightly/on request. Default local ticket
runs remain full; `--affected BASE` opts into dependency-aware device selection.
New report schema v2 records that scope; historical v1 reports stay readable.
Completion evidence is checked on every run.
PR and merge permissions are defined in [AGENTS.md](../../AGENTS.md).
Runner, evidence-validator, architecture and workflow tests live in
`scripts/quality/`. Commands and failure recovery are in the
[quality gates guide](../development/quality-gates.md#quality-runner).
