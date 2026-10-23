# Performance profile

How fast Sello is with a large history, how that is measured, and the targets the
measurements must meet. SELLO-035 owns it under
[D10](../decisions/0001-mvp-contract.md#d10--release-acceptance-and-review). Synthetic
data only.

## Targets

Approved by the owner on 2026-10-23, before any result was taken. They hold for the
large history on the reference emulator, release build. A slow run does not lower a
target; it is a finding to fix.

| Journey | Target | Measured as |
| --- | --- | --- |
| Cold start | ≤ 1.5 s to the figures | Median of 10: Recibo reports itself fully drawn once the month is read |
| Warm start | ≤ 0.5 s | Median of 10: the first frame after the activity ended and the process stayed |
| Scrolling Recibo | < 5% slow frames | Median of 5 runs of 10 swipes: Android's janky-frame share |
| Opening Resumen | ≤ 700 ms with its chart | Median of 10, first opening in a new process |
| Opening a category's detail | Informative | Median of 10, first opening in a new process, until its comparison and numbers show |
| Backup | ≤ 5 s | Median of 5: from Sello's first frame after "Save" until "Guardado" |
| Restore | ≤ 10 s | Median of 5: checking the file plus replacing |
| Main thread | No database or file work | `MainThreadWorkTest`, below |

Owners: the project owner tests on physical devices and keeps the signing key.

## Datasets

`scripts/performance/dataset.py` writes each history as a version 1
[backup file](../data/backup-format.md); the same profile and day always make the same
bytes. It goes into the app through Respaldo → Restaurar, so the app's own validation
and replacement store it, as a person's backup would be.

| Profile | Categories | Expenses | Incomes | Span | File |
| --- | --- | --- | --- | --- | --- |
| Large (targets) | 20 | 10,000 | 2,000 | 12 past months and the current one | 3.0 MB |
| Typical (informative) | 8 | 600 | 40 | 3 past months and the current one | 0.16 MB |

About 26 expenses a day, half with a note. Every category has a monthly limit; in a
finished month about four in ten categories went over it.

## Method

`scripts/performance/measure.py` drives an installed build through `adb` and never
installs, clears or resets anything:

- **Starts:** `am start-activity -W`. A cold start force-stops the app first; a warm
  start follows Back on Recibo, which on API 30 ends the activity and keeps the process.
  "Fully drawn" is the time Android logs when `ReportDrawnWhen` on Recibo sees the
  month read, so it includes reading the database, not only the first frame.
- **Frames:** `dumpsys gfxinfo`, reset before each journey. Scrolling uses Android's
  janky-frame count. Opening a screen is timed from the first frame's intended start to
  the completion of the last frame drawn before the screen showed its content and then
  paused for 0.5 s; the content is checked on screen with `uiautomator`.
- **Animations:** on for starts and scrolling, as a person has them; off while timing
  Resumen, backup and restore so that an animation's length is not counted as work.
- **System dialogs:** the file is opened and saved through the system's own dialogs.
  Their positions are calibrated for the reference phone's 1080 × 2340 screen.

Repeat with:

```bash
python3 scripts/performance/dataset.py large /tmp/sello-grande.json
adb -s <serial> push /tmp/sello-grande.json /sdcard/Download/sello-grande.json
# Install the build under test, open it once, restore the file through Respaldo.
python3 scripts/performance/measure.py startup --serial <serial> --runs 10
python3 scripts/performance/measure.py scroll --serial <serial> --runs 5
python3 scripts/performance/measure.py resumen --serial <serial> --runs 10
python3 scripts/performance/measure.py category --serial <serial> --runs 10
python3 scripts/performance/measure.py backup --serial <serial> --runs 5
python3 scripts/performance/measure.py restore --serial <serial> --runs 5
```

The restore journey opens the first file in the picker's Downloads.

## Results

Taken on 2026-10-23 on the isolated emulator `sello022_api30`: API 30 Google APIs
x86_64, 4 cores, 2 GB, 1080 × 2340 at 440 dpi, host GPU, on a 16-core desktop. Build:
the release build type, not yet shrunk, not debuggable, signed with a throwaway
measurement key outside the repository; built from the part 1 commit on top of
`main` with #88. Each history was
restored once and the app opened before timing.

| Journey | Target | Large: median (range) | Typical: median |
| --- | --- | --- | --- |
| Cold start, figures shown | ≤ 1.5 s | **353 ms** (331–392) | 326 ms |
| Cold start, first frame | — | 281 ms (263–319) | 272 ms |
| Warm start | ≤ 0.5 s | **79 ms** (66–86) | 80 ms |
| Scrolling Recibo, slow frames | < 5% | **0%** in all 5 runs; 90th percentile 6 ms | — |
| Opening Resumen | ≤ 700 ms | **535 ms** (535–551) | 251 ms |
| Opening a category's detail | — | 218 ms (201–235) | 202 ms |
| Backup | ≤ 5 s | **1.47 s** (1.45–1.49) | 0.50 s |
| Restore: checking the file | — | 0.99 s (0.92–1.00) | 0.35 s |
| Restore: replacing | — | 1.38 s (1.38–1.45) | 0.48 s |
| Restore, total | ≤ 10 s | **2.37 s** | 0.84 s |

Every target is met. A category's detail, which since #88 compares the month with the
previous one and averages finished months, had no target of its own; it opens well within
Resumen's. After opening Resumen with the large history the app used 78 MB (total PSS),
12 MB of it Java heap.

**Resumen has the least margin.** Its figures arrive about 300 ms after the tap; the
database read and the arithmetic run off the main thread. Then two frames take about
200 ms each to draw the 20 categories, the 12 months and the daily line, against 100 to
130 ms with 8 categories. It reads the expense rows of the 12 months it charts, so its
cost follows the months shown, not the whole history. Part 2 measures it again on the
shrunk build before deciding whether drawing needs work.

**Starting** waits on the main thread for the database to open and the financial zone
to be read (`openStorage`, on purpose: no date may be computed before the zone is
known). It is one small read whatever the history's size: the large history starts
27 ms later than the typical one, to the figures.

**Main thread.** `MainThreadWorkTest` walks Recibo, a category's detail, a new expense
saved with the keypad and Resumen in the real app under StrictMode, and fails on any
disk read or write on the main thread from Sello's code. It passes; a deliberate read
planted in the test made it fail, naming the call.

Findings outside performance, reported and not fixed here:

- The restore preview and result write counts without a thousands separator:
  "10000 gastos".
- Resumen shows "Movimientos anotados hasta 2026-10-23", a date in storage form.

## Not covered yet

- The optimized release: R8 shrinking arrives in part 2 of SELLO-035 and is measured
  again then. These results are the unshrunk release build.
- A physical phone: the emulator runs on a fast desktop and flatters every number.
  The owner may run the same script on a phone; acceptance on a device is SELLO-036.
