# 0008 — When the data cannot be opened: a screen, a retry and a kept-aside start over

**Status:** Accepted product direction · **Date:** 2026-10-11
**Owner/approver:** project owner (user) · **Ticket:** SELLO-045

## Context

A database that could not be opened at start crashed the app on every launch
(reproduced on API 30 with a damaged file). The architecture forbids a destructive
fallback and D09 defines reset as counted, twice confirmed and typed. Neither says
what a person can do when the data cannot be read at all, and restore and reset
(SELLO-030, SELLO-031) do not exist yet.

## Decision

- **MUST** show a screen instead of crashing: it says the data could not be opened
  and that nothing was deleted, and offers **Intentar de nuevo** and **Empezar de cero**.
  The financial graph is built only on data that opened; the screen needs none of it.
- **MUST** keep one message for every cause. The failure carries only an error type
  name, and a damaged file and one written by a newer version look alike, so the
  screen does not guess. Telling them apart needs a typed failure and its own decision.
- **MUST**, for Empezar de cero, **set the unreadable file aside, not delete it**:
  it is moved with its companion files to `sello-unreadable.db` in the same private
  folder and the next open starts an empty history with the device zone. One kept
  copy exists; a later start over replaces it. The reason is the indistinguishable
  newer-version case, where the data is intact and deleting it would destroy it.
- **MUST** confirm in two steps with the typed word **BORRAR**, as D09 does. Counts
  are not shown because the data cannot be read.
- **MUST** refuse both actions once the data has opened: a second store is never
  opened beside one in use and readable data is never set aside.
- **MUST** start the empty history in a new task, so nothing saved by the previous
  screens (a selected category, a draft and its operation identifier) is restored
  onto it.
- Startup stays synchronous. The ticket proposed opening storage asynchronously
  with an initializing state; the owner chose not to, because the main-thread wait
  is one small read that nobody has measured as a problem, and E11 does not turn
  unmeasured concerns into fixes. Revisit with a measurement when the first slow
  migration exists.

## Consequences

- This is the one place where the app starts empty over existing data without
  restore or reset. It is not a silent fallback: it happens only after the typed
  confirmation, and the file is kept.
- The kept copy is private plaintext financial data that no screen can reach yet.
  **SELLO-030** must make restore reachable from this screen and decide whether the
  kept copy can be recovered; **SELLO-031** must erase it on reset; **SELLO-032**
  must count it among the private files it verifies.
- Unexpected errors that are not storage failures still stop the app; they are not
  dressed up as unreadable data.

## Verification

`SetAsideTest`, `StartupTest`, `StartupFailureViewModelTest` (host);
`UnreadableFileSetAsideTest`, `StartupFailureScreenTest` (API 30); and the launch,
retry and start-over journey recorded in SELLO-045's Delivery evidence.
