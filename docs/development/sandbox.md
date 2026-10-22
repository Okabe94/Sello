# Debug sandbox and financial time

The debug app (`com.software.sello.debug`, "Sello Debug") can run on a separate
**sandbox** with its own data and a financial date you control. It is for exploring
time-sensitive behaviour (rollover, month ends, closed months) through the real app.
Release builds contain none of it. Seeded histories arrive with SELLO-025; until then
a sandbox starts empty.

## Use it

1. Open **Sello Herramientas**, the debug build's second launcher icon.
2. **Entrar al sandbox** waits for any financial write in progress, then restarts the
   app on the sandbox. Every app screen shows an orange badge in the status bar strip:
   `SANDBOX · 22 oct 2026 · America/Bogota`.
3. In the tools: **+1 día**, **+1 mes**, **Ir a la fecha** (`AAAA-MM-DD`) and **Avanzar
   solo**, one financial day every 2 real seconds, stopping by itself after 31 days.
   **Pausar** stops it. A new process starts paused.
4. **Volver al perfil normal** restarts the app on the normal debug data, which the
   sandbox never touches. The sandbox keeps its data and date for next time.
5. **Reiniciar sandbox**, after a confirmation, erases only the sandbox's data and
   opens it again on today's date.

Android gives touches in the status bar strip to the system, so the badge can't be
tapped with a finger; TalkBack can activate it. Open the tools from their launcher.

## Rules it keeps

- **Only the financial date changes.** The tools publish a new date on the
  `FinancialClock`; rollover, snapshots and Recibo recompute through the normal paths.
  Device time, audit timestamps, elapsed time (so the six-second undo), animations and
  timeouts stay real.
- **No rewriting the past.** Any date is allowed until the sandbox saves its first
  financial change. After that the date only moves forward; going back needs a reset.
- **No crossing profiles.** Each switch or reset closes a `WriteFence` (new commands
  never start, running ones finish), saves the choice and restarts the process. No
  screen, draft or pending action of one profile survives into the other.
- **Separate files.** Normal: `sello.db`, `sello-preferences`. Sandbox: `sandbox.db`,
  `sandbox-preferences`. The session lives in `shared_prefs/sandbox-session.xml`.
  `eraseFinancialStorage` refuses the normal names.

## How it is built

- `composition/Variant.kt` (main) declares `VariantComposition`. The release source
  set returns `ProductionComposition`; the debug one returns `DebugComposition`, which
  picks the storage names from the session, wraps the three command ports in
  `Fenced*Commands`, and in the sandbox binds a `ControlledFinancialClock`.
- `devtools/` (debug only): `SandboxSession` and its store, `WriteFence`,
  `SandboxTime`, `SandboxProfiles` (enter, leave, reset, restart), `SandboxBadges` and
  `ToolsActivity`.
- `:app:assembleRelease` is finalized by `verifyReleaseHasNoDeveloperTools`, which reads
  the release APK's dex files, manifest and resources for any devtools class, the tools
  activity or a sandbox string. `-Psello.inspectApk=build/outputs/apk/debug` points it
  at the debug APK, where it finds them.

## Before running device tests

The app's device tests expect the normal profile. Leave the sandbox (or uninstall the
debug app) first; otherwise the test process opens `sandbox.db`.
