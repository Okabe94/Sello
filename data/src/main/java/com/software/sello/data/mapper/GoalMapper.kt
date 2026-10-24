package com.software.sello.data.mapper

import com.software.sello.data.local.entity.GoalEntity
import com.software.sello.data.local.entity.GoalEventEntity
import com.software.sello.domain.model.Goal
import com.software.sello.domain.model.GoalEvent
import com.software.sello.domain.model.GoalHistory
import com.software.sello.domain.model.GoalId
import com.software.sello.domain.model.GoalMovement
import com.software.sello.domain.model.GoalMovementId
import com.software.sello.domain.model.GoalMovementKind
import com.software.sello.domain.model.GoalName
import com.software.sello.domain.model.GoalTargetChange
import com.software.sello.domain.model.IconKey
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.Note
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.policy.EffectiveDates
import java.time.Instant

internal fun Goal.toEntity(nextSequence: Long) = GoalEntity(
    id = id.value,
    name = name.value,
    iconKey = icon.value,
    targetMonth = targetMonth?.let(EffectiveDates::storedText),
    completedOn = completedOn?.let(EffectiveDates::storedText),
    nextSequence = nextSequence,
    version = version,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli()
)

internal fun GoalEntity.toDomain(): Outcome<Goal, StorageFailure.Integrity> =
    decodeRow(GoalEntity.TABLE, id) {
        check("next_sequence", nextSequence >= 1)
        check("version", version >= 1)
        val name = valid("name", GoalName.of(name))
        check("name", name.value == this@toDomain.name)
        Goal(
            id = valid("id", GoalId.of(id)),
            name = name,
            icon = valid("icon_key", IconKey.of(iconKey)),
            targetMonth = targetMonth?.let { valid("target_month", EffectiveDates.parseMonth(it)) },
            completedOn = completedOn?.let { valid("completed_on", EffectiveDates.parseDate(it)) },
            version = version,
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt)
        )
    }

/** A target change keeps its row's [id], which the domain does not name. */
internal fun GoalTargetChange.toEntity(id: String, goalId: GoalId, at: Instant) = GoalEventEntity(
    id = id,
    goalId = goalId.value,
    sequence = sequence,
    kind = GoalEventEntity.TARGET,
    amountMinor = target.money.minorUnits,
    currency = target.money.currency.code,
    effectiveDate = null,
    recordedOn = EffectiveDates.storedText(recordedOn),
    note = null,
    version = 1,
    createdAt = at.toEpochMilli(),
    updatedAt = at.toEpochMilli()
)

internal fun GoalMovement.toEntity(goalId: GoalId) = GoalEventEntity(
    id = id.value,
    goalId = goalId.value,
    sequence = sequence,
    kind = when (kind) {
        GoalMovementKind.Contribution -> GoalEventEntity.CONTRIBUTION
        GoalMovementKind.Withdrawal -> GoalEventEntity.WITHDRAWAL
    },
    amountMinor = amount.money.minorUnits,
    currency = amount.money.currency.code,
    effectiveDate = EffectiveDates.storedText(date),
    recordedOn = EffectiveDates.storedText(recordedOn),
    note = note?.value,
    version = version,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli()
)

/** A target has no date or note; a movement always has a date. Anything else is damage. */
internal fun GoalEventEntity.toDomain(): Outcome<GoalEvent, StorageFailure.Integrity> =
    decodeRow(GoalEventEntity.TABLE, id) {
        check("sequence", sequence >= 0)
        check("version", version >= 1)
        val currency = currency("currency", currency)
        check("currency", currency.isMvpEntryCurrency)
        val amount = valid("amount_minor", TransactionAmount.of(Money(amountMinor, currency)))
        val recordedOn = valid("recorded_on", EffectiveDates.parseDate(recordedOn))
        valid("id", GoalMovementId.of(id))
        valid("goal_id", GoalId.of(goalId))
        when (kind) {
            GoalEventEntity.TARGET -> {
                check("effective_date", effectiveDate == null)
                check("note", note == null)
                check("version", version == 1L)
                GoalTargetChange(amount, sequence, recordedOn)
            }

            GoalEventEntity.CONTRIBUTION, GoalEventEntity.WITHDRAWAL -> {
                val date = valid(
                    "effective_date",
                    EffectiveDates.parseDate(effectiveDate ?: reject("effective_date"))
                )
                val stored = note
                val note = stored?.let { valid("note", Note.of(it)) }
                check("note", note?.value == stored)
                GoalMovement(
                    id = valid("id", GoalMovementId.of(id)),
                    kind = if (kind == GoalEventEntity.CONTRIBUTION) {
                        GoalMovementKind.Contribution
                    } else {
                        GoalMovementKind.Withdrawal
                    },
                    amount = amount,
                    date = date,
                    sequence = sequence,
                    recordedOn = recordedOn,
                    note = note,
                    version = version,
                    createdAt = Instant.ofEpochMilli(createdAt),
                    updatedAt = Instant.ofEpochMilli(updatedAt)
                )
            }

            else -> reject("kind")
        }
    }

/**
 * A goal and its whole history, or the first damaged row. Every event must belong to
 * the goal and come before its [GoalEntity.nextSequence].
 */
internal fun GoalEntity.toHistory(
    events: List<GoalEventEntity>
): Outcome<GoalHistory, StorageFailure.Integrity> {
    val goal = when (val decoded = toDomain()) {
        is Outcome.Success -> decoded.value
        is Outcome.Failure -> return decoded
    }
    events.firstOrNull { it.goalId != id || it.sequence >= nextSequence }?.let {
        return Outcome.Failure(StorageFailure.Integrity(GoalEventEntity.TABLE, it.id, "sequence"))
    }
    return when (val decoded = events.decodeAll { it.toDomain() }) {
        is Outcome.Success -> Outcome.Success(GoalHistory(goal, decoded.value))
        is Outcome.Failure -> decoded
    }
}
