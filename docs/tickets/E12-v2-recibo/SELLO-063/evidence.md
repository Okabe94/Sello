# SELLO-063 — Update the Recibo categories card and first-use and failed states

- **Revision:** tested snapshot is the uncommitted `sello-063-categories-card` working
  tree on `26d97c2f8dc55b2c0168b4984d19a18ee33c6e63`; it is identified exactly by the
  retained input fingerprint below. The commit and pull request follow this evidence;
  no claim is made that the gate tested a clean commit.
- **Requirement mapping:** `ReciboScreenTest.theCategoriesCardHeaderCarriesTheOnlyAddButton`
  proves the **+** sits on the **Tus categorías** line above the grid, is named
  "Agregar categoría", has a 48dp target, opens category creation, and that the
  caption and the link under the grid are gone. `theFirstRunOffers…` and
  `aFailureOffersRetryAndShowsNoAmount` prove the v2 first-use and failed-read copy,
  that each never shows as the other, and that a failure shows no amount;
  `aLongPressOrAScreenReaderActionOnACategoryOffersToEditIt…` still passes. The
  categories card keeps `pinked = false` with a comment; only the answer slip is
  pinked. `ReciboListDetailWideTest` proves the header **+** is present and clickable
  beside category detail at 900 × 700dp. `FirstCategoryJourneyTest`,
  `WalkingSliceJourneyTest`, `ExpenseEntryJourneyTest`, `ResumenJourneyTest` and
  `ShellOnDeviceTest` pass through the new first-use action. In `:design-system`,
  `EmptySlip` gains an overload with a required `icon` and `ErrorSlip` an optional
  `icon`; both centre their text under a decorative icon only when given one, and
  `FeedbackInteractionTest.withAnIconTheStateSlipsCentreTheirTextAndKeepTheIconSilent`
  proves the centring, the silent icon and the 48dp retry. The catalog's V2 states
  example uses the new look.
- **Red / Green:** before the change, `theCategoriesCardHeaderCarriesTheOnlyAddButton`
  failed finding no node described "Agregar categoría", and the updated first-use and
  failed-read tests failed on the old copy ("Tu recibo está en blanco", "No pudimos
  leer tus datos de este mes."); all passed after it. Adding `icon` after `onAction`
  first broke existing trailing-lambda and positional `EmptySlip` callers at compile
  time; the original signature was restored and the icon moved to an overload.
- **Gate results:** `./scripts/verify-ticket SELLO-063 --retain` passed all six
  checks: board validation/tests, quality-runner tests, architecture, host and API-30
  device gates. Host: 511 tests; device: 528 tests; zero failures, errors or skips.
  Lint had zero errors and 32 existing warnings.
- **Quality run:** run 20261017T181528Z-48779804; SELLO-063 G2 passed; HEAD 26d97c2, inputs sha256 e7c8bac6467b; report docs/tickets/E12-v2-recibo/SELLO-063/quality-report.json sha256 40841b45e4f8f94f0036fe73df4df2ca69d07b3a1972eaf2e1ae812a8d1b92d7
- **Device / Artifact:** the gate ran on an isolated synthetic API-30 AVD (`ci30`,
  320 × 640dp, mdpi, animations off, matching the hosted runner), serial
  `emulator-5582`. Focused tests and a visual check ran on an API-31 AVD (`test31`,
  `emulator-5580`): a fresh install showed the v2 first-use slip, and creating a
  category from it showed the **Tus categorías** header with its **+**. No personal
  history or physical device was used; no manual TalkBack session or hosted CI run is
  claimed.
- **Review:** executor self-review covered the copy against the v2 Recibo mockup, the
  distinct empty/failed/stale states, the two-column layout and source compatibility
  of the design-system change. Done is proposed by
  [PR #56](https://github.com/Okabe94/Sello/pull/56); its merge is the designated
  owner acceptance under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI
  pass is claimed.

