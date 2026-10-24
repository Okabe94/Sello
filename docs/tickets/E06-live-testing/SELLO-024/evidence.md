# SELLO-024 — Create isolated sandbox sessions and financial time controls

- **Revision:** tested snapshot is the `sello-024-sandbox-time-controls` working tree on
  `main` at `03bb458`, uncommitted; it is identified exactly by the retained input
  fingerprint below. The commit and pull request follow this evidence; no claim is
  made that the gate tested a clean commit.
- **Decisions:** the owner chose, on 2026-10-22: a switch restarts the process; one
  sandbox beside the normal debug profile; a separate debug-only launcher activity
  with a badge; play at one day every 2 real seconds, stopping after 31 days or at a
  chosen date. Deviation: the badge sits in the status-bar strip so it covers no
  control, and Android gives touches there to the system, so it cannot be tapped
  with a finger (TalkBack can activate it); the tools open from their own launcher.
- **Requirement mapping:** `VariantComposition` (main) is the only hook; the release
  source set returns `ProductionComposition`, the debug one `DebugComposition`.
  `StorageNames` selects `sello.db`/`sello-preferences` or `sandbox.db`/
  `sandbox-preferences`; the session (active, date, changed, pending wipe) is kept in
  `sandbox-session` preferences. Every financial command runs through a `WriteFence`;
  enter, leave and reset close it (running writes finish, new ones never start),
  save the choice and restart the process, so no screen or action crosses profiles.
  `SandboxTime` drives `ControlledFinancialClock`: +day, +month, jump, bounded play
  and pause; dates persist, and once a change is saved an earlier date is refused
  until a confirmed reset, which erases only sandbox files at the next start
  (`eraseFinancialStorage`, debug-only, refuses the normal names). Audit and
  monotonic clocks stay real. `ToolsActivity` (debug manifest, "Sello Herramientas")
  and `SandboxBadges` show the session. `:app:assembleRelease` is finalized by
  `verifyReleaseHasNoDeveloperTools`, which reads the APK's dex, manifest and
  resources. The U01–U05 undo cases already run against distinct clocks in
  `UndoCompensationContractTest`; the sandbox adds "a year later, undo still has its
  six real seconds" through its own controls. Seeded histories are SELLO-025.
- **Red / Green:** mutations each turned a test red: a fence that does not wait
  (`WriteFenceTest`), no rewind guard (`SandboxTimeTest`), a command run outside the
  fence (`FencedCommandsTest`). The release check failed on the debug APK (devtools
  classes, `ToolsActivity` in the manifest, sandbox strings) and passes on release.
  The badge test first failed because the badge had no click semantics and no height
  without a status bar; both are fixed. `ExportedSchemaTest` refused a
  `deleteDatabase` in `:data`, so the erase moved to the debug source set.
- **Gate results:** `./scripts/verify-ticket SELLO-024 --retain` passed all six checks.
  Host: 551 tests (18 new); device: 606 tests (12 new sandbox tests in
  `SandboxSessionOnDeviceTest` and `TimeControlsJourneyTest`); zero failures, errors or
  skips. Lint had zero errors and 33 warnings: app dependency-update notices and
  unused template colours, two in untouched prototype files; none in changed code.
- **Quality run:** run 20261022T051135Z-f25b9709; SELLO-024 G2 passed; HEAD 03bb458, inputs sha256 01e14a11b4e0; report docs/tickets/E06-live-testing/SELLO-024/quality-report.json sha256 f414aa0b8e4f414d04ecdbb330074bdcc05b8320297ac626b6705e071baffeca
- **Device / Artifact:** the gate ran on the isolated synthetic API-30 AVD `ci30`
  (320 × 640dp), serial `emulator-5582`. On the same AVD the installed debug build
  entered the sandbox in a new process with the badge, stepped 22 Oct to 23 Nov while
  the device date stayed 22 Oct and Recibo showed Noviembre, kept the date across a
  force-stop, left to the normal profile on Octubre without a badge, and a confirmed
  reset reopened the sandbox on today's date
  ([screenshots](artifacts)). No physical device or personal
  data was used; no hosted CI run is claimed.
- **Review:** executor self-review covered release exclusion, profile isolation, the
  fence, forward-only dates and real-time undo. Done is proposed by this ticket's pull
  request; its merge is the designated owner acceptance under `QUALITY_FLOW.md`. No
  owner approval, merge or hosted CI pass is claimed.
- **Acceptance migrated:** 08ecad712f5a96b3ef070093685e4f0e87708353dc6ba350c867c14b814bd04f → e910fbc713c445cb1d13fb226c9288d7b9a7aedb42a404cb6e604ea4f5b55d79 on 2026-10-24; only link paths changed when the ticket moved into its folder.
