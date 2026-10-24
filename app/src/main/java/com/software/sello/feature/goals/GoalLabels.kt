package com.software.sello.feature.goals

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.software.sello.R
import com.software.sello.designsystem.component.MilestoneSpec
import com.software.sello.designsystem.component.MilestoneState
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.domain.model.GoalPlace
import com.software.sello.presentation.category.CategoryIcons

/** "25 %" … "Meta", as written on the stamp. */
@Composable
internal fun placeLabel(place: GoalPlace): String = stringResource(
    when (place) {
        GoalPlace.Quarter -> R.string.goal_place_quarter
        GoalPlace.Half -> R.string.goal_place_half
        GoalPlace.ThreeQuarters -> R.string.goal_place_three_quarters
        GoalPlace.Meta -> R.string.goal_place_meta
    }
)

/** "25 por ciento" … "Meta", as a screen reader says it. */
@Composable
internal fun placeSpoken(place: GoalPlace): String = stringResource(
    when (place) {
        GoalPlace.Quarter -> R.string.goal_place_quarter_spoken
        GoalPlace.Half -> R.string.goal_place_half_spoken
        GoalPlace.ThreeQuarters -> R.string.goal_place_three_quarters_spoken
        GoalPlace.Meta -> R.string.goal_place_meta_spoken
    }
)

/** A place as the design system draws it, said with its state. */
@Composable
internal fun milestone(model: PlaceModel): MilestoneSpec {
    val spoken = placeSpoken(model.place)
    return MilestoneSpec(
        label = placeLabel(model.place),
        description = stringResource(
            when (model.look) {
                PlaceLook.Empty -> R.string.goal_place_empty
                PlaceLook.Earned -> R.string.goal_place_earned
                PlaceLook.Kept -> R.string.goal_place_kept
                PlaceLook.Ready -> R.string.goal_place_ready
            },
            spoken
        ),
        state = when (model.look) {
            PlaceLook.Empty -> MilestoneState.Empty
            PlaceLook.Earned -> MilestoneState.Earned
            PlaceLook.Kept -> MilestoneState.Kept
            PlaceLook.Ready -> MilestoneState.Ready
        }
    )
}

/** A goal's icon. Goals choose from the same drawings as categories. */
internal fun goalIcon(key: String): SelloIcon = CategoryIcons.iconFor(key)

/** How full a bar is: geometry only, never shown as a number or turned back into money. */
internal fun barFill(percentage: Long): Float = percentage.coerceIn(0, 100) / 100f
