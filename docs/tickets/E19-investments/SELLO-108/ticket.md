# SELLO-108 — Carry investments through backup, restore and erase

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-107, SELLO-004
- **Gate:** G2

### Outcome
Backups include investments and rates; restore and erase handle them.

### Deliverables
Format additions with docs; restore and reset updates.

### Acceptance criteria
- Older files still restore; new ones round-trip exactly, including decimal rates.

### Tests
Codec and round-trip tests; G2.

### Working checkpoint
A backup with investments restores with the same figures.

### Context and starting points
`docs/data/backup-format.md`.

### Implementation plan
1. Format.
2. Restore and reset.
3. Tests.

### Concrete cases and pitfalls
- Rates as exact decimals.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-108 --retain`.
