package com.software.sello.presentation.income

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.software.sello.R

@Composable
fun incomeSourceLabel(key: String, otherName: String? = null): String = when (key) {
    "salary" -> stringResource(R.string.income_salary)
    "freelance" -> stringResource(R.string.income_freelance)
    "passive_income" -> stringResource(R.string.income_passive)
    "transfer" -> stringResource(R.string.income_transfer)
    else -> otherName ?: stringResource(R.string.income_other)
}
