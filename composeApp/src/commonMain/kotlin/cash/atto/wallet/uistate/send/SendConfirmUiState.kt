package cash.atto.wallet.uistate.send

import java.math.BigDecimal

data class SendConfirmUiState(
    val amount: BigDecimal?,
    val address: String?,
    val showLoader: Boolean
)