package cash.atto.wallet.repository

import cash.atto.commons.AttoReceivable
import cash.atto.commons.AttoUnit
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

data class PendingReceivablesState(
    val receivables: List<AttoReceivable> = emptyList(),
) {
    val count: Int
        get() = receivables.size

    val totalAmount: BigDecimal
        get() =
            receivables
                .fold(BigDecimal.ZERO) { acc, receivable ->
                    acc + receivable.amount.toString(AttoUnit.ATTO).toBigDecimal()
                }

    companion object {
        val EMPTY = PendingReceivablesState()
    }
}
