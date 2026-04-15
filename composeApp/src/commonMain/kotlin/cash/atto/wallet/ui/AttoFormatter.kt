package cash.atto.wallet.ui

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object AttoFormatter {
    private const val SIGNIFICANT_DIGITS_BELOW_ONE = 3

    private val SIGNIFICANT_DIGITS_MODE =
        DecimalMode(
            decimalPrecision = SIGNIFICANT_DIGITS_BELOW_ONE.toLong(),
            roundingMode = RoundingMode.ROUND_HALF_CEILING,
        )

    fun format(value: BigDecimal?): String {
        if (value == null) return "…"

        val abs = value.abs()
        return if (abs > BigDecimal.ZERO && abs < BigDecimal.ONE) {
            roundToSignificantDigits(value)
        } else {
            formatLocalized(
                value
                    .roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_CEILING)
                    .toStringExpanded(),
            )
        }
    }

    fun format(value: ULong): String = formatLocalized(value.toString())

    @OptIn(ExperimentalTime::class)
    fun format(value: Instant): String = formatDateTime(value)

    @OptIn(ExperimentalTime::class)
    fun formatDate(value: Instant): String = formatDateOnly(value)

    fun formatRelativeDate(value: Instant): String {
        if (value.toEpochMilliseconds() == 0L) {
            return "Unknown"
        }

        val difference = Clock.System.now() - value
        return when (difference.inWholeDays) {
            0L -> "Today"
            1L -> "Yesterday"
            else -> "$difference days ago"
        }
    }

    private fun roundToSignificantDigits(value: BigDecimal): String {
        val rounded = value.roundSignificand(SIGNIFICANT_DIGITS_MODE)
        val expanded = rounded.toStringExpanded()

        val fraction = expanded.substringAfter('.', "")
        val firstSig = fraction.indexOfFirst { it != '0' }
        if (firstSig == -1) return "0.00"

        val minLength = firstSig + SIGNIFICANT_DIGITS_BELOW_ONE
        return expanded.substringBefore('.') + "." + fraction.padEnd(minLength, '0')
    }
}

internal expect fun formatLocalized(value: String): String

@OptIn(ExperimentalTime::class)
internal expect fun formatDateTime(value: Instant): String

@OptIn(ExperimentalTime::class)
internal expect fun formatDateOnly(value: Instant): String
