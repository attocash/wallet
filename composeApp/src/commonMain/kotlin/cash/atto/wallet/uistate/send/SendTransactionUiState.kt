package cash.atto.wallet.uistate.send

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoSendBlock
import cash.atto.commons.AttoUnit
import cash.atto.commons.toAddress
import cash.atto.wallet.ui.AttoFormatter
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

data class SendTransactionUiState(
    private val account: AttoAccount?,
    private val amountString: String?,
    private val amount: BigDecimal?,
    private val address: String?,
    private val operationResult: SendOperationResult,
    private val showAmountError: Boolean = false,
    private val showAddressError: Boolean = false,
    private val showLoader: Boolean = false,
    val priceUsd: BigDecimal? = null,
    val isUsdMode: Boolean = false,
    val elapsedMs: Long? = null,
    val sendBlock: AttoSendBlock? = null
) {
    private fun parseDecimal(value: String?): BigDecimal? =
        try {
            value?.toBigDecimal()
        } catch (_: Exception) {
            null
        }

    private fun amountUsd(attoAmount: BigDecimal?): BigDecimal? {
        if (attoAmount == null || priceUsd == null) return null
        return attoAmount.multiply(priceUsd)
    }

    private fun usdToAtto(usdAmount: BigDecimal?): BigDecimal? {
        if (usdAmount == null || priceUsd == null || priceUsd == BigDecimal.ZERO) return null
        return usdAmount.divide(priceUsd, DecimalMode(decimalPrecision = 30, roundingMode = RoundingMode.ROUND_HALF_CEILING))
    }

    private fun equivalentDisplay(): String {
        val input = parseDecimal(amountString)
        return if (isUsdMode) {
            usdToAtto(input)?.let {
                "~ ${it.roundToDigitPositionAfterDecimalPoint(6, RoundingMode.ROUND_HALF_CEILING).toStringExpanded()} ATTO"
            } ?: "USD price unavailable"
        } else {
            amountUsd(input)?.let { AttoFormatter.formatUsd(it) }
                ?: "USD price unavailable"
        }
    }
    val sendFromUiState
        get() = account?.let {
            val accountBalance = it.balance.toString(AttoUnit.ATTO)
            SendFromUiState(
                accountName = "Main Account",
                accountSeed = it.publicKey
                    .toAddress(AttoAlgorithm.V1)
                    .value,
                accountBalance = accountBalance,
                accountBalanceUsd = amountUsd(parseDecimal(accountBalance)),
                amountString = amountString,
                amountUsd = amountUsd(parseDecimal(amountString)),
                address = address,
                showAmountError = showAmountError,
                showAddressError = showAddressError,
                showLoader = showLoader,
                isUsdMode = isUsdMode,
                equivalentDisplay = equivalentDisplay(),
                priceUsd = priceUsd
            )
        } ?: SendFromUiState.DEFAULT

    val sendConfirmUiState
        get() = SendConfirmUiState(
            amount = amount,
            amountUsd = amountUsd(amount),
            address = address,
            showLoader = showLoader,
            accountHeight = account?.height?.value
        )

    val sendResultUiState
        get() = SendResultUiState(
            result = operationResult,
            amount = amount,
            amountUsd = amountUsd(amount),
            address = address,
            elapsedMs = elapsedMs,
            sendBlock = sendBlock
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
            operationResult = SendOperationResult.UNKNOWN,
            elapsedMs = null,
            isUsdMode = false
        )
    }
}
