# UI acceptance matrix

Which windows, text sizes and themes the assembled daily-use app was checked in, what
each check proves, and the test or walk that proves it. SELLO-033 assembled this
matrix under [D10](../decisions/0001-mvp-contract.md#d10--release-acceptance-and-review).
Manual TalkBack, Switch Access and hardware keyboard walks are post-MVP
([SELLO-074](../planning/mvp/epics/E14-assistive-technology.md)); the accessibility
checks a test can read are here. Synthetic data only.

## Configurations

| ID | Window (dp) | Text | Theme | Where | Layout it exercises |
| --- | --- | --- | --- | --- | --- |
| C1 | 320 × 640 | 2.0 | Cobalto light | Sweep | Compact phone: tab bar, dock, one pane |
| L1 | 640 × 360 | 1.3 | Violeta dark | Sweep | Short landscape: rail, two panes |
| L2 | 640 × 360 | 2.0 | Cobalto dark | Sweep | Short landscape at the largest text |
| M1 | 700 × 900 | 1.0 | Violeta light | Sweep | Medium: tab bar, side-by-side detail |
| E1 | 1000 × 700 | 1.0 | Cobalto light | Sweep | Expanded: rail, list and detail panes |

The sweep is `UiAcceptanceSweepTest`: the real app root with its own database and view
models, run on the API 30 gate emulator in a window of the given size, one dp drawn
as one pixel. A window larger than the emulator's screen proves what exists and
where it is laid out, not what is visible; part 2's walks on real windows cover that.

Every theme appears once in the sweep; colour contrast for all four is checked
separately, token by token, by `SelloColorsTest` (WCAG AA: 4.5:1 for text, 3:1 for
marks and large text), including the stamps, soft fills and past-month bar.

Font scales 1.0 and 1.3 on a compact phone are covered by each screen's own tests
(`*ScreenTest`, `ShellScreenLayoutTest`, `ExpenseEntryGeometryTest` and the
design-system suites); the sweep takes the riskiest corners rather than all 48
combinations.

## Screens and checks

Each window opens every screen below through the app's own buttons, from a history
with two categories (one over its limit), two expenses and an income:

Recibo · category detail · expense edit · category edit · expense entry · new
category · Resumen · Resumen's category · income history · income entry ·
Configuración · Respaldo · Borrar todos los datos.

On each screen the sweep records, then reports together:

| Check | Rule |
| --- | --- |
| Touch targets | Every control answers a touch at least 48 × 48 dp (Compose's widened touch area included). A control a list has scrolled out of view is measured where it is whole. |
| Text not cut short | No text is shortened with an ellipsis or stopped at its line limit. |
| Amounts spoken in full | Every amount shown is read with its currency, such as "48.700 pesos". |
| Main action reachable | Save, delete, Respaldar, Continuar and the Configuración rows scroll into the window. |

## Findings and fixes

| Found | Where | Fix | Regression test |
| --- | --- | --- | --- |
| Screen titles cut short ("Editar gasto", "Ingresos de octubre", "Configuración · Octubre 2026") | C1 | Titles and the month title take a second line before any ellipsis | `LargeTextFitTest`, sweep |
| "Editar categoría" did not fit beside **Archivar** even on two lines | C1 | When the title has no room beside its actions it moves under them, full width; otherwise unchanged | `LargeTextFitTest.aTitleWithNoRoomBesideItsActionMovesUnderItAndStaysWhole`, `aTitleThatFitsStaysBesideItsAction` |
| Rail labels "Recibo", "Resumen" cut short | L2 | The rail widens to its longest label (at least 80dp) | `LargeTextFitTest.theRailWidensSoEveryTabLabelIsWhole` |
| Chip label "Último gasto agregado" cut short | C1 | Chips take a second line before an ellipsis, as the v1.1 spec's "chips wrap" says | `LargeTextFitTest.aChipsLabelTakesASecondLineInsteadOfBeingShortened` |
| Limit suggestions read "200.000" without pesos | all | Each suggestion is read as "200.000 pesos" | `CategoryEditorViewModelTest`, `CategoryManagementViewModelTest`, `CategoryEditorScreenTest` |
| Resumen's limit line read "Límite 350.000" | all | The chart's reference line is read as "Límite 350.000 pesos" | `ChartExactSelectionTest`, `ResumenScreenTest` |

No touch target under 48dp and no main action outside its window was found.
