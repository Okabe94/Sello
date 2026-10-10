# Time sources and composition

**Owner:** SELLO-005 · Rules: [architecture](../../ARCHITECTURE.md) §§2, 4, 9.

## Three clocks, never interchangeable

| Port (`:domain`, `domain.port`) | Meaning | Production adapter (`:app`, `platform`) | May a sandbox replace it? |
| --- | --- | --- | --- |
| `FinancialClock` | Today's date in the financial zone, observable | `SystemFinancialClock` | Yes, and only this one |
| `AuditClock` | Real instant for "when was this recorded" | `SystemAuditClock` | No |
| `MonotonicClock` | Elapsed time for deadlines such as undo | `SystemMonotonicClock` | No |

Financial rules read `FinancialClock.today` and observe it; they do not call
`LocalDate.now()` or any other wall-clock API. `scripts/quality/architecture.py`
rejects those calls outside the `platform` package, along with `GlobalScope`,
global `Dispatchers` and Koin outside the composition root.

## Financial zone

The financial zone is stored in the database's profile row ([storage](storage.md)).
At startup `openStorage` establishes that row: the first start of an installation
stores the device zone, and every later start reads the stored zone and ignores the
device's. `SelloApplication` passes the stored zone to `ProcessFinancialZone`, which
holds it for the process. Do not add a preference or file for it.

## When financial "today" is recomputed

`SystemFinancialClock` recomputes at each midnight of the financial zone and on
every `TimeSignals` change. `AndroidTimeSignals` emits when the app returns to the
foreground and on the system's time, date and time-zone broadcasts. A signal also
reschedules the midnight timer, so a clock moved backwards is handled. A device
zone change triggers a recompute but cannot change the financial zone.

## Ownership and disposal

- `ApplicationScope` (supervisor job on the injected default dispatcher) owns work
  that outlives a screen. The financial clock's observer runs in it.
- Closing the Koin application closes `ApplicationScope`, which cancels the
  observer, stops the midnight timer and unregisters the receiver and lifecycle
  observer. A closed scope is not reused.
- Cancellation is rethrown, never turned into a failed or empty result.
- A sandbox session that replaces financial time builds a graph with
  `financialTimeOverride(...)` loaded after the production modules. The production
  clock is then never created, and audit and monotonic time stay real. Switching
  sessions means closing the old graph, which cancels its observers.

## Composition

`composition/PlatformModule.kt` is the only place that knows the adapters,
`composition/StorageModule.kt` the only place that knows `:data`, and
`composition/PresentationModule.kt` the only place that builds view models
([app shell](app-shell.md)). Every
collaborator is a required constructor parameter and the financial clock is
created with the graph, so a missing binding or an uninitialized zone fails at
startup. New workflows are bound when they exist; do not register a placeholder
that reports success.

`ControlledFinancialClock` and `financialTimeOverride` live in
`app/src/debug/.../devtools` and are absent from release builds. The debug app
still runs on production time; SELLO-024 adds the sandbox entry point.

## Not here yet

Preference contracts: none is defined because nothing consumes one before
SELLO-023. When added, the contract belongs in `:domain` with no DataStore type.
