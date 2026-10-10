package com.software.sello.navigation

import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.policy.CopAmountInput
import java.net.URI
import java.net.URISyntaxException

/**
 * A request to open the entry form, optionally with a category and an amount already
 * filled in. It is a suggestion for a draft and nothing more: it is never saved as an
 * expense, and whoever shows the form still has to check that the category exists and
 * accepts entries. A part that was missing or invalid in the link is simply absent.
 */
data class EntryRequest(val categoryId: CategoryId?, val amount: TransactionAmount?)

/**
 * Links of the form `sello://anotar?categoria=<identifier>&monto=<amount>`.
 *
 * A link comes from outside the app and is treated as hostile. Anything that is not
 * exactly this shape is not a Sello link at all. Within a Sello link each value goes
 * through the same rules as typed input; a value that fails them is dropped, never
 * repaired, so `1e3` cannot become an amount.
 */
object EntryLinks {
    const val SCHEME = "sello"
    const val HOST = "anotar"
    const val CATEGORY = "categoria"
    const val AMOUNT = "monto"
    const val MAX_LENGTH = 512

    fun parse(link: String?): EntryRequest? {
        if (link == null || link.length > MAX_LENGTH) return null
        val uri = try {
            URI(link)
        } catch (_: URISyntaxException) {
            return null
        }
        val recognised = uri.scheme == SCHEME &&
            uri.host == HOST &&
            uri.port == -1 &&
            uri.rawUserInfo == null &&
            uri.rawPath.isNullOrEmpty() &&
            uri.rawFragment == null
        if (!recognised) return null

        val values = uri.rawQuery.orEmpty().split('&').filter { it.isNotEmpty() }.map {
            it.substringBefore('=') to it.substringAfter('=', "")
        }

        // A key given twice is ambiguous, so neither value is used.
        fun only(key: String) = values.filter { it.first == key }.singleOrNull()?.second
        return EntryRequest(category(only(CATEGORY)), amount(only(AMOUNT)))
    }

    private fun category(value: String?): CategoryId? =
        (value?.let(CategoryId::of) as? Outcome.Success)?.value

    private fun amount(value: String?): TransactionAmount? {
        val money = (value?.let(CopAmountInput::parse) as? Outcome.Success)?.value ?: return null
        return (TransactionAmount.of(money) as? Outcome.Success)?.value
    }
}
