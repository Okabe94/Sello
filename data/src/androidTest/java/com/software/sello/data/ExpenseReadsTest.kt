package com.software.sello.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.ArchiveCategory
import com.software.sello.domain.port.ExpenseCommandOutcome
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Reading back what the expense command stored, on a real database file. */
@RunWith(AndroidJUnit4::class)
class ExpenseReadsTest {
    @get:Rule
    val files = DatabaseFiles()

    private val ledger by lazy { TestLedger(files).also { it.start() } }

    private fun id(number: Int) = ExpenseId.of(uuid(number)).valueOrFail()

    @Test
    fun aSavedExpenseReadsBackExactlyAsItsReceiptSays() = runBlocking {
        ledger.categories.submit(ledger.createCategory("Mercado"))
        val saved = ledger.expenses.create(ledger.createExpense(100, "2026-10-03", 999_999_999_999))
        val receipt = (saved as ExpenseCommandOutcome.Committed).expense

        val read = ledger.expenseReads.byId(receipt.expenseId).valueOrFail()!!

        assertEquals(receipt.expenseId, read.id)
        assertEquals(ledger.categoryId(100), read.categoryId)
        assertEquals("2026-10-03", read.date.toString())
        assertEquals(999_999_999_999, read.amount.money.minorUnits)
        assertEquals(ledger.auditMillis, read.createdAt)
        assertEquals(receipt.receipt.committedAt, read.createdAt)
        assertEquals(Outcome.Success(null), ledger.expenseReads.byId(id(999)))
    }

    @Test
    fun theLastUsedCategoryIsTheOneOfTheExpenseAddedLastNotTheLatestDate() = runBlocking {
        ledger.categories.submit(ledger.createCategory("Mercado"))
        ledger.categories.submit(ledger.createCategory("Transporte"))
        val none = ledger.expenseReads.lastUsedCategory()
        ledger.expenses.create(ledger.createExpense(100, "2026-10-08", 5_000))
        // Added later, though dated earlier.
        ledger.expenses.create(ledger.createExpense(101, "2026-10-01", 7_000))

        val last = ledger.expenseReads.lastUsedCategory()
        ledger.categories.submit(ArchiveCategory(ledger.operation(), 1, ledger.categoryId(101), 1))

        assertEquals(Outcome.Success(null), none)
        assertEquals(Outcome.Success(ledger.categoryId(101)), last)
        // Still the answer after archiving; whether it can take an entry is the caller's check.
        assertEquals(
            Outcome.Success(ledger.categoryId(101)),
            ledger.expenseReads.lastUsedCategory()
        )
    }

    @Test
    fun aDamagedExpenseIsAFailureNotAMissingOne() = runBlocking {
        ledger.categories.submit(ledger.createCategory("Mercado"))
        ledger.expenses.create(ledger.createExpense(100, "2026-10-03", 5_000))
        ledger.database.openHelper.writableDatabase.execSQL("UPDATE expense SET amount_minor = 0")

        assertEquals(
            Outcome.Failure(StorageFailure.Integrity("expense", uuid(101), "amount_minor")),
            ledger.expenseReads.byId(id(101))
        )
    }
}
