package com.software.sello.domain.model

import java.time.LocalDate
import java.time.YearMonth

data class DailySpending(val date: LocalDate, val spent: Money, val cumulative: Money)

data class SpendingMonth(
    val month: YearMonth,
    val spent: Money,
    val limit: Money?,
    val forecast: Forecast?,
    val expenseCount: Int
)

data class ResumenSnapshot(
    val budget: MonthlySnapshot,
    val income: Money,
    val netFlow: Money,
    val allocated: Money,
    val unallocated: Money,
    val actualVerdict: PastVerdict?,
    val daily: List<DailySpending>,
    val history: List<SpendingMonth>,
    val futureDatedIncomes: Int
)
