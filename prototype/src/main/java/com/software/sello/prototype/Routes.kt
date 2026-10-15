package com.software.sello.prototype

import kotlinx.serialization.Serializable

// Tabs. Each is a nested graph, so it keeps its own back stack when another tab is chosen.
@Serializable data object ReciboTab

@Serializable data object ResumenTab

@Serializable data object PatrimonioTab

// Recibo tab.
@Serializable data object ReciboRoute

@Serializable data class CategoryRoute(val id: String)

@Serializable data class ExpenseListRoute(val id: String)

@Serializable data object SettingsRoute

@Serializable data object BackupRoute

// Resumen tab.
@Serializable data object ResumenRoute

@Serializable data object IncomeRoute

@Serializable data object RecurringRoute

// Patrimonio tab.
@Serializable data object PatrimonioRoute

@Serializable data class InvestmentRoute(val id: String)

@Serializable data class GoalRoute(val id: String)

// Full-screen sheets and editors: they cover the tabs and the dock.
@Serializable
data class AnotarRoute(
    val categoryId: String? = null,
    val income: Boolean = false,
    val edit: Boolean = false,
    val recurring: Boolean = false
)

@Serializable data class VariosRoute(val movements: Boolean = false, val categoryId: String? = null)

@Serializable data class CategoryEditorRoute(val id: String? = null, val forEntry: Boolean = false)

@Serializable data object IncomeReceiptRoute

@Serializable data class InvestmentEditorRoute(val id: String? = null)

@Serializable data class MovementRoute(val id: String)

@Serializable data class GoalEditorRoute(val id: String? = null)

@Serializable data class AportarRoute(val id: String, val amount: Long? = null)

@Serializable data class AporteReceiptRoute(val id: String, val amount: Long)

@Serializable data class GoalDoneRoute(val id: String)

/** Prototype only: every screen in one list, to jump straight to it. */
@Serializable data object MapRoute
