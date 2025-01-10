package cash.atto.wallet.uistate.overview

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class OverviewHeaderUiState(
    val attoCoins: BigDecimal?
) {
    companion object {
        val DEFAULT = OverviewHeaderUiState(BigDecimal.ZERO)
    }
}