package com.software.sello.data

import com.software.sello.data.local.SelloDatabase
import com.software.sello.data.repository.RoomCategoryCommands
import com.software.sello.data.repository.RoomCategoryReads
import com.software.sello.data.repository.RoomExpenseCommands
import com.software.sello.data.repository.RoomExpenseReads
import com.software.sello.data.repository.RoomFinancialProfileStore
import com.software.sello.data.repository.RoomMonthlySnapshots
import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryName
import com.software.sello.domain.model.IconKey
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.Note
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.OperationKind
import com.software.sello.domain.model.OperationReceipt
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.port.CategoryCommands
import com.software.sello.domain.port.CategoryReads
import com.software.sello.domain.port.CommittedCategoryChange
import com.software.sello.domain.port.CreateCategory
import com.software.sello.domain.port.CreateExpense
import com.software.sello.domain.port.ExpenseCommands
import com.software.sello.domain.port.ExpenseReads
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.FinancialDay
import com.software.sello.domain.port.MonthlySnapshots
import com.software.sello.domain.port.RecordIdSource
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking

/**
 * The real workflows over a real database file, with the three things a test needs
 * to control: the financial day, the real time stamped on rows, and new identifiers.
 * New records are numbered from 100 and operations from 1000.
 */
class TestLedger(private val files: DatabaseFiles) {
    val zone: ZoneId = ZoneId.of("America/Bogota")
    val auditMillis: Instant = Instant.parse("2026-10-09T15:04:05.678Z")
    private val audit = Instant.parse("2026-10-09T15:04:05.678912Z")
    private val today = MutableStateFlow(FinancialDay(LocalDate.of(2026, 10, 9), zone))
    private val clock = object : FinancialClock {
        override val today = this@TestLedger.today
    }

    var nextRecord = 100
    var whenAnIdIsTaken: () -> Unit = {}
    private val ids = RecordIdSource {
        whenAnIdIsTaken()
        uuid(nextRecord++)
    }
    private var nextOperation = 1000

    lateinit var database: SelloDatabase
    lateinit var categories: CategoryCommands
    lateinit var expenses: ExpenseCommands
    lateinit var reads: CategoryReads
    lateinit var snapshots: MonthlySnapshots
    lateinit var expenseReads: ExpenseReads

    /** A fresh handle on the same file, as after a process restart. */
    fun open() {
        database = files.open()
        categories = RoomCategoryCommands(database, { audit }, clock, ids)
        expenses = RoomExpenseCommands(database, { audit }, clock, ids)
        reads = RoomCategoryReads(database)
        snapshots = RoomMonthlySnapshots(database, clock)
        expenseReads = RoomExpenseReads(database)
    }

    /** A new installation: a profile and nothing else. */
    fun start() = runBlocking {
        open()
        RoomFinancialProfileStore(database) { audit }.establish(zone)
    }

    fun reopen() {
        database.close()
        open()
    }

    /** Moves the financial day, as a real month change or the sandbox would. */
    fun goTo(date: String) {
        today.value = FinancialDay(LocalDate.parse(date), zone)
    }

    fun operation(): OperationId = operation(nextOperation++)

    fun operation(number: Int): OperationId = OperationId.of(uuid(number)).valueOrFail()

    fun categoryId(number: Int): CategoryId = CategoryId.of(uuid(number)).valueOrFail()

    fun name(text: String): CategoryName = CategoryName.of(text).valueOrFail()

    fun icon(key: String): IconKey = IconKey.of(key).valueOrFail()

    fun createCategory(
        name: String,
        limit: BudgetLimit = finite(100_000),
        operation: OperationId = operation(),
        icon: String = "shopping_cart",
        generation: Long = 1
    ) = CreateCategory(operation, generation, name(name), icon(icon), limit)

    fun createExpense(categoryNumber: Int, date: String, pesos: Long) = CreateExpense(
        operationId = operation(),
        generation = 1,
        categoryId = categoryId(categoryNumber),
        amount = TransactionAmount.of(Money.cop(pesos)).valueOrFail(),
        date = LocalDate.parse(date),
        note = Note.of(null).valueOrFail()
    )

    fun change(operation: OperationId, kind: OperationKind, category: Int, revision: Long) =
        CommittedCategoryChange(
            OperationReceipt(operation, kind, generation = 1, revision = revision, auditMillis),
            categoryId(category)
        )

    fun revision(): Long = long("SELECT revision FROM profile")

    fun long(sql: String): Long = database.openHelper.readableDatabase.query(sql).use {
        it.moveToFirst()
        it.getLong(0)
    }

    /** Every row of [table] as text, columns in [columns] order, rows in that order too. */
    fun rows(table: String, vararg columns: String): List<String> {
        val list = columns.joinToString(", ")
        return database.openHelper.readableDatabase
            .query("SELECT $list FROM $table ORDER BY $list")
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(columns.indices.joinToString("|") { cursor.getString(it) ?: "null" })
                    }
                }
            }
    }
}
