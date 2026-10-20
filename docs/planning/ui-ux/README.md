# Sello UI/UX audit package

This directory preserves the evidence and analysis behind the owner's
[`ui_issues.md`](../../../ui_issues.md) inventory. The redesign epic first proposed
from it was not adopted: Sello v2 ([ADR 0013](../../decisions/0013-sello-v2-design.md))
and its epics [E12](../mvp/epics/E12-v2-recibo.md) and
[E13](../mvp/epics/E13-v2-current-app.md) replaced it. It is an observation package,
not proof that any issue has been fixed or that an earlier product decision has been
reversed.

## Baseline and method

- **Repository revision:** `4559e0b` (`docs: map current app experience and navigation`)
  on branch `docs/current-app-navigation`, with this package and the owner's
  untracked `ui_issues.md` outside that revision.
- **Reviewed change:** merged [pull request 47](https://github.com/Okabe94/Sello/pull/47),
  especially the current-experience and navigation-graph documentation it added.
- **Artifact:** locally assembled `app-debug.apk`, SHA-256
  `881a06995bfe6f47cf946cdc9ba99a4732bb1f474e9b177b9efa59ec9f8beec6`.
- **Device:** isolated Android API 30 emulator, 1080×2340 px at density 440,
  package `com.software.sello.debug`.
- **Date:** 2026-10-15, with the emulator in the `America/Bogota` context.

The audit used only synthetic financial data. Some category names were joined by
the scripted input (`SaludCasa`, `EducacionMascotas`); those strings are capture
artifacts and are not proposed product copy. The package contains no personal
financial history, credentials or signing material.

The audit is intentionally conservative. A screenshot demonstrates only the state
shown. A recording is not treated as proof of a timing defect unless the defect is
visible in the recording. XML hierarchy captures accompany still screenshots where
available so visible presentation can be compared with accessibility semantics.

## Package contents

- [`reports/2026-10-15-ui-ux-audit.md`](reports/2026-10-15-ui-ux-audit.md) maps
  every item in `ui_issues.md` to its observed status, evidence, governing contract
  and where it stands in E12 and E13, including the findings no ticket covers yet.
- [`evidence/20261015/`](evidence/20261015/) contains 47 numbered capture artifacts
  (approximately 8 MiB): PNG screenshots, UIAutomator XML hierarchies, MP4 interaction
  recordings and two contact sheets. [`SHA256SUMS`](evidence/20261015/SHA256SUMS)
  makes the retained bytes independently verifiable.

## Evidence conventions

The numeric prefix records the audit sequence, not severity. Related suffixes such
as `09a`/`09b` are before/after states. Contact sheets ending in `-frames.png` are
sampled from the adjacent MP4 and should be reviewed together with the recording.
XML bounds use physical emulator pixels.

The classifications in the report mean:

- **Reproduced:** the audit directly observed the material behavior or presentation.
- **Partly reproduced:** related behavior is visible, but the report's strongest
  wording was not established.
- **Decision:** the item asks for a product/design choice rather than describing a
  falsifiable implementation failure.
- **Contract conflict:** adopting the request as written would change an approved
  architecture, ADR or visual-reference contract; an explicit decision must precede
  implementation.
- **Not reproduced:** the controlled attempt did not show the reported behavior.
- **Not separately exercised:** the audit lacks direct evidence for that specific
  variant; it remains a verification case, not an established defect.

These labels can be combined. They are triage, not a claim of priority, acceptance,
independent review or completion. They describe evidence, not the kind of requested
work.
