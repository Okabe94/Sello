# Sello

A new local-only Android financial application, modernizing the legacy app through
a deliberately smaller, tested MVP and the Sello visual language.

## Start here

- [Current app experience baseline](docs/development/current-app-experience.md):
  the implemented screens, navigation, behavior, adaptive layout and component
  families to use as the starting point for UI/UX work.
- [Current navigation graph](docs/development/navigation-graph.md): the visual route
  map, screen composable hierarchy and navigation test coverage.
- [Architecture and guardrails](ARCHITECTURE.md): ownership, financial/recovery
  contracts, technologies, MVP scope and developer tooling requirements.
- [MVP Kanban board](docs/planning/mvp/BOARD.md): canonical implementation status
  and scoped tickets.
- [Post-MVP roadmap board](docs/planning/mvp/ROADMAP.md): signed carryover (seven
  dependency-linked tasks) and legacy data import (one), separate from MVP release acceptance.
- [Delivery workflow and gates](docs/planning/mvp/README.md): dependencies,
  definition of done, evidence and Jira handoff.
- [Independent executor guide](docs/planning/mvp/EXECUTION_GUIDE.md): prerequisites,
  file ownership, tools, verification procedures and source library.
- [Time and composition](docs/development/time-and-composition.md): the three clocks,
  the financial zone, the Koin composition root and who cancels what.
- [Financial storage](docs/development/storage.md): the Room tables, what the database
  and the mappers each enforce, and how to change the schema.
- [Commands and receipts](docs/development/commands.md): how a change is saved once,
  replayed safely and recovered after an uncertain result.
- [Categories and monthly limits](docs/development/categories-and-limits.md): how a
  month's limit follows from history, and what archiving does.
- [Monthly snapshot](docs/development/monthly-snapshot.md): the one source of a
  month's spent, remaining, allowance, forecast and verdict.
- [App shell](docs/development/app-shell.md): the frame, the shared month, back,
  restoration and entry links.
- [First run and the category editor](docs/development/category-editor.md): the
  form's rules, saving once, and what it hands back.
- [Anotar](docs/development/expense-entry.md): recording an expense, the preview,
  saving once and the receipt.
- [Quality flow](docs/planning/mvp/QUALITY_FLOW.md): `./scripts/verify-ticket`, the
  evidence every ticket needs and what CI enforces.
- [Local visual reference](docs/design/README.md): Sello snapshot and provenance.
- [Inspected baseline](docs/planning/mvp/BASELINE.md): what actually builds today.

The app currently implements Recibo and Resumen tabs, expense and income
entry/history, category and limit management, corrections with guarded undo,
persisted appearance/order settings, adaptive navigation and unreadable-storage
startup recovery. The component catalog is populated. Backup/restore/reset and the
live-testing sandbox are not yet customer-facing capabilities; see the current app
baseline and board for the exact boundary.

## Local host verification (G1)

Use the installed Android SDK and Android Studio JBR, or the validated replacement
matrix documented by SELLO-002:

```bash
ANDROID_HOME=/home/okabe94/Android/Sdk JAVA_HOME=/opt/android-studio/jbr \
  PATH="/opt/android-studio/jbr/bin:$PATH" \
  ./scripts/verify-ticket SELLO-0XX
```

Replace `SELLO-0XX` with the ticket being worked on. The command runs that ticket's
gate (board checks, architecture rules, ktlint, unit tests, lint and assembly for
G1) and writes a report under `build/reports/quality/`.

Build/dependency/IDE caches, machine-local configuration and signing secrets are
not tracked. No Git remote or hosted Jira project is configured by this setup.
