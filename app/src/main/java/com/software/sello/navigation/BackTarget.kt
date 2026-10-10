package com.software.sello.navigation

/** What the Back gesture does next. */
enum class BackTarget {
    /** Close the open sheet or side panel. */
    ClosePanel,

    /** Leave the detail screen for the tab's own screen. */
    LeaveDetail,

    /** From another tab's own screen, go to Recibo. */
    GoToRecibo,

    /** Leave the app. */
    Exit
}

/**
 * One order for everything: a panel closes first, then a detail, then any tab returns
 * to Recibo, and only Back on Recibo itself leaves the app.
 */
fun backTarget(panelOpen: Boolean, onDetail: Boolean, tab: ShellTab): BackTarget = when {
    panelOpen -> BackTarget.ClosePanel
    onDetail -> BackTarget.LeaveDetail
    tab != ShellTab.Recibo -> BackTarget.GoToRecibo
    else -> BackTarget.Exit
}
