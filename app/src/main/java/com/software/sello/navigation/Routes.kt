package com.software.sello.navigation

import kotlinx.serialization.Serializable

/*
 * Routes are identifiers only. A route never carries an amount, a record or a result:
 * a screen that needs data gets an identifier and reads the data itself.
 */

/** The start destination and the place Back returns to before leaving the app. */
@Serializable
data object ReciboRoute

/**
 * The tabs that exist. A tab is added here when its screen works; there is no
 * disabled or placeholder tab. With a single tab the tab bar is not shown.
 */
enum class ShellTab(val id: String) {
    Recibo("recibo")
}
