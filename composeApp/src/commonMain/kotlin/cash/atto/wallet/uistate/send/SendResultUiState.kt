package cash.atto.wallet.uistate.send

import com.ionspin.kotlin.bignum.decimal.BigDecimal

class SendResultUiState(
    val result: SendTransactionUiState.SendOperationResult,
    val amount: BigDecimal?,
    val amountUsd: BigDecimal?,
    val address: String?
)
