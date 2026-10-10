package com.software.sello.catalog

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.software.sello.catalog.components.AmountKeypadExample
import com.software.sello.catalog.components.ButtonExample
import com.software.sello.catalog.components.CategoryCellExample
import com.software.sello.catalog.components.CategoryCircleExample
import com.software.sello.catalog.components.ChipExample
import com.software.sello.catalog.components.ColumnsChartExample
import com.software.sello.catalog.components.ConfirmSlipExample
import com.software.sello.catalog.components.CountingExample
import com.software.sello.catalog.components.EmptySlipExample
import com.software.sello.catalog.components.EntryDockExample
import com.software.sello.catalog.components.ErrorSlipExample
import com.software.sello.catalog.components.ExpenseEntryExample
import com.software.sello.catalog.components.FieldPillExample
import com.software.sello.catalog.components.FormFieldExample
import com.software.sello.catalog.components.IconChoiceExample
import com.software.sello.catalog.components.KeyValueExample
import com.software.sello.catalog.components.LeaderLineExample
import com.software.sello.catalog.components.LineChartExample
import com.software.sello.catalog.components.MoneyTextExample
import com.software.sello.catalog.components.MonthSwitcherExample
import com.software.sello.catalog.components.NavigationBarExample
import com.software.sello.catalog.components.ScaffoldExample
import com.software.sello.catalog.components.SlipExample
import com.software.sello.catalog.components.SlipSkeletonExample
import com.software.sello.catalog.components.StampExample
import com.software.sello.catalog.components.SwitchExample
import com.software.sello.catalog.components.TearLineExample
import com.software.sello.catalog.components.TotalLineExample
import com.software.sello.catalog.foundations.ColorsExample
import com.software.sello.catalog.foundations.IconsExample
import com.software.sello.catalog.foundations.MoneyTextExample as LongMoneyTextExample
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
    /** Fills the stage itself and scrolls on its own, like a whole screen. */
    val fullBleed: Boolean = false,
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
    ) { LongMoneyTextExample() },
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
    ) { IconsExample() },
    CatalogExample(
        "paper.slip",
        CatalogGroup.Paper,
        R.string.catalog_example_slip
    ) { SlipExample() },
    CatalogExample(
        "paper.tear-line",
        CatalogGroup.Paper,
        R.string.catalog_example_tear_line
    ) { TearLineExample() },
    CatalogExample(
        "paper.leader-line",
        CatalogGroup.Paper,
        R.string.catalog_example_leader_line
    ) { LeaderLineExample() },
    CatalogExample(
        "paper.total-line",
        CatalogGroup.Paper,
        R.string.catalog_example_total_line
    ) { TotalLineExample() },
    CatalogExample(
        "paper.key-value",
        CatalogGroup.Paper,
        R.string.catalog_example_key_value
    ) { KeyValueExample() },
    CatalogExample(
        "stamps.stamp",
        CatalogGroup.Stamps,
        R.string.catalog_example_stamp
    ) { StampExample() },
    CatalogExample(
        "money.money-text",
        CatalogGroup.Money,
        R.string.catalog_example_money_text_component
    ) { MoneyTextExample() },
    CatalogExample(
        "money.counting",
        CatalogGroup.Money,
        R.string.catalog_example_counting
    ) { CountingExample() },
    CatalogExample(
        "categories.category-circle",
        CatalogGroup.Categories,
        R.string.catalog_example_category_circle
    ) { CategoryCircleExample() },
    CatalogExample(
        "categories.category-cell",
        CatalogGroup.Categories,
        R.string.catalog_example_category_cell
    ) { CategoryCellExample() },
    CatalogExample(
        "inputs.button",
        CatalogGroup.Inputs,
        R.string.catalog_example_button
    ) { ButtonExample() },
    CatalogExample(
        "inputs.amount-keypad",
        CatalogGroup.Inputs,
        R.string.catalog_example_amount_keypad
    ) { AmountKeypadExample() },
    CatalogExample(
        "inputs.chip",
        CatalogGroup.Inputs,
        R.string.catalog_example_chip
    ) { ChipExample() },
    CatalogExample(
        "inputs.field-pill",
        CatalogGroup.Inputs,
        R.string.catalog_example_field_pill
    ) { FieldPillExample() },
    CatalogExample(
        "inputs.form-field",
        CatalogGroup.Inputs,
        R.string.catalog_example_form_field
    ) { FormFieldExample() },
    CatalogExample(
        "inputs.switch",
        CatalogGroup.Inputs,
        R.string.catalog_example_switch
    ) { SwitchExample() },
    CatalogExample(
        "inputs.icon-choice",
        CatalogGroup.Inputs,
        R.string.catalog_example_icon_choice
    ) { IconChoiceExample() },
    CatalogExample(
        "inputs.expense-entry",
        CatalogGroup.Inputs,
        R.string.catalog_example_expense_entry
    ) { ExpenseEntryExample() },
    CatalogExample(
        "navigation.scaffold",
        CatalogGroup.Navigation,
        R.string.catalog_example_scaffold,
        fullBleed = true
    ) { ScaffoldExample() },
    CatalogExample(
        "navigation.entry-dock",
        CatalogGroup.Navigation,
        R.string.catalog_example_entry_dock
    ) { EntryDockExample() },
    CatalogExample(
        "navigation.navigation-bar",
        CatalogGroup.Navigation,
        R.string.catalog_example_navigation_bar
    ) { NavigationBarExample() },
    CatalogExample(
        "navigation.month-switcher",
        CatalogGroup.Navigation,
        R.string.catalog_example_month_switcher
    ) { MonthSwitcherExample() },
    CatalogExample(
        "charts.columns",
        CatalogGroup.Charts,
        R.string.catalog_example_columns_chart
    ) { ColumnsChartExample() },
    CatalogExample(
        "charts.line",
        CatalogGroup.Charts,
        R.string.catalog_example_line_chart
    ) { LineChartExample() },
    CatalogExample(
        "feedback.confirm-slip",
        CatalogGroup.Feedback,
        R.string.catalog_example_confirm_slip
    ) { ConfirmSlipExample() },
    CatalogExample(
        "feedback.empty-slip",
        CatalogGroup.Feedback,
        R.string.catalog_example_empty_slip
    ) { EmptySlipExample() },
    CatalogExample(
        "feedback.error-slip",
        CatalogGroup.Feedback,
        R.string.catalog_example_error_slip
    ) { ErrorSlipExample() },
    CatalogExample(
        "feedback.slip-skeleton",
        CatalogGroup.Feedback,
        R.string.catalog_example_slip_skeleton
    ) { SlipSkeletonExample() }
)
