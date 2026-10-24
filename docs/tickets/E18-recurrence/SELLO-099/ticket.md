# SELLO-099 — Carry schedules through backup, restore and erase

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-098, SELLO-004
- **Gate:** G2

### Outcome
Backups include schedules; restore and erase handle them without replaying occurrences twice.

### Deliverables
Format additions with docs, restore and reset updates.

### Acceptance criteria
- Restored schedules do not re-record occurrences already in the backup.
- Older files still restore.

### Tests
Codec and round-trip tests; G2.

### Working checkpoint
A backup with schedules restores and catches up correctly.

### Context and starting points
`docs/data/backup-format.md`.

### Implementation plan
1. Format.
2. Restore and reset.
3. Tests.

### Concrete cases and pitfalls
- Restore resets catch-up state to the backup's recorded occurrences.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-099 --retain`.
