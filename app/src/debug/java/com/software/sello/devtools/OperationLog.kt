package com.software.sello.devtools

import com.software.sello.domain.model.OperationId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** One write as its caller saw it. [fault] is the simulated fault that shaped that view. */
data class LoggedOperation(
    val operation: OperationId,
    val kind: String,
    val told: String,
    val fault: Fault?
)

/** What storage holds for an operation, read through the real receipt lookup. */
sealed interface StoredOutcome {
    data class Saved(val revision: Long) : StoredOutcome

    /** No receipt: this operation wrote nothing. */
    data object Nothing : StoredOutcome

    /** The lookup itself failed, so nothing is known. */
    data object Unreadable : StoredOutcome
}

/**
 * The writes of this process, newest last, as their callers were told about them. It
 * records; it never decides or changes an outcome. The inspector pairs each entry
 * with [StoredOutcome] from storage itself.
 */
class OperationLog(private val limit: Int = 50) {
    private val entries = MutableStateFlow<List<LoggedOperation>>(emptyList())
    val state: StateFlow<List<LoggedOperation>> = entries.asStateFlow()

    fun record(entry: LoggedOperation) = entries.update { (it + entry).takeLast(limit) }
}
