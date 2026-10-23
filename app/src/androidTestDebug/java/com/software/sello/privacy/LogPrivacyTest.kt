package com.software.sello.privacy

import android.content.Context
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.software.sello.TestCommands
import com.software.sello.TestData
import com.software.sello.domain.model.CategoryOrder
import com.software.sello.domain.model.CategoryOrderDirection
import com.software.sello.domain.model.InkPreference
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.ThemePreference
import com.software.sello.domain.port.AppearancePreferences
import com.software.sello.domain.port.BackupExports
import com.software.sello.domain.port.CategoryOrderingPreferences
import com.software.sello.domain.port.DocumentDestination
import com.software.sello.domain.port.DocumentRead
import com.software.sello.domain.port.DocumentReader
import com.software.sello.domain.port.DocumentSource
import com.software.sello.domain.port.DocumentWrite
import com.software.sello.domain.port.DocumentWriter
import com.software.sello.domain.port.ResetOutcome
import com.software.sello.domain.port.Resets
import com.software.sello.domain.port.RestoreOutcome
import com.software.sello.domain.port.Restores
import com.software.sello.platform.TestDocuments
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

/**
 * Money, names and notes never reach the system log. Synthetic markers are written as
 * a category name, a note and an amount, then carried through a backup file, a
 * restore and a reset with the app's own adapters; afterwards the device log holds
 * none of them. A probe line written first proves the log is really being read.
 */
@RunWith(AndroidJUnit4::class)
class LogPrivacyTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context: Context get() = instrumentation.targetContext
    private val koin get() = GlobalContext.get()
    private lateinit var zone: String

    private val category = "MarcadorCat${UUID.randomUUID().toString().take(6)}"
    private val note = "MarcadorNota${UUID.randomUUID().toString().take(6)}"
    private val amount = 4_271_839L
    private val probe = "SelloLogProbe-${UUID.randomUUID()}"

    @Before
    fun clearLog() {
        TestData.reset()
        zone = TestData.rows("SELECT financial_zone FROM profile").single()
        shell("logcat -c")
    }

    @After
    fun leaveNoFixture() {
        TestData.reset()
        TestData.execute("DELETE FROM operation_receipt")
        TestData.execute("UPDATE profile SET generation = 1, financial_zone = '$zone'")
        File(context.filesDir, "sello-reset").deleteRecursively()
        File(context.filesDir, "sello-restore").deleteRecursively()
        TestCommands.showCurrentMonth()
        runBlocking {
            koin.get<AppearancePreferences>().setTheme(ThemePreference.System)
            koin.get<AppearancePreferences>().setInk(InkPreference.Cobalto)
            koin.get<CategoryOrderingPreferences>().setCategoryOrder(CategoryOrder.MostUsed)
            koin.get<CategoryOrderingPreferences>()
                .setCategoryOrderDirection(CategoryOrderDirection.Descending)
        }
    }

    private fun shell(command: String): String {
        val output = instrumentation.uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(output).use { it.readBytes() }
            .toString(Charsets.UTF_8)
    }

    private fun <T> Outcome<T, *>.value(): T = (this as Outcome.Success).value

    @Test
    fun noAmountNameOrNoteReachesTheLogThroughBackupRestoreAndReset() = runBlocking {
        Log.i("SelloLogProbe", probe)
        TestCommands.createCategory(category, 900_000)
        TestCommands.createExpense(category, TestCommands.today, amount, note = note)

        val prepared = koin.get<BackupExports>().prepare().value()
        val document = TestDocuments.uri("ok", "respaldo-${UUID.randomUUID()}.json").toString()
        val written = koin.get<DocumentWriter>()
            .write(DocumentDestination(document), prepared.bytes)
        assertTrue("$written", written is DocumentWrite.Completed)
        val read = koin.get<DocumentReader>().read(DocumentSource(document), 10_000_000)
        val bytes = (read as DocumentRead.Read).bytes
        assertTrue(note in bytes.toString(Charsets.UTF_8))

        val restores = koin.get<Restores>()
        val restored = restores.replace(restores.stage(bytes).value())
        assertTrue("$restored", restored is RestoreOutcome.Restored)
        val resets = koin.get<Resets>()
        val erased = resets.reset(
            resets.preview().value(),
            OperationId.of(UUID.randomUUID().toString()).value()
        )
        assertTrue("$erased", erased is ResetOutcome.Erased)

        val log = shell("logcat -d -v raw")
        assertTrue("the log was not read: the probe is missing", probe in log)
        for (marker in listOf(category, note, "$amount", "4.271.839")) {
            assertTrue("$marker reached the log", marker !in log)
        }
    }
}
