package cash.atto.wallet.uistate.desktop

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class BalanceChipUiState(
    val attoCoins: BigDecimal?,
    val usdValue: BigDecimal? = null
) {
    companion object {
        val DEFAULT = BalanceChipUiState(null, null)
    }
}