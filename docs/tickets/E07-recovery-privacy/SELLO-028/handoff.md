# SELLO-028 — Define portable MVP backups and bounded validation

A versioned plaintext JSON [backup format](../../../data/backup-format.md), independent of Room: one-revision snapshot, preferences captured separately (left out if unreadable), and a strict reader that rejects a whole bad file with where it is and writes nothing. No screen yet: saving is SELLO-029, restoring SELLO-030.
