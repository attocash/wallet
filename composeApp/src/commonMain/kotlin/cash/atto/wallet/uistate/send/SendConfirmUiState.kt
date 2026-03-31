package cash.atto.wallet.uistate.send

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class SendConfirmUiState(
    val amount: BigDecimal?,
    val amountUsd: BigDecimal?,
    val address: String?,
    val showLoader: Boolean
)
