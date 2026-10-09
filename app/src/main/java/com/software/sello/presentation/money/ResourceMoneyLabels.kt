package com.software.sello.presentation.money

import android.content.res.Resources
import com.software.sello.R
import com.software.sello.domain.model.Currency

/** Money wording from string resources, so copy and plurals stay translatable. */
class ResourceMoneyLabels(private val resources: Resources) : MoneyLabels {
    override fun amount(currency: Currency, digits: String, isOne: Boolean): String =
        if (currency == Currency.COP) {
            // The figure is already text; the quantity only selects singular or plural.
            resources.getQuantityString(R.plurals.money_cop, if (isOne) 1 else 2, digits)
        } else {
            resources.getString(R.string.money_other_currency, digits, currency.code)
        }

    override fun negative(amount: String): String =
        resources.getString(R.string.money_negative, amount)

    override fun positive(amount: String): String =
        resources.getString(R.string.money_positive, amount)
}
