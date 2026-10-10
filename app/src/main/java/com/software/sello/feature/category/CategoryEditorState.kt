package com.software.sello.feature.category

/** Why the name cannot be used. */
enum class NameError { Required, TooLong, NotAllowed, Taken }

/** Why the limit cannot be used. The text typed is kept as it was. */
enum class LimitError { Required, NotAnAmount }

/** Why saving did not finish. None of these means the category was created. */
enum class SaveError {
    /** Storage failed and nothing was written. */
    NotSaved,

    /** The data was replaced meanwhile (restore or reset). Trying again uses the new data. */
    DataChanged,

    /** It is not known yet whether it was saved; asking again will tell. */
    Unknown
}

/** A limit offered as a shortcut. [label] is the amount as it is shown. */
data class LimitSuggestion(val pesos: Long, val label: String)

/** How the editor ended. Kept in state so a rotation cannot lose it. */
sealed interface EditorResult {
    /** A receipt confirmed the category. [categoryId] is its real identifier. */
    data class Created(val categoryId: String) : EditorResult

    data object Discarded : EditorResult
}

/**
 * The new-category form. [name] and [limitText] are exactly what was typed. The limit
 * is either [unlimited] or the amount in [limitText]; an empty amount is never read
 * as zero.
 */
data class CategoryEditorState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    /** Opened because recording an expense needs a category first. */
    val forEntry: Boolean = false,
    val name: String = "",
    val nameError: NameError? = null,
    val iconKeys: List<String> = emptyList(),
    val iconKey: String = "",
    val unlimited: Boolean = true,
    val limitText: String = "",
    val limitError: LimitError? = null,
    val suggestions: List<LimitSuggestion> = emptyList(),
    val busy: Boolean = false,
    val saveError: SaveError? = null,
    val confirmDiscard: Boolean = false,
    val result: EditorResult? = null
) {
    /** Nothing can be sent without a name, while sending, or before the form is ready. */
    val canSubmit: Boolean get() = !loading && !loadFailed && !busy && name.isNotBlank()

    /** While saving, or while a save's outcome is unknown, the draft must not change. */
    val editable: Boolean get() = !busy && saveError != SaveError.Unknown
}

sealed interface CategoryEditorAction {
    data class NameChanged(val text: String) : CategoryEditorAction

    data class IconPicked(val key: String) : CategoryEditorAction

    data class UnlimitedChanged(val unlimited: Boolean) : CategoryEditorAction

    data class LimitChanged(val text: String) : CategoryEditorAction

    /** One of the suggested limits, in whole pesos. */
    data class SuggestionPicked(val pesos: Long) : CategoryEditorAction

    data object Submit : CategoryEditorAction

    /** Reload the form, or ask again about a save whose outcome is unknown. */
    data object Retry : CategoryEditorAction

    data object Back : CategoryEditorAction

    data object DiscardConfirmed : CategoryEditorAction

    data object KeepEditing : CategoryEditorAction
}
