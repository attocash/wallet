package cash.atto.wallet.ui

import com.ionspin.kotlin.bignum.decimal.BigDecimal

object AttoFormatter {

    fun format(value: BigDecimal?): String {
        return value?.toStringExpanded()
            ?.let { AttoLocalizedFormatter.format(it) }
            ?: "…"
    }

    fun format(value: String?): String {
        return value?.let { AttoLocalizedFormatter.format(it) } ?: "…"
    }

    fun format(value: ULong): String =
        AttoLocalizedFormatter.format(value.toString())
}
