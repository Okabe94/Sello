package com.software.sello.devtools

import android.content.Context
import androidx.core.content.edit
import java.time.LocalDate

/**
 * What the debug build remembers about its sandbox from one process to the next.
 *
 * - [active]: this process opened the sandbox's data rather than the normal profile's.
 * - [date]: the sandbox's financial date; none until the sandbox is first entered.
 * - [changed]: a financial change was saved in this sandbox, so its date may no
 *   longer move back without a reset.
 * - [wipe]: the sandbox's data is erased at the next start, before anything opens it.
 */
data class SandboxSession(
    val active: Boolean = false,
    val date: LocalDate? = null,
    val changed: Boolean = false,
    val wipe: Boolean = false
)

/** Where [SandboxSession] is kept. Writes finish before they return. */
interface SandboxSessionStore {
    fun read(): SandboxSession

    fun write(session: SandboxSession)
}

/** The session in the debug app's private preferences, separate from both profiles' data. */
class SharedPreferencesSessionStore(context: Context) : SandboxSessionStore {
    private val preferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override fun read(): SandboxSession = SandboxSession(
        active = preferences.getBoolean(ACTIVE, false),
        date = preferences.getString(DATE, null)?.let(LocalDate::parse),
        changed = preferences.getBoolean(CHANGED, false),
        wipe = preferences.getBoolean(WIPE, false)
    )

    override fun write(session: SandboxSession) {
        // Written before returning, not later: the process may end right after this.
        preferences.edit(commit = true) {
            putBoolean(ACTIVE, session.active)
            putString(DATE, session.date?.toString())
            putBoolean(CHANGED, session.changed)
            putBoolean(WIPE, session.wipe)
        }
    }

    private companion object {
        const val FILE_NAME = "sandbox-session"
        const val ACTIVE = "active"
        const val DATE = "date"
        const val CHANGED = "changed"
        const val WIPE = "wipe"
    }
}
