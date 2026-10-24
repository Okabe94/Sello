# SELLO-023 — Ship persisted appearance, ordering and essential settings

- **Revision:** tested snapshot is the uncommitted `sello-023-settings` working tree
  on `a7f834338555a5da170f2ebfbe6270c60790a2e4`; it is identified exactly by the
  retained input fingerprint below. The commit and pull request follow this evidence;
  no claim is made that the gate tested a clean commit.
- **Requirement mapping:** `DataStoreUserPreferencesTest` and
  `DataStoreUserPreferencesPersistenceTest` prove stable codes, defaults, invalid/
  unavailable reads and real-file reopen persistence; `CategoryOrderPolicyTest`
  exercises D06/O01–O14 selected-month metrics, direction, finite/unlimited
  partitions, ties, backdating, edits, deletion and restored original creation times.
  `SettingsViewModelTest`, `AppearanceThemeTest`,
  `CorrectionAnnouncementsViewModelTest`, screen tests and `SettingsJourneyTest`
  prove narrow ports, immediate theme mapping, persisted announcements, Recibo gear,
  back navigation, unchanged Room rows/selected month and cold activity restart. The
  DataStore unit regressions additionally prove that a successful write or a retrying
  observer reconnects existing app-wide consumers after a temporary read failure.
- **Red / Green:** `CategoryOrderPolicyTest.latestAdditionUsesOriginalCreationTimeAndReversesOnlyThatMetric`
  failed as intended at line 203 when the policy was temporarily changed to ignore
  descending direction, then passed after the D06 comparator was restored. The new
  catalog example also exposed its deliberately stable-ID contract in the full gate;
  that expectation was updated and the repeat gate passed. For the review fix,
  `DataStoreUserPreferencesTest.aSuccessfulWriteReconnectsAConsumerAfterATemporaryReadFailure`
  expected `Failed(Unavailable)` followed by `Ready(Dark)` but received only the
  failure; it passed after failed public observation flows were kept alive and
  successful writes/new observers triggered a fresh DataStore collection.
- **Gate results:** `./scripts/verify-ticket SELLO-023 --retain` passed all six
  checks: board validation/tests, quality-runner tests, architecture, host and API-30
  device gates. Host: 508 tests; device: 491 tests; zero failures, errors or skips.
  Debug/release app and debug catalog assembled; lint had zero errors and 30 existing
  warnings. Kotlin/JDK emitted `Unsafe` deprecation warnings during focused checks.
- **Quality run:** run 20261014T233049Z-75ea0aa4; SELLO-023 G2 passed; HEAD a7f8343, inputs sha256 59c00ddb7f44; report docs/tickets/E05-mvp-workflows/SELLO-023/quality-report.json sha256 017da527fa56e5db10068f1e841ec1e462ea6e2ef7b1361972dcacc01f66ad3c
- **Device / Artifact:** isolated synthetic API-30 AVD `sello023_api30run`, serial
  `emulator-5580`; no personal history or physical device was used. The real journey
  opens Recibo's gear, changes settings, returns with the selected month and stored
  figures intact, relaunches to prove Dark/Violeta and alphabetical ascending order,
  and the device suite verifies persisted preferences and correction-announcement UI.
  No manual TalkBack session, physical-device test or hosted CI run is claimed.
- **Review:** executor self-review covered DataStore/Room separation, stable codecs,
  cancellation/failure recovery paths, D06 partition/tie behavior, no-draft route recreation,
  catalog/release boundaries and the exact retained report. Done is proposed by
  [PR #46](https://github.com/Okabe94/Sello/pull/46); its merge is the designated
  owner acceptance under `QUALITY_FLOW.md`. No owner approval, merge or hosted CI
  pass is claimed.
