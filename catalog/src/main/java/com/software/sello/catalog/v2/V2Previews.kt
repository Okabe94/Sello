package com.software.sello.catalog.v2

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.software.sello.catalog.components.copFixture
import com.software.sello.designsystem.component.v2.CelebrationOverlay
import com.software.sello.designsystem.component.v2.MilestoneState
import com.software.sello.designsystem.component.v2.NoteComposer
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloInk
import com.software.sello.designsystem.theme.SelloTheme

class V2PreviewTheme(val ink: SelloInk, val dark: Boolean)
class V2PreviewThemes : PreviewParameterProvider<V2PreviewTheme> {
    override val values = SelloInk.entries.asSequence().flatMap { ink ->
        sequenceOf(V2PreviewTheme(ink, false), V2PreviewTheme(ink, true))
    }
}

@Preview(name = "Compact", widthDp = 360, heightDp = 780)
@Preview(name = "Narrow, large text", widthDp = 320, heightDp = 780, fontScale = 2f)
@Preview(name = "Expanded", widthDp = 840, heightDp = 780)
annotation class V2Previews

@Composable
private fun Frame(theme: V2PreviewTheme, content: @Composable () -> Unit) {
    SelloTheme(ink = theme.ink, darkTheme = theme.dark, reducedMotion = true) {
        Box(Modifier.fillMaxSize()) { content() }
    }
}

@V2Previews
@Composable
private fun V2EntryExamplePreview(@PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme) =
    Frame(theme) { V2EntryExample() }

@V2Previews
@Composable
private fun V2AmountExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2AmountExample() }

@V2Previews
@Composable
private fun V2KeypadExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2KeypadExample() }

@V2Previews
@Composable
private fun V2ChipsExamplePreview(@PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme) =
    Frame(theme) { V2ChipsExample() }

@V2Previews
@Composable
private fun V2ComposerExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2ComposerExample() }

@V2Previews
@Composable
private fun V2ButtonsExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2ButtonsExample() }

@V2Previews
@Composable
private fun V2DockExamplePreview(@PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme) =
    Frame(theme) { V2DockExample() }

@V2Previews
@Composable
private fun V2NavigationExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2NavigationExample() }

@V2Previews
@Composable
private fun V2MonthExamplePreview(@PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme) =
    Frame(theme) { V2MonthExample() }

@V2Previews
@Composable
private fun V2RibbonExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2RibbonExample() }

@V2Previews
@Composable
private fun V2StampsExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2StampsExample() }

@V2Previews
@Composable
private fun V2FootnotesExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2FootnotesExample() }

@V2Previews
@Composable
private fun V2SummaryExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2SummaryExample() }

@V2Previews
@Composable
private fun V2ComparisonExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2ComparisonExample() }

@V2Previews
@Composable
private fun V2RankingExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2RankingExample() }

@V2Previews
@Composable
private fun V2SuggestionExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2SuggestionExample() }

@V2Previews
@Composable
private fun V2ChartsExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2ChartsExample() }

@V2Previews
@Composable
private fun V2MilestonesExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2MilestonesExample() }

@V2Previews
@Composable
private fun V2GoalPillExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2GoalPillExample() }

@V2Previews
@Composable
private fun V2GoalCardsExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2GoalCardsExample() }

@V2Previews
@Composable
private fun V2MovementsExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2MovementsExample() }

@V2Previews
@Composable
private fun V2InvestmentsExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2InvestmentsExample() }

@V2Previews
@Composable
private fun V2IconChoiceExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2IconChoiceExample() }

@V2Previews
@Composable
private fun V2FeedbackExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2FeedbackExample() }

@V2Previews
@Composable
private fun V2CelebrationExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2CelebrationExample() }

@V2Previews
@Composable
private fun V2StatesExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2StatesExample() }

@V2Previews
@Composable
private fun V2FoundationsExamplePreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) { V2FoundationsExample() }

@V2Previews
@Composable
private fun NoteComposerOpenPreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) {
    NoteComposer(
        AnnotatedString("Gasto en Comida · 48.700"),
        "Almuerzo",
        {},
        "8/60",
        "Opcional",
        null,
        "Listo",
        "Cancelar",
        {},
        {},
        contextIcon = SelloIcon.Restaurant
    )
}

@V2Previews
@Composable
private fun CelebrationOpenPreview(
    @PreviewParameter(V2PreviewThemes::class) theme: V2PreviewTheme
) = Frame(theme) {
    CelebrationOverlay(
        "¡Lo lograste!", "META LOGRADA", "Viaje a la costa", SelloIcon.Flight,
        copFixture(
            3_000_000
        ),
        "16 oct 2026 · 6 aportes",
        GoalPlaces.map {
            it.copy(state = MilestoneState.Earned, description = "${it.label}, logrado")
        },
        "Listo", "Ver mi meta", {}, {}
    )
}
