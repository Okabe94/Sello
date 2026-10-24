# SELLO-030 — Implement staged atomic restore and resumable preference completion

Respaldo → Restaurar checks a whole file, previews current against backup counts and replaces the history in one transaction (new generation, backup zone), then applies its preferences, naming any that failed with a retry that never replaces twice; the app restarts. The start-up failure screen offers the same restore, setting the unreadable file aside first.
