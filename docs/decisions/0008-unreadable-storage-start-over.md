# 0008 — Starting over when storage cannot be opened

**Owner:** project owner (user) · **Updated:** 2026-10-16
**Status:** approved recovery rules; retry and start-over are implemented.

Show a recovery screen when storage cannot be opened. Say that nothing was deleted
and offer **Intentar de nuevo** and **Empezar de cero**. Build the financial graph
only after storage opens; this screen must work without it.

Use one general storage-failure message. The current failure cannot distinguish a
damaged file from one written by a newer app version. Specific diagnoses need typed
failures and a separate decision. Do not disguise unrelated errors as storage failures.

## Starting over

Require two confirmations and typed **BORRAR**. Show no record counts because the
data cannot be read. Move the unreadable database and its companion files to
`sello-unreadable.db` in the same private folder, preserving the data instead of
deleting it. Keep one copy; a later start-over replaces that copy.

The next open creates an empty history with the device financial zone. Start a new
Android task so old selections, drafts and operation IDs cannot return. Refuse
retry and moving files once storage has opened; never open a second store beside it.

Startup remains synchronous. Revisit that choice using measurements when startup
or migration performance warrants a change.

## Remaining recovery work

The kept copy is private plaintext financial data with no recovery screen yet.
SELLO-030 must make backup restore reachable from this screen. Decided with the
owner on 2026-10-22 (SELLO-030): the kept copy is never recovered by Sello; a restore
from this screen sets the unreadable file aside, as starting over does, and fills the
new store from the backup. SELLO-031 must erase it on reset; SELLO-032 must
include it in private-file checks. See the
[recovery epic](../planning/mvp/epics/E07-recovery-privacy.md).

## Verification

Host: `SetAsideTest`, `StartupTest`, `StartupFailureViewModelTest`.
Device: `UnreadableFileSetAsideTest`, `StartupFailureScreenTest`.
