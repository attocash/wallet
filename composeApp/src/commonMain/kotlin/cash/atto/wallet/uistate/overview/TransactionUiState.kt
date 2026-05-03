package cash.atto.wallet.uistate.overview

import cash.atto.commons.AttoHeight
import cash.atto.wallet.ui.AttoFormatter
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class TransactionUiState
    @OptIn(ExperimentalTime::class)
    constructor(
        val type: TransactionType,
        val amount: String?,
        val source: String,
        val sourceLabel: String? = null,
        val transactionLabel: String? = null,
        val timestamp: Instant,
        val height: AttoHeight,
        val hash: String? = null,
    ) {
        var shownAmount =
            amount?.let { a ->
                if (a.firstOrNull() == '+' || a.firstOrNull() == '-') {
                    val sign = a.split(' ').getOrNull(0)
                    val number =
                        try {
                            a.split(' ').getOrNull(1)?.toBigDecimal()
                        } catch (_: Exception) {
                            null
                        }

                    "$sign ${AttoFormatter.format(number)}"
                } else {
                    AttoFormatter.format(
                        try {
                            a.toBigDecimal()
                        } catch (_: Exception) {
                            null
                        },
                    )
                }
            } ?: " "

        val shownHeight: String
            get() = AttoFormatter.format(height.value)

        @OptIn(ExperimentalTime::class)
        val formattedTimestamp: String
            get() = AttoFormatter.format(timestamp)
    }

enum class TransactionType {
    OPEN,
    SEND,
    RECEIVE,
    CHANGE,
}
