package cash.atto.wallet.uistate.desktop

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class BalanceChipUiState(
    val attoCoins: BigDecimal?
) {
    companion object {
        val DEFAULT = BalanceChipUiState(null)
    }
}