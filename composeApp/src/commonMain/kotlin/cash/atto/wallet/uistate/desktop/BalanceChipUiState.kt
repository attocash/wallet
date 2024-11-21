package cash.atto.wallet.uistate.desktop

import java.math.BigDecimal

data class BalanceChipUiState(
    val attoCoins: BigDecimal?
) {
    companion object {
        val DEFAULT = BalanceChipUiState(BigDecimal.ZERO)
    }
}