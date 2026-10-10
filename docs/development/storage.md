# Financial storage

**Owner:** SELLO-011 · Rules: [architecture](../../ARCHITECTURE.md) §§4, 5, 7.

One Room database, `sello.db`, in the app's private storage, is the only place
financial facts live. Its schema is version 1, exported to
`data/schemas/com.software.sello.data.local.SelloDatabase/1.json`.

## What is stored

| Table | Holds | Key and order |
| --- | --- | --- |
| `profile` | The one profile row: financial zone, generation, revision | `id` is always 1 |
| `category` | Name, name key, icon key, archived flag, version, audit times | `id`; unique `name_key` |
| `default_limit` | A category's default limit from a month onwards | (`category_id`, `effective_month`) |
| `month_limit` | The limit set for one category in one month | (`category_id`, `month`) |
| `expense` | Category, effective date, sequence, amount, note, version, audit times | `id`; unique `sequence`; read by (`effective_date`, `sequence`) |
| `income` | Effective date, sequence, amount, source key and name, note, version, audit times | `id`; unique `sequence`; read by (`effective_date`, `sequence`) |
| `operation_receipt` | Proof that an operation committed: kind, input digest, generation, revision, subject | `operation_id` |

There are no recurrence, goal, investment, widget or capture tables, and no
preference such as theme or category order: those are not financial and stay out
of Room.

- **Generation** starts at 1 and advances only when the whole history is replaced
  (restore or reset). **Revision** starts at 0 and advances with every committed
  change. Both are created with the zone in one insert.
- **Receipts** have no foreign key to the record they describe and hold no amount,
  name or note. That is what lets them, and the profile row, outlive a replacement
  of the financial rows without keeping erased content.
- A month with neither a `month_limit` nor an applicable `default_limit` is
  unconfigured. That is different from a finite limit of zero and from `unlimited`.

## How values are stored

| Value | Stored as |
| --- | --- |
| Identifier | Lower-case hyphenated UUID text; any other spelling is damage |
| Money | `*_minor` whole minor units plus a `currency` code; only `COP` is valid in the MVP |
| Date, month | `yyyy-MM-dd`, `yyyy-MM` text, so text order is calendar order |
| Audit time | Milliseconds since the epoch, from `AuditClock`. A writer truncates to milliseconds first so a value reads back equal |
| Kind, source, icon | Stable lower-case keys (`finite`, `unlimited`, `paused` for a default only, `salary`, `shopping_cart`); never a label or an enum position |
| Zone | The IANA identifier, such as `America/Bogota` |

## Who enforces what

- **The database** refuses: a second category with the same name key, including an
  archived one; a second receipt for an operation; a repeated ordering sequence; an
  expense or limit whose category does not exist; deleting or re-keying a category
  that has expenses or limits. Every foreign key is `RESTRICT`; nothing cascades.
- **The mappers** (`data/mapper`) check every column of every row they read against
  the domain rules and stop at the first that fails, returning
  `StorageFailure.Integrity(table, row, column)`. Nothing is defaulted, trimmed,
  clamped or skipped, and one damaged row fails the whole read. The failure carries
  no stored value, so it is safe to log.
- Room cannot declare `CHECK` constraints. Value ranges are therefore enforced by
  the typed domain values on the way in and the mappers on the way out, not by
  hand-written triggers that the exported schema would not describe.

## Opening and failing

`openFinancialStorage` in `:data` is the only entry point; `:app` sees
`FinancialStorage` and domain ports, never Room. There is no destructive fallback:

- A file from a newer schema with no migration is refused and left untouched.
- A damaged file is refused and left untouched. Android's default is to delete a
  damaged database and start empty; `NonDestructiveOpenHelperFactory` removes that.
- A database error becomes `StorageFailure.Unavailable`. Cancellation is rethrown.

At startup `openStorage` (app `composition`) establishes the profile before the
graph is built: the first start of an installation stores the device zone, and every
later start reads the stored zone and ignores the device's. If that fails the app
stops with `FinancialStorageUnavailable`; it never continues on the device zone or
an empty database. Showing that failure as a screen is the app shell's job (SELLO-015).

## Backups by the system are off

`allowBackup` is false and both rule files exclude every storage domain from cloud
backup and device transfer, so the database never leaves the device through
Android. The only backup is the file a person saves themselves (SELLO-028).

Changes are made only through [commands](commands.md), which write the record, the
revision and the receipt in one transaction.

## Changing the schema

1. Change the entities, raise `version` in `SelloDatabase` and build: KSP writes the
   next `N.json`. Commit it. Never edit an exported file.
2. Add a `Migration` and register it in `openSelloDatabase`. No destructive or
   automatic fallback.
3. Extend `ExportedSchemaOnDeviceTest` with `runMigrationsAndValidate` from every
   earlier version, using rows that must survive, and update `ExportedSchemaTest`.
4. Review whether the portable backup format is affected.

## Not here yet

- Deletion and undo records: SELLO-019. Restore and reset: SELLO-030, SELLO-031.
