package com.software.sello.feature.recibo

import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.designsystem.component.StampInk
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.policy.MonthBudgetState
import com.software.sello.domain.port.SnapshotState
import com.software.sello.feature.expense.categoryIn
import com.software.sello.feature.expense.monthOf
import com.software.sello.feature.expense.storedExpense
import com.software.sello.feature.expense.uuid
import com.software.sello.presentation.money.MoneyFormatter
import com.software.sello.presentation.money.MoneyLabels
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Snapshots here are worked out by the real policy from rows; every expected figure
 * was worked out by hand from those rows and is typed, not computed.
 */
class ReciboRenderModelTest {
    private val labels = object : MoneyLabels {
        override fun amount(currency: Currency, digits: String, isOne: Boolean) = "$digits pesos"

        override fun negative(amount: String) = "menos $amount"

        override fun positive(amount: String) = "más $amount"
    }
    private val render = ReciboRenderModel(MoneyFormatter(labels))

    private fun plain(digits: String) = MoneyTextValue(MoneySign.None, "", digits, "$digits pesos")

    private fun minus(digits: String) =
        MoneyTextValue(MoneySign.Minus, "", digits, "menos $digits pesos")

    private val food = categoryIn(1, "Alimentación", 900_000, icon = "restaurant")
    private val coffee = categoryIn(2, "Café", 300_000)
    private val transport = categoryIn(3, "Transporte", 200_000)
    private val leisure = categoryIn(4, "Entretenimiento", 100_000)
    private val art = categoryIn(5, "Arte", null)
    private val everything = listOf(art, transport, leisure, food, coffee)

    private var next = 100

    private fun spent(category: Int, month: String, vararg amounts: Long): List<Expense> =
        amounts.mapIndexed { index, amount ->
            storedExpense(next++, category, "$month-%02d".format(index + 1), amount)
        }

    /** 23 October: nine days left, today included. */
    private fun october(): List<Expense> = spent(1, "2026-10", 600_000, 12_400) +
        spent(2, "2026-10", 50_000, 30_000, 20_000) +
        spent(3, "2026-10", 145_000) +
        spent(4, "2026-10", 121_500) +
        spent(5, "2026-10", 5_000, 5_000, 5_000, 5_000, 5_000, 5_000, 5_000, 5_000)

    private fun shown(snapshot: SnapshotState) = render.recibo(snapshot) as ReciboState.Month

    @Test
    fun theMonthInProgressSaysWhatIsLeftWithItsVerdictDaysAndDailyAmount() {
        val state = shown(SnapshotState.Ready(monthOf("2026-10-23", everything, october())))

        // Limits 900.000 + 300.000 + 200.000 + 100.000 = 1.500.000. Spent 612.400 +
        // 100.000 + 145.000 + 121.500 + 40.000 = 1.018.900. Left 481.100; over nine
        // days that is 53.455,5, shown as 53.450. Below the limits: Al día.
        assertEquals(
            ReciboSummary(
                BudgetFigure(ReciboHeadline.Remaining, plain("481.100"), ReciboStamp.OnTrack),
                days = 9,
                perDay = plain("53.450"),
                perDayExhausted = false
            ),
            state.summary
        )
        assertEquals(false, state.stale)
    }

    @Test
    fun categoriesComeMostUsedFirstWithUnlimitedLastAndEachOwnExactFigures() {
        val state = shown(SnapshotState.Ready(monthOf("2026-10-23", everything, october())))

        assertEquals(
            listOf("Café", "Alimentación", "Entretenimiento", "Transporte", "Arte"),
            state.categories.map { it.name }
        )
        val byName = state.categories.associateBy { it.name }
        // 612.400 of 900.000: below its limit.
        assertEquals(
            ReciboCategory(
                id = uuid(1),
                name = "Alimentación",
                iconKey = "restaurant",
                archived = false,
                figure = BudgetFigure(
                    ReciboHeadline.Remaining,
                    plain("287.600"),
                    ReciboStamp.OnTrack
                ),
                cellAmount = plain("287.600"),
                spent = plain("612.400"),
                limit = plain("900.000"),
                fraction = byName.getValue("Alimentación").fraction,
                over = false,
                expenseCount = 2
            ),
            byName.getValue("Alimentación")
        )
        // 145.000 of 200.000: below its limit, even though this pace would end near 195.435.
        assertEquals(
            BudgetFigure(ReciboHeadline.Remaining, plain("55.000"), ReciboStamp.OnTrack),
            byName.getValue("Transporte").figure
        )
        // 121.500 of 100.000: the cell keeps the minus, the sentence says it in words.
        val over = byName.getValue("Entretenimiento")
        assertEquals(
            BudgetFigure(ReciboHeadline.Overspent, plain("21.500"), ReciboStamp.Over),
            over.figure
        )
        assertEquals(minus("21.500"), over.cellAmount)
        assertEquals(true, over.over)
        assertEquals(plain("100.000"), over.limit)
        // No limit: what was spent, nothing to fill and nothing to judge.
        val open = byName.getValue("Arte")
        assertEquals(
            BudgetFigure(ReciboHeadline.Spent, plain("40.000"), ReciboStamp.NoLimit),
            open.figure
        )
        assertEquals(plain("40.000"), open.cellAmount)
        assertNull(open.limit)
        assertNull(open.fraction)
        assertEquals(false, open.over)
        assertEquals(8, open.expenseCount)
    }

