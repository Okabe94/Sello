package com.software.sello.domain.policy

import com.software.sello.domain.model.Money
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.ResumenSnapshot
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.model.TransactionAmount

object IncomePreviewPolicy {
    fun preview(
        snapshot: ResumenSnapshot,
        amount: TransactionAmount
    ): Outcome<Money, SnapshotFailure> = when (val result = snapshot.income + amount.money) {
        is Outcome.Success -> result
        is Outcome.Failure -> Outcome.Failure(SnapshotFailure.Unrepresentable)
    }
}
