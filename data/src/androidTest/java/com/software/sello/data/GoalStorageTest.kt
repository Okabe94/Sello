package com.software.sello.data

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.data.local.SelloDatabase
import com.software.sello.data.local.entity.GoalEntity
import com.software.sello.data.mapper.toHistory
import com.software.sello.domain.model.GoalPlace
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.PlaceState
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.policy.GoalPolicy
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Goal rows written straight into the real database and read back through the real
 * queries and mappers. The figures are approved examples MT07 and MT17
 * (docs/testing/goal-examples.md).
 */
@RunWith(AndroidJUnit4::class)
class GoalStorageTest {
    @get:Rule
    val files = DatabaseFiles()

    private val millis = recordedAt.toEpochMilli()
    private val goal = uuid(1)

    private fun SelloDatabase.goal(nextSequence: Long = 4, name: String = "Viaje a la costa") =
        insertRaw(
            "goal",
            "id" to goal,
            "name" to name,
            "icon_key" to "flight",
            "target_month" to "2026-12",
            "completed_on" to null,
            "next_sequence" to nextSequence,
            "version" to 1L,
            "created_at" to millis,
            "updated_at" to millis
        )

    private fun SelloDatabase.event(
        number: Int,
        sequence: Long,
        kind: String,
        pesos: Long,
        date: String?,
        recordedOn: String = "2026-10-23",
        note: String? = null,
        currency: String = "COP"
    ) = insertRaw(
        "goal_event",
        "id" to uuid(number),
        "goal_id" to goal,
        "sequence" to sequence,
        "kind" to kind,
        "amount_minor" to pesos,
        "currency" to currency,
        "effective_date" to date,
        "recorded_on" to recordedOn,
        "note" to note,
        "version" to 1L,
        "created_at" to millis,
        "updated_at" to millis
    )

    private suspend fun SelloDatabase.history() =
        goalDao().byId(goal)!!.toHistory(goalDao().events(goal))

    @Test
    fun aStoredHistoryReadsBackToTheApprovedFigures() = runBlocking {
        val database = files.open()
        database.goal()
        database.event(10, 0, "target", 3_000_000, null, recordedOn = "2026-10-02")
        database.event(11, 1, "contribution", 1_710_000, "2026-10-02", "2026-10-02", "Prima")
        database.event(12, 2, "withdrawal", 400_000, "2026-10-20", note = "Arreglo del carro")

        val progress = GoalPolicy.progress(database.history().valueOrFail()).valueOrFail()

        // MT07: 1.310.000 saved, 43 %, the 50 % stamp kept from 2 October.
        assertEquals(Money.cop(1_310_000), progress.saved)
        assertEquals(43L, progress.percentage)
        assertEquals(PlaceState.Kept(LocalDate.of(2026, 10, 2)), progress.state(GoalPlace.Half))
    }

    @Test
    fun aTargetChangeIsReadInItsRecordingPlace() = runBlocking {
        val database = files.open()
        database.goal()
        database.event(10, 0, "target", 3_000_000, null)
        database.event(11, 1, "contribution", 1_710_000, "2026-10-23")
        database.event(12, 2, "target", 4_000_000, null)

        val progress = GoalPolicy.progress(database.history().valueOrFail()).valueOrFail()

        // MT17: 42 % of the new target; 50 % earned under the old one stays faded.
        assertEquals(42L, progress.percentage)
        assertEquals(PlaceState.Kept(LocalDate.of(2026, 10, 23)), progress.state(GoalPlace.Half))
    }

    @Test
    fun aDamagedGoalOrEventIsAnIntegrityFailureNamingItsColumn() = runBlocking {
        fun damaged(record: String, key: String, field: String) =
            Outcome.Failure(StorageFailure.Integrity(record, key, field))
        val cases = listOf<Pair<(SelloDatabase) -> Unit, Outcome<*, *>>>(
            { db: SelloDatabase -> db.event(10, 0, "target", 3_000_000, "2026-10-23") } to
                damaged("goal_event", uuid(10), "effective_date"),
            { db: SelloDatabase -> db.event(10, 0, "contribution", 3_000_000, null) } to
                damaged("goal_event", uuid(10), "effective_date"),
            { db: SelloDatabase -> db.event(10, 0, "gift", 3_000_000, "2026-10-23") } to
                damaged("goal_event", uuid(10), "kind"),
            { db: SelloDatabase -> db.event(10, 0, "contribution", 0, "2026-10-23") } to
                damaged("goal_event", uuid(10), "amount_minor"),
            { db: SelloDatabase ->
                db.event(10, 0, "contribution", 1_000_000_000_000, "2026-10-23")
            } to
                damaged("goal_event", uuid(10), "amount_minor"),
            { db: SelloDatabase ->
                db.event(10, 0, "contribution", 1, "2026-10-23", currency = "USD")
            } to
                damaged("goal_event", uuid(10), "currency"),
            { db: SelloDatabase ->
                db.event(10, 0, "contribution", 1, "2026-10-23", note = " Prima")
            } to
                damaged("goal_event", uuid(10), "note"),
            { db: SelloDatabase -> db.event(10, 0, "contribution", 1, "2026-02-30") } to
                damaged("goal_event", uuid(10), "effective_date"),
            { db: SelloDatabase -> db.event(10, 4, "contribution", 1, "2026-10-23") } to
                damaged("goal_event", uuid(10), "sequence")
        )
        val database = files.open()
        for ((write, expected) in cases) {
            database.goal()
            write(database)
            assertEquals(expected, database.history())
            database.openHelper.writableDatabase.execSQL("DELETE FROM goal_event")
            database.openHelper.writableDatabase.execSQL("DELETE FROM goal")
        }
    }

    @Test
    fun aGoalNameThatIsNotWhatTheRuleStoresIsDamage() = runBlocking {
        val database = files.open()
        database.goal(name = " Viaje")

        assertEquals(
            Outcome.Failure(StorageFailure.Integrity(GoalEntity.TABLE, goal, "name")),
            database.history()
        )
    }

    @Test
    fun twoEventsCannotShareARecordingPlaceAndAGoalWithEventsCannotBeRemoved() {
        val database = files.open()
        database.goal()
        database.event(10, 0, "target", 3_000_000, null)

        assertThrows(SQLiteConstraintException::class.java) {
            database.event(11, 0, "contribution", 1, "2026-10-23")
        }
        assertThrows(SQLiteConstraintException::class.java) {
            database.openHelper.writableDatabase.execSQL("DELETE FROM goal")
        }
    }
}