    @Test
    fun theCircleFillsWithTheShareSpentAndIsFullWhenOverOrWithNothingToSpend() {
        val nothing = categoryIn(6, "Nada", 0)
        val untouched = categoryIn(7, "Sin tocar", 50_000)
        val rows = october() + spent(6, "2026-10", 1_000)
        val state = shown(
            SnapshotState.Ready(monthOf("2026-10-23", everything + nothing + untouched, rows))
        )
        val fill = state.categories.associate { it.name to it.fraction }

        // 100.000 of 300.000 and 145.000 of 200.000.
        assertEquals(0.3333f, fill.getValue("Café")!!, 0.0001f)
        assertEquals(0.725f, fill.getValue("Transporte")!!, 0.0001f)
        assertEquals(1f, fill.getValue("Entretenimiento"))
        assertEquals(1f, fill.getValue("Nada"))
        assertEquals(0f, fill.getValue("Sin tocar"))
    }

    @Test
    fun aFinishedMonthSaysWhatWasLeftOrByHowMuchItWentOverAndNothingPerDay() {
        val september = YearMonth.of(2026, 9)
        val within = shown(
            SnapshotState.Ready(
                monthOf("2026-10-23", listOf(food, coffee), spent(1, "2026-09", 700_000), september)
            )
        )
        val over = shown(
            SnapshotState.Ready(
                monthOf(
                    "2026-10-23",
                    listOf(food, coffee),
                    spent(1, "2026-09", 700_000) + spent(2, "2026-09", 300_000, 250_000),
                    september
                )
            )
        )

        // Limits 1.200.000: 700.000 leaves 500.000; 1.250.000 is 50.000 over.
        assertEquals(
            ReciboSummary(
                BudgetFigure(ReciboHeadline.LeftOver, plain("500.000"), ReciboStamp.WithinLimit),
                days = null,
                perDay = null,
                perDayExhausted = false
            ),
            within.summary
        )
        assertEquals(
            ReciboSummary(
                BudgetFigure(ReciboHeadline.Overspent, plain("50.000"), ReciboStamp.OverLimit),
                days = null,
                perDay = null,
                perDayExhausted = false
            ),
            over.summary
        )
        assertEquals(
            listOf(
                BudgetFigure(ReciboHeadline.Overspent, plain("250.000"), ReciboStamp.OverLimit),
                BudgetFigure(ReciboHeadline.LeftOver, plain("200.000"), ReciboStamp.WithinLimit)
            ),
            over.categories.map { it.figure }
        )
    }

    @Test
    fun spendingExactlyTheLimitLeavesZeroAndIsNotOver() {
        val state = shown(
            SnapshotState.Ready(
                monthOf(
                    "2026-10-23",
                    listOf(coffee),
                    spent(2, "2026-09", 300_000),
                    YearMonth.of(2026, 9)
                )
            )
        )

        assertEquals(
            BudgetFigure(ReciboHeadline.LeftOver, plain("0"), ReciboStamp.WithinLimit),
            state.summary.figure
        )
        assertEquals(false, state.categories.single().over)
        assertEquals(plain("0"), state.categories.single().cellAmount)
    }

    @Test
    fun spendingExactlyTheLimitThisMonthIsAlLimiteWithNothingLeftPerDay() {
        val state = shown(
            SnapshotState.Ready(monthOf("2026-10-23", listOf(coffee), spent(2, "2026-10", 300_000)))
        )

        assertEquals(
            ReciboSummary(
                BudgetFigure(ReciboHeadline.Remaining, plain("0"), ReciboStamp.AtLimit),
                days = 9,
                perDay = plain("0"),
                perDayExhausted = false
            ),
            state.summary
        )
        assertEquals(
            BudgetFigure(ReciboHeadline.Remaining, plain("0"), ReciboStamp.AtLimit),
            state.categories.single().figure
        )
        assertEquals(false, state.categories.single().over)
    }

