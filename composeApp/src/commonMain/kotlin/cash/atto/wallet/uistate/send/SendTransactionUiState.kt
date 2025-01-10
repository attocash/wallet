package cash.atto.wallet.uistate.send

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoUnit
import cash.atto.commons.toAddress
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

data class SendTransactionUiState(
    private val account: AttoAccount?,
    private val amountString: String?,
    private val amount: BigDecimal?,
    private val address: String?,
    private val operationResult: SendOperationResult,
    private val showAmountError: Boolean = false,
    private val showAddressError: Boolean = false,
    private val showLoader: Boolean = false
) {
    val sendFromUiState
        get() = account?.let {
            SendFromUiState(
                accountName = "Main Account",
                accountSeed = it.publicKey
                    .toAddress(AttoAlgorithm.V1)
                    .value,
                accountBalance = it.balance
                    .toString(AttoUnit.ATTO)
                    .toBigDecimal(),
                amountString = amountString,
                address = address,
                showAmountError = showAmountError,
                showAddressError = showAddressError,
                showLoader = showLoader
            )
        } ?: SendFromUiState.DEFAULT

    val sendConfirmUiState
        get() = SendConfirmUiState(
            amount = amount,
            address = address,
            showLoader = showLoader
        )

    val sendResultUiState
        get() = SendResultUiState(
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
            amountString = null,
            amount = null,
            address = null,
            operationResult = SendOperationResult.UNKNOWN
        )
    }
}