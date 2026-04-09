package cash.atto.wallet.uistate.send

import cash.atto.commons.AttoSendBlock
import com.ionspin.kotlin.bignum.decimal.BigDecimal

class SendResultUiState(
    val result: SendTransactionUiState.SendOperationResult,
    val amount: BigDecimal?,
    val amountUsd: BigDecimal?,
    val address: String?,
    val elapsedMs: Long? = null,
    val sendBlock: AttoSendBlock? = null
)
