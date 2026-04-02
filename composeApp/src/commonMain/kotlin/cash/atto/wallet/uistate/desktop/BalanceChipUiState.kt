package cash.atto.wallet.uistate.desktop

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class BalanceChipUiState(
    val attoCoins: BigDecimal?,
    val usdValue: BigDecimal? = null,
    val apy: BigDecimal? = null,
    val pendingReceivableCount: Int = 0,
    val pendingReceivableAmount: BigDecimal = BigDecimal.ZERO
) {
    companion object {
        val DEFAULT = BalanceChipUiState(null, null, null)
    }
}
