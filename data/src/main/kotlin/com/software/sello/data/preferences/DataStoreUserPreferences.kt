package com.software.sello.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.software.sello.domain.model.AppearancePreference
import com.software.sello.domain.model.CategoryOrder
import com.software.sello.domain.model.CategoryOrderDirection
import com.software.sello.domain.model.CategoryOrderingPreference
import com.software.sello.domain.model.CorrectionAnnouncementPreference
import com.software.sello.domain.model.InkPreference
import com.software.sello.domain.model.ThemePreference
import com.software.sello.domain.port.AppearancePreferences
import com.software.sello.domain.port.CategoryOrderingPreferences
import com.software.sello.domain.port.CorrectionAnnouncementPreferences
import com.software.sello.domain.port.PreferenceFailure
import com.software.sello.domain.port.PreferenceRead
import com.software.sello.domain.port.PreferenceWrite
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * The sole owner of Sello's nonfinancial preferences. Financial values deliberately
 * have no key here: they remain Room data with the command/recovery guarantees it has.
 */
class DataStoreUserPreferences internal constructor(private val data: DataStore<Preferences>) :
    AppearancePreferences,
    CategoryOrderingPreferences,
    CorrectionAnnouncementPreferences {
    constructor(context: Context, scope: CoroutineScope) : this(
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { context.preferencesDataStoreFile(FILE_NAME) }
        )
    )

    override fun observeAppearance(): Flow<PreferenceRead<AppearancePreference>> =
        observe(::appearance)

    override suspend fun setTheme(theme: ThemePreference): PreferenceWrite =
        write { this[THEME] = theme.stored }

    override suspend fun setInk(ink: InkPreference): PreferenceWrite =
        write { this[INK] = ink.stored }

    override fun observeCategoryOrdering(): Flow<PreferenceRead<CategoryOrderingPreference>> =
        observe(::categoryOrdering)

    override suspend fun setCategoryOrder(order: CategoryOrder): PreferenceWrite =
        write { this[CATEGORY_ORDER] = order.stored }

    override suspend fun setCategoryOrderDirection(
        direction: CategoryOrderDirection
    ): PreferenceWrite = write { this[CATEGORY_DIRECTION] = direction.stored }

    override fun observeCorrectionAnnouncements():
        Flow<PreferenceRead<CorrectionAnnouncementPreference>> =
        observe(::correctionAnnouncements)

    override suspend fun dismissPastExpenseAnnouncement(): PreferenceWrite =
        write { this[PAST_EXPENSE_SEEN] = true }

    override suspend fun dismissPastLimitAnnouncement(): PreferenceWrite =
        write { this[PAST_LIMIT_SEEN] = true }

    private fun <T> observe(decoder: (Preferences) -> Decoded<T>): Flow<PreferenceRead<T>> = flow {
        var lastGood: T? = null
        try {
            data.data.collect { stored ->
                when (val decoded = decoder(stored)) {
                    is Decoded.Value -> {
                        lastGood = decoded.value
                        emit(PreferenceRead.Ready(decoded.value))
                    }

                    Decoded.Invalid -> emit(
                        PreferenceRead.Failed(PreferenceFailure.InvalidValue, lastGood)
                    )
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            emit(PreferenceRead.Failed(PreferenceFailure.Unavailable, lastGood))
        } catch (_: ClassCastException) {
            emit(PreferenceRead.Failed(PreferenceFailure.InvalidValue, lastGood))
        }
    }

    private suspend fun write(
        change: androidx.datastore.preferences.core.MutablePreferences.() -> Unit
    ): PreferenceWrite = try {
        data.edit(change)
        PreferenceWrite.Saved
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: IOException) {
        PreferenceWrite.Failed(PreferenceFailure.Unavailable)
    }

    private fun appearance(stored: Preferences): Decoded<AppearancePreference> {
        val theme = when (stored[THEME] ?: ThemePreference.System.stored) {
            "system" -> ThemePreference.System
            "light" -> ThemePreference.Light
            "dark" -> ThemePreference.Dark
            else -> return Decoded.Invalid
        }
        val ink = when (stored[INK] ?: InkPreference.Cobalto.stored) {
            "cobalto" -> InkPreference.Cobalto
            "violeta" -> InkPreference.Violeta
            else -> return Decoded.Invalid
        }
        return Decoded.Value(AppearancePreference(theme, ink))
    }

    private fun categoryOrdering(stored: Preferences): Decoded<CategoryOrderingPreference> {
        val order = when (stored[CATEGORY_ORDER] ?: CategoryOrder.MostUsed.stored) {
            "most_used" -> CategoryOrder.MostUsed
            "alphabetical" -> CategoryOrder.Alphabetical
            "amount_spent" -> CategoryOrder.AmountSpent
            "limit" -> CategoryOrder.Limit
            "latest_expense_added" -> CategoryOrder.LatestExpenseAdded
            else -> return Decoded.Invalid
        }
        val defaultDirection = CategoryOrderDirection.Descending.stored
        val direction = when (stored[CATEGORY_DIRECTION] ?: defaultDirection) {
            "descending" -> CategoryOrderDirection.Descending
            "ascending" -> CategoryOrderDirection.Ascending
            else -> return Decoded.Invalid
        }
        return Decoded.Value(CategoryOrderingPreference(order, direction))
    }

    private fun correctionAnnouncements(stored: Preferences) = Decoded.Value(
        CorrectionAnnouncementPreference(
            pastExpenseSeen = stored[PAST_EXPENSE_SEEN] ?: false,
            pastLimitSeen = stored[PAST_LIMIT_SEEN] ?: false
        )
    )

    private sealed interface Decoded<out T> {
        data class Value<T>(val value: T) : Decoded<T>
        data object Invalid : Decoded<Nothing>
    }

    private companion object {
        const val FILE_NAME = "sello-preferences"

        val THEME = stringPreferencesKey("appearance_theme_v1")
        val INK = stringPreferencesKey("appearance_ink_v1")
        val CATEGORY_ORDER = stringPreferencesKey("recibo_category_order_v1")
        val CATEGORY_DIRECTION = stringPreferencesKey("recibo_category_direction_v1")
        val PAST_EXPENSE_SEEN = booleanPreferencesKey("past_expense_correction_seen_v1")
        val PAST_LIMIT_SEEN = booleanPreferencesKey("past_limit_correction_seen_v1")

        val ThemePreference.stored: String
            get() = when (this) {
                ThemePreference.System -> "system"
                ThemePreference.Light -> "light"
                ThemePreference.Dark -> "dark"
            }

        val InkPreference.stored: String
            get() = when (this) {
                InkPreference.Cobalto -> "cobalto"
                InkPreference.Violeta -> "violeta"
            }

        val CategoryOrder.stored: String
            get() = when (this) {
                CategoryOrder.MostUsed -> "most_used"
                CategoryOrder.Alphabetical -> "alphabetical"
                CategoryOrder.AmountSpent -> "amount_spent"
                CategoryOrder.Limit -> "limit"
                CategoryOrder.LatestExpenseAdded -> "latest_expense_added"
            }

        val CategoryOrderDirection.stored: String
            get() = when (this) {
                CategoryOrderDirection.Descending -> "descending"
                CategoryOrderDirection.Ascending -> "ascending"
            }
    }
}