    @Test
    fun eachStampHasTheInkOfWhatItSays() {
        assertEquals(StampInk.Good, stampInk(ReciboStamp.OnTrack))
        assertEquals(StampInk.Good, stampInk(ReciboStamp.WithinLimit))
        assertEquals(StampInk.Neutral, stampInk(ReciboStamp.AtLimit))
        assertEquals(StampInk.Neutral, stampInk(ReciboStamp.NoLimit))
        assertEquals(StampInk.Neutral, stampInk(ReciboStamp.Paused))
        assertEquals(StampInk.Neutral, stampInk(ReciboStamp.NotSet))
        assertEquals(StampInk.Over, stampInk(ReciboStamp.Over))
        assertEquals(StampInk.Over, stampInk(ReciboStamp.OverLimit))
    }

    @Test
    fun anOverspentMonthInProgressHasNothingLeftPerDayAndSaysSo() {
        val state = shown(
            SnapshotState.Ready(
                monthOf("2026-10-31", listOf(coffee), spent(2, "2026-10", 300_001))
            )
        )

        assertEquals(
            ReciboSummary(
                BudgetFigure(ReciboHeadline.Overspent, plain("1"), ReciboStamp.Over),
                days = 1,
                perDay = plain("0"),
                perDayExhausted = true
            ),
            state.summary
        )
    }

    @Test
    fun withNoLimitAnywhereItSaysWhatWasSpentAndJudgesNothing() {
        val state = shown(
            SnapshotState.Ready(monthOf("2026-10-23", listOf(art), spent(5, "2026-10", 80_000)))
        )

        assertEquals(
            ReciboSummary(
                BudgetFigure(ReciboHeadline.Spent, plain("80.000"), ReciboStamp.NoLimit),
                days = null,
                perDay = null,
                perDayExhausted = false
            ),
            state.summary
        )
    }

    @Test
    fun aPausedOrUnconfiguredCategoryShowsItsSpendingAndSaysWhyThereIsNoLimit() {
        val paused = categoryIn(8, "Vieja", null, archived = true)
            .copy(state = MonthBudgetState.Paused)
        val unset = categoryIn(9, "Nueva", null).copy(state = MonthBudgetState.Unconfigured)
        val state = shown(
            SnapshotState.Ready(
                monthOf("2026-10-23", listOf(paused, unset), spent(8, "2026-10", 7_000))
            )
        )

        assertEquals(
            listOf(
                Triple("Vieja", true, ReciboStamp.Paused),
                Triple("Nueva", false, ReciboStamp.NotSet)
            ),
            state.categories.map { Triple(it.name, it.archived, it.figure.stamp) }
        )
        assertEquals(plain("7.000"), state.categories.first().cellAmount)
    }

    @Test
    fun amountsTooLargeForAnyScreenAreStillWholeAndExact() {
        val huge = categoryIn(1, "Grande", 9_000_000_000_000_000)
        val state = shown(
            SnapshotState.Ready(
                monthOf(
                    "2026-10-31",
                    listOf(huge),
                    spent(1, "2026-10", 999_999_999_999, 999_999_999_999, 1)
                )
            )
        )

        // 9.000.000.000.000.000 less 1.999.999.999.999.
        assertEquals(plain("8.998.000.000.000.001"), state.summary.figure.amount)
        assertEquals(plain("1.999.999.999.999"), state.categories.single().spent)
    }

    @Test
    fun nothingReadYetIsLoadingAndAMonthWithoutCategoriesIsTheFirstRun() {
        assertEquals(ReciboState.Loading, render.recibo(SnapshotState.Loading))
        assertEquals(
            ReciboState.FirstRun,
            render.recibo(SnapshotState.Ready(monthOf("2026-10-23", emptyList())))
        )
    }

    @Test
    fun aFailedReadIsNeverAnEmptyMonthAndOlderFiguresAreMarkedAsOlder() {
        val failure = SnapshotFailure.Storage(StorageFailure.Unavailable("SQLiteException"))
        val earlier = monthOf("2026-10-23", everything, october())

        val first = render.recibo(SnapshotState.Failed(failure, lastGood = null))
        val later = render.recibo(SnapshotState.Failed(failure, lastGood = earlier))

        assertEquals(ReciboState.Failed, first)
        assertEquals(shown(SnapshotState.Ready(earlier)).copy(stale = true), later)
        assertEquals(true, (later as ReciboState.Month).stale)
    }
}
