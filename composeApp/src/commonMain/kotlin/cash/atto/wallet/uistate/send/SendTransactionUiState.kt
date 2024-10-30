package cash.atto.wallet.uistate.send

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoUnit
import cash.atto.commons.toAddress
import java.math.BigDecimal

data class SendTransactionUiState(
    private val account: AttoAccount?,
    private val amount: BigDecimal?,
    private val address: String?,
    private val operationResult: SendOperationResult
) {
    val sendFromUiState get() = account?.let {
        SendFromUiState(
            accountName = "Main Account",
            accountSeed = it.publicKey
                .toAddress(AttoAlgorithm.V1)
                .value,
            accountBalance = it.balance
                .toString(AttoUnit.ATTO)
                .toBigDecimal(),
            amount = amount,
            address = address
        )
    } ?: SendFromUiState.DEFAULT

    val sendConfirmUiState get() = SendConfirmUiState(
        amount = amount,
        address = address
    )

    val sendResultUiState get() = SendResultUiState(
        result = operationResult,
        amount = amount,
        address = address
    )

    enum class SendOperationResult {
        UNKNOWN, SUCCESS, FAILURE
    }

    companion object {
        val DEFAULT = SendTransactionUiState(
            account = null,
            amount = null,
            address = null,
            operationResult = SendOperationResult.UNKNOWN
        )
    }
}