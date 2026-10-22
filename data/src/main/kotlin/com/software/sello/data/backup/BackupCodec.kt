package com.software.sello.data.backup

import com.software.sello.domain.model.AutomaticBudget
import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.IncomeSource
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.policy.EffectiveDates
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/** The fixed parts of the format; see docs/data/backup-format.md. */
object BackupFormat {
    const val FORMAT = "sello-backup"
    const val VERSION = 1L
    const val MIME_TYPE = "application/json"

    private val stamp = DateTimeFormatter.ofPattern("uuuu-MM-dd-HHmm")

    /** `sello-respaldo-2026-10-22-1430.json`, in the backup's financial zone. */
    fun fileName(exportedAt: Instant, zone: ZoneId): String =
        "sello-respaldo-${stamp.format(exportedAt.atZone(zone))}.json"
}

/**
 * Writes and reads version 1 backups. Reading never touches storage: a file either
 * becomes a [BackupPlan] or is rejected whole with the first problem found.
 */
class BackupCodec(private val bounds: BackupBounds = BackupBounds()) {
    /**
     * The file for [contents], read back before it is returned, so the app never hands
     * out a backup it would itself refuse to restore.
     */
    fun encode(contents: BackupContents): Outcome<ByteArray, BackupRejection> {
        val text = writer.encodeToString(BackupFileV1.serializer(), contents.toWire())
        val bytes = "$text\n".toByteArray(Charsets.UTF_8)
        return when (val check = read(bytes)) {
            is Outcome.Success -> Outcome.Success(bytes)
            is Outcome.Failure -> check
        }
    }

    /** Reads at most one byte more than the size cap, so a huge stream is never held. */
    fun read(input: InputStream): Outcome<BackupPlan, BackupRejection> {
        val held = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_BYTES)
        while (held.size() <= bounds.maxBytes) {
            val read = input.read(buffer, 0, minOf(buffer.size, bounds.maxBytes + 1 - held.size()))
            if (read < 0) break
            held.write(buffer, 0, read)
        }
        if (held.size() > bounds.maxBytes) {
            return Outcome.Failure(BackupRejection.TooLarge(bounds.maxBytes, held.size().toLong()))
        }
        return read(held.toByteArray())
    }

    fun read(bytes: ByteArray): Outcome<BackupPlan, BackupRejection> {
        if (bytes.size > bounds.maxBytes) {
            return Outcome.Failure(BackupRejection.TooLarge(bounds.maxBytes, bytes.size.toLong()))
        }
        val text = utf8(bytes) ?: return Outcome.Failure(BackupRejection.Malformed(null))
        scanStructure(text, bounds)?.let { return Outcome.Failure(it) }
        header(text)?.let { return Outcome.Failure(it) }
        val file = try {
            reader.decodeFromString(BackupFileV1.serializer(), text)
        } catch (failure: SerializationException) {
            return Outcome.Failure(BackupRejection.Malformed(pathOf(failure)))
        } catch (failure: IllegalArgumentException) {
            return Outcome.Failure(BackupRejection.Malformed(pathOf(failure)))
        }
        return when (val contents = validate(file)) {
            is Outcome.Success -> Outcome.Success(BackupPlan(contents.value))
            is Outcome.Failure -> contents
        }
    }

    /** A file says what it is before the rest of it is held to version 1's shape. */
    private fun header(text: String): BackupRejection? {
        val header = try {
            headerReader.decodeFromString(BackupHeaderWire.serializer(), text)
        } catch (_: SerializationException) {
            return BackupRejection.NotSelloBackup
        } catch (_: IllegalArgumentException) {
            return BackupRejection.NotSelloBackup
        }
        val format = header.format as? JsonPrimitive
        if (format == null || !format.isString || format.content != BackupFormat.FORMAT) {
            return BackupRejection.NotSelloBackup
        }
        val version = header.version?.wholeNumberOrNull()
            ?: return BackupRejection.NotSelloBackup
        return if (version ==
            BackupFormat.VERSION
        ) {
            null
        } else {
            BackupRejection.UnsupportedVersion(version)
        }
    }

    private companion object {
        const val BUFFER_BYTES = 64 * 1024
        val reader = Json
        val headerReader = Json { ignoreUnknownKeys = true }

        @OptIn(ExperimentalSerializationApi::class)
        val writer = Json {
            prettyPrint = true
            prettyPrintIndent = "  "
        }

        private val pathPattern = Regex("at path: (\\$[^\\s]*)")

        /** Only where the problem is: the library's message also quotes file contents. */
        fun pathOf(failure: Exception): String? =
            failure.message?.let { pathPattern.find(it)?.groupValues?.get(1) }

        fun utf8(bytes: ByteArray): String? = try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (_: CharacterCodingException) {
            null
        }
    }
}

private fun BudgetLimit.amount() = (this as? BudgetLimit.Finite)?.amount

private fun BudgetLimit.kind() = when (this) {
    is BudgetLimit.Finite -> WireNames.FINITE
    BudgetLimit.Unlimited -> WireNames.UNLIMITED
}

/**
 * Records are written in a fixed order, so the same data always makes the same file:
 * categories by creation then identifier, limits by category then month, records by
 * date then sequence.
 */
internal fun BackupContents.toWire(): BackupFileV1 = BackupFileV1(
    format = BackupFormat.FORMAT,
    version = JsonPrimitive(BackupFormat.VERSION),
    exportedAt = exportedAt.toString(),
    financial = FinancialV1(
        sourceRevision = JsonPrimitive(financial.revision),
        zone = financial.zone.id,
        categories = financial.categories
            .sortedWith(compareBy({ it.createdAt }, { it.id.value }))
            .map {
                CategoryV1(
                    id = it.id.value,
                    name = it.name.value,
                    icon = it.icon.value,
                    archived = JsonPrimitive(it.archived),
                    createdAt = it.createdAt.toString(),
                    updatedAt = it.updatedAt.toString()
                )
            },
        defaultLimits = financial.defaultLimits
            .sortedWith(compareBy({ it.categoryId.value }, { it.effectiveMonth }))
            .map {
                val limit = (it.budget as? AutomaticBudget.Limit)?.limit
                DefaultLimitV1(
                    categoryId = it.categoryId.value,
                    fromMonth = EffectiveDates.storedText(it.effectiveMonth),
                    kind = limit?.kind() ?: WireNames.PAUSED,
                    amount = JsonPrimitive(limit?.amount()?.minorUnits),
                    currency = limit?.amount()?.currency?.code
                )
            },
        monthLimits = financial.monthLimits
            .sortedWith(compareBy({ it.categoryId.value }, { it.month }))
            .map {
                MonthLimitV1(
                    categoryId = it.categoryId.value,
                    month = EffectiveDates.storedText(it.month),
                    kind = it.limit.kind(),
                    amount = JsonPrimitive(it.limit.amount()?.minorUnits),
                    currency = it.limit.amount()?.currency?.code
                )
            },
        expenses = financial.expenses
            .sortedWith(compareBy({ it.date }, { it.sequence }))
            .map {
                ExpenseV1(
                    id = it.id.value,
                    categoryId = it.categoryId.value,
                    date = EffectiveDates.storedText(it.date),
                    sequence = JsonPrimitive(it.sequence),
                    amount = JsonPrimitive(it.amount.money.minorUnits),
                    currency = it.amount.money.currency.code,
                    note = it.note?.value,
                    createdAt = it.createdAt.toString(),
                    updatedAt = it.updatedAt.toString()
                )
            },
        incomes = financial.incomes
            .sortedWith(compareBy({ it.date }, { it.sequence }))
            .map {
                IncomeV1(
                    id = it.id.value,
                    date = EffectiveDates.storedText(it.date),
                    sequence = JsonPrimitive(it.sequence),
                    amount = JsonPrimitive(it.amount.money.minorUnits),
                    currency = it.amount.money.currency.code,
                    source = it.source.key,
                    sourceName = (it.source as? IncomeSource.Other)?.name?.value,
                    note = it.note?.value,
                    createdAt = it.createdAt.toString(),
                    updatedAt = it.updatedAt.toString()
                )
            }
    ),
    preferences = preferences?.let {
        PreferencesV1(
            theme = WireNames.themes.getValue(it.appearance.theme),
            ink = WireNames.inks.getValue(it.appearance.ink),
            categoryOrder = WireNames.orders.getValue(it.ordering.order),
            categoryDirection = WireNames.directions.getValue(it.ordering.direction)
        )
    }
)
