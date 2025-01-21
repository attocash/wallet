package cash.atto.wallet.ui

import com.ionspin.kotlin.bignum.decimal.BigDecimal

object AttoFormatter {

    fun format(value: BigDecimal?) = format(value?.toStringExpanded())

    fun format(value: String?): String {
        return value ?: "..."
    }
}