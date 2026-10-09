# 0006 — Quality tooling and evidence retention

**Status:** Accepted · **Date:** 2026-10-08
**Owner/approver:** project owner (user) · **Ticket:** SELLO-004

## Decision

| Concern | Choice |
| --- | --- |
| Gate runner | `scripts/verify-ticket`, Python standard library only, same entrypoint locally and in CI |
| Evidence | Passing report committed under `docs/planning/mvp/quality-reports/`, plus CI artifacts kept 90 days |
| Architecture rules | In-repo text checker `scripts/quality/architecture.py`; no rule-engine dependency |
| Formatting | ktlint 1.8.0 through ktlint-gradle 14.2.0, Android Studio style, `.editorconfig` |
| Static analysis | Android Lint and ktlint now; **Detekt deferred** |
| Dependency review | Gradle dependency locking, strict mode, one `gradle.lockfile` per module |
| Host | GitHub (`Okabe94/Sello`, public), GitHub Actions, protected `main` |
| Device tests in CI | Every merge candidate runs G2 on an API 30 emulator |

## Reasons and alternatives

- **Committed reports rather than CI artifacts alone.** GitHub deletes artifacts
  after 90 days, and the quality flow requires old Done tickets to stay verifiable
  from a fresh checkout. A committed file can be edited by hand, so it is not
  trusted alone: CI re-runs the gate on the merge candidate and the merge rule
  blocks a failing run. The report holds tool versions, commit hashes and file
  names; no machine paths, user names or secrets.
- **Own architecture checker rather than a library.** The ticket requires the
  checker's coverage and limits to be stated. Both are written in the script header
  and exercised by its tests. It is a text check, not compiler proof; Gradle's
  classpaths remain the compile-time enforcement of module edges.
- **ktlint 1.8.0, not 2.0.** 2.0 is alpha. 1.8.0 predates Kotlin 2.4.20, so it was
  probed first: it runs on all five modules with AGP 9.3.3 built-in Kotlin and
  Gradle 9.5.0 and parses the current sources. It prints a JDK 25
  `sun.misc.Unsafe` deprecation warning from its bundled Kotlin; results are unaffected.
- **Detekt deferred.** The latest stable, 1.23.8 (February 2025), is built against
  Kotlin 2.0.21, Gradle 8.12 and AGP 8.8, the same kind of mismatch ADR 0005
  rejected for KSP. 2.0.0-alpha.6 is built against Kotlin 2.4.10, Gradle 9.6.1 and
  AGP 9.3.1 but is a prerelease. The architecture document lists Detekt as SHOULD.
  Revisit when a stable 2.x supports this toolchain; adopt it through its own probe.
- **Lock files for dependency review.** Every resolved version, including indirect
  ones, is recorded per module. A dependency change that is not written to the
  lock fails the build, and a refreshed lock shows the full change in the pull
  request diff. The owner chose this alone over GitHub's dependency review, which
  adds vulnerability and licence checks but needs an upload step with write
  permission. Limits: locks say that something changed, not whether it is safe;
  Gradle plugin classpaths are pinned in the version catalog but not locked.
- **G2 on every merge candidate.** Chosen over scheduled or local-only device runs
  so each G2 ticket has device evidence for the exact code being merged. Cost is
  several minutes per run; Actions is free for a public repository.

## Consequences

- Changes reach `main` only through a pull request with a passing `quality` check.
- New modules need an explicit rule in `architecture.py`; the checker fails otherwise.
- G3 has no executable profile yet. The runner refuses it until SELLO-035 delivers
  its checks; a lower gate cannot stand in.
- After a deliberate dependency change run `./scripts/update-dependency-locks` and
  review the lockfile diff. It needs no device.
- Adding or changing a gate task means editing `scripts/quality/gates.py` and the
  gate block in the board README together; a test fails if they differ.

## Verification

Runner, validator, architecture and gate tests under `scripts/quality/`; negative
fixtures and hosted results are recorded in SELLO-004's Delivery evidence.

Sources consulted 2026-10-08: [Detekt releases](https://github.com/detekt/detekt/releases),
[ktlint releases](https://github.com/pinterest/ktlint/releases),
[ktlint-gradle releases](https://github.com/JLLeitschuh/ktlint-gradle/releases),
[GitHub artifact retention](https://docs.github.com/en/actions/how-tos/manage-workflow-runs/remove-workflow-artifacts).
