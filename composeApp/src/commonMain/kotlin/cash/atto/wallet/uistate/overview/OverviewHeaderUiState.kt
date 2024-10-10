package cash.atto.wallet.uistate.overview

import java.math.BigDecimal

data class OverviewHeaderUiState(
    val attoCoins: BigDecimal?
) {
    companion object {
        val DEFAULT = OverviewHeaderUiState(BigDecimal.ZERO)
    }
}