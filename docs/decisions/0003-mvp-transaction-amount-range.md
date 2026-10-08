# 0003 — One MVP transaction amount range

**Status:** Accepted product direction · **Date:** 2026-10-08
**Owner/approver:** project owner (user) · **Ticket:** SELLO-001

## Context and decision

D05 originally limited manual entry to 12 digits while allowing larger positive
Long amounts from backups. During the M01–M05 walkthrough, the owner confirmed
input rules but rejected that exception: unsupported default amounts should not
be supported through backup. No financial persistence exists in the scaffold.

- **MUST** enforce 1–999,999,999,999 COP for each expense/income at entry, command,
  storage-decoding and portable-file boundaries. Shared domain validation owns the
  rule; a UI-only check is insufficient.
- **MUST** reject an out-of-range transaction in a backup before any replacement;
  no truncation, saturation, skipped-record import or partial success.
- **MUST** preserve valid amounts exactly, including the maximum, during edits.
- **MUST** distinguish per-transaction bounds from checked Long aggregate totals.
  Two valid 600,000,000,000 expenses total 1,200,000,000,000 legitimately; reject
  actual aggregate overflow, not a sum merely exceeding the record maximum.

This supersedes the earlier wider-backup transaction allowance, not exact-money
arithmetic. It does not change category-budget policy, add currencies or introduce
an account ledger. Any future range expansion requires explicit policy approval
and persistence/portable-format compatibility review.

## Alternatives and consequences

Rejected: a wider import-only range, silently clipping values, dropping invalid
records or bounding all money values to the keypad cap. The supported transaction
range is predictable and identical across paths; oversized external files cannot
be restored. Existing malformed storage must surface a typed read failure, not
zero/empty history or destructive repair.

## Verification deliverables

SELLO-010/011/015/021 enforce/test the same bound; SELLO-028/030 prove complete
backup validation and nonmutating rejection with real storage. Cover 0, 1, maximum,
maximum+1, oversized paste, valid maximum on note edit and aggregate values above
the record cap. M05/M06 are expected cases, not passing implementation tests.
