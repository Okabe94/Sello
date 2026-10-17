package com.software.sello.catalog.v2

import androidx.compose.runtime.Composable
import com.software.sello.catalog.CatalogExample
import com.software.sello.catalog.CatalogGroup
import com.software.sello.catalog.R

private fun example(
    id: String,
    title: Int,
    fullBleed: Boolean = false,
    content: @Composable () -> Unit
) = CatalogExample("v2.$id", CatalogGroup.V2, title, fullBleed, content)

val v2CatalogExamples = listOf(
    example("entry", R.string.v2_entry, true) { V2EntryExample() },
    example("amount", R.string.v2_amount) { V2AmountExample() },
    example("keypad", R.string.v2_keypad) { V2KeypadExample() },
    example("chips", R.string.v2_chips) { V2ChipsExample() },
    example("note-composer", R.string.v2_note_composer) { V2ComposerExample() },
    example("buttons", R.string.v2_buttons) { V2ButtonsExample() },
    example("dock", R.string.v2_dock) { V2DockExample() },
    example("navigation", R.string.v2_navigation) { V2NavigationExample() },
    example("month-bar", R.string.v2_month_bar) { V2MonthExample() },
    example("past-month-ribbon", R.string.v2_past_month_ribbon) { V2RibbonExample() },
    example("stamps", R.string.v2_stamps) { V2StampsExample() },
    example("footnotes", R.string.v2_footnotes) { V2FootnotesExample() },
    example("summary", R.string.v2_summary) { V2SummaryExample() },
    example("comparison", R.string.v2_comparison) { V2ComparisonExample() },
    example("ranking", R.string.v2_ranking) { V2RankingExample() },
    example("suggestion", R.string.v2_suggestion) { V2SuggestionExample() },
    example("charts", R.string.v2_charts) { V2ChartsExample() },
    example("milestones", R.string.v2_milestones) { V2MilestonesExample() },
    example("goal-pill", R.string.v2_goal_pill) { V2GoalPillExample() },
    example("goal-cards", R.string.v2_goal_cards) { V2GoalCardsExample() },
    example("movements", R.string.v2_movements) { V2MovementsExample() },
    example("investments", R.string.v2_investments) { V2InvestmentsExample() },
    example("icon-choice", R.string.v2_icon_choice) { V2IconChoiceExample() },
    example("feedback", R.string.v2_feedback) { V2FeedbackExample() },
    example("celebration", R.string.v2_celebration) { V2CelebrationExample() },
    example("states", R.string.v2_states) { V2StatesExample() },
    example("foundations", R.string.v2_foundations) { V2FoundationsExample() }
)
