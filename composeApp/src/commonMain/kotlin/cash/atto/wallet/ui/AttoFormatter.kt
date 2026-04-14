package cash.atto.wallet.ui

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object AttoFormatter {
    fun format(value: BigDecimal?): String =
        value
            ?.toStringExpanded()
            ?.let { formatLocalized(it) }
            ?: "…"

    fun format(value: String?): String = value?.let { formatLocalized(it) } ?: "…"

    fun formatUsd(value: BigDecimal?): String {
        if (value == null) return ""
        val rounded = value.roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_CEILING)
        return "~ $${ rounded.toStringExpanded() } USD"
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
}

internal expect fun formatLocalized(value: String): String

@OptIn(ExperimentalTime::class)
internal expect fun formatDateTime(value: Instant): String

@OptIn(ExperimentalTime::class)
internal expect fun formatDateOnly(value: Instant): String
