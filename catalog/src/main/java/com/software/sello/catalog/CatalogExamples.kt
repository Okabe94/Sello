package com.software.sello.catalog

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.software.sello.catalog.foundations.ColorsExample
import com.software.sello.catalog.foundations.IconsExample
import com.software.sello.catalog.foundations.MoneyTextExample
import com.software.sello.catalog.foundations.MotionExample
import com.software.sello.catalog.foundations.ShapeSpacingExample
import com.software.sello.catalog.foundations.TypographyExample

/** Component types, in the order the catalog home lists them. */
enum class CatalogGroup(val id: String, @StringRes val title: Int) {
    Foundations("foundations", R.string.catalog_group_foundations),
    Paper("paper", R.string.catalog_group_paper),
    Stamps("stamps", R.string.catalog_group_stamps),
    Money("money", R.string.catalog_group_money),
    Categories("categories", R.string.catalog_group_categories),
    Inputs("inputs", R.string.catalog_group_inputs),
    Navigation("navigation", R.string.catalog_group_navigation),
    Charts("charts", R.string.catalog_group_charts),
    Feedback("feedback", R.string.catalog_group_feedback)
}

/**
 * One browsable example. [id] is `<group id>.<name>` and is stable: UI and
 * accessibility tests target it, so renaming one is a deliberate change.
 */
class CatalogExample(
    val id: String,
    val group: CatalogGroup,
    @StringRes val title: Int,
    val content: @Composable () -> Unit
)

val catalogExamples: List<CatalogExample> = listOf(
    CatalogExample(
        "foundations.colors",
        CatalogGroup.Foundations,
        R.string.catalog_example_colors
    ) { ColorsExample() },
    CatalogExample(
        "foundations.typography",
        CatalogGroup.Foundations,
        R.string.catalog_example_typography
    ) { TypographyExample() },
    CatalogExample(
        "foundations.money-text",
        CatalogGroup.Foundations,
        R.string.catalog_example_money_text
    ) { MoneyTextExample() },
    CatalogExample(
        "foundations.shape-spacing",
        CatalogGroup.Foundations,
        R.string.catalog_example_shape_spacing
    ) { ShapeSpacingExample() },
    CatalogExample(
        "foundations.motion",
        CatalogGroup.Foundations,
        R.string.catalog_example_motion
    ) { MotionExample() },
    CatalogExample(
        "foundations.icons",
        CatalogGroup.Foundations,
        R.string.catalog_example_icons
    ) { IconsExample() }
)
