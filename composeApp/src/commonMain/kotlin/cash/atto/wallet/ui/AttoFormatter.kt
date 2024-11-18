package cash.atto.wallet.ui

import java.math.BigDecimal

object AttoFormatter {

    fun format(value: BigDecimal?) = format(value?.toString())

    fun format(value: String?): String {
        return value?.let {
            "Ӿ$it"
        } ?: "..."
    }
}