package cash.atto.wallet.uistate.send

import java.math.BigDecimal

class SendResultUiState(
    val result: SendTransactionUiState.SendOperationResult,
    val amount: BigDecimal?,
    val address: String?
)