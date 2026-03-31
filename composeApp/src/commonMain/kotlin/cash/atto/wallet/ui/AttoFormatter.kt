package cash.atto.wallet.ui

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode

object AttoFormatter {
    fun format(value: BigDecimal?): String {
        return value?.toStringExpanded()
            ?.let { AttoLocalizedFormatter.format(it) }
            ?: "…"
    }

    fun format(value: String?): String {
        return value?.let { AttoLocalizedFormatter.format(it) } ?: "…"
    }

    fun formatUsd(value: BigDecimal?): String {
        if (value == null) return ""
        val rounded = value.roundToDigitPositionAfterDecimalPoint(2, RoundingMode.ROUND_HALF_CEILING)
        return "≈ $${ rounded.toStringExpanded() } USD"
    }

    fun format(value: ULong): String =
        AttoLocalizedFormatter.format(value.toString())
}
