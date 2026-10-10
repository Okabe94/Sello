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
 * The new-category form. [forEntry] says it was opened because recording an expense
 * needs a category first, so the form explains that.
 */
@Serializable
data class CategoryEditorRoute(val forEntry: Boolean = false)

/** Where the editor leaves the new category's identifier for the screen that opened it. */
const val CREATED_CATEGORY_RESULT = "result.createdCategoryId"

/**
 * The tabs that exist. A tab is added here when its screen works; there is no
 * disabled or placeholder tab. With a single tab the tab bar is not shown.
 */
enum class ShellTab(val id: String) {
    Recibo("recibo")
}
