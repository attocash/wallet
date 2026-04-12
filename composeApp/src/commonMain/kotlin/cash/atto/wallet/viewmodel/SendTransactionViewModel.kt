package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoUnit
import cash.atto.wallet.repository.HomeRepository
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.ui.AttoPaymentRequests
import cash.atto.wallet.uistate.send.SendTransactionUiState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SendTransactionViewModel(
    private val walletManagerRepository: WalletManagerRepository,
    private val homeRepository: HomeRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SendTransactionUiState.DEFAULT)
    val state = _state.asStateFlow()

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    init {
        viewModelScope.launch {
            homeRepository.homeResponse
                .map { homeRepository.getPriceUsd() }
                .distinctUntilChanged()
                .collect { priceUsd ->
                    _state.emit(
                        state.value.copy(
                            priceUsd = priceUsd,
                        ),
                    )
                }
        }

        viewModelScope.launch {
            walletManagerRepository.state
                .filterNotNull()
                .collect { wallet ->
                    println(
                        "SendTransactionViewModel is collecting account information from wallet ${
                            AttoAddress(
                                AttoAlgorithm.V1,
                                wallet.publicKey,
                            )
                        }",
                    )
                    wallet.accountFlow.collect { account ->
                        println("Account $account")
                        _state.emit(
                            state.value.copy(
                                account = account,
                            ),
                        )
                    }
                }
        }
    }

    suspend fun updateSendInfo(
        amount: String?,
        address: String?,
    ) {
        _state.emit(
            state.value.copy(
                amountString = amount,
                address = address,
                priceUsd = homeRepository.getPriceUsd(),
            ),
        )
    }

    suspend fun applyPaymentRequest(paymentRequest: String?) {
        val parsed = AttoPaymentRequests.parse(paymentRequest) ?: return
        val amountAtto =
            parsed.amountRaw?.let { rawAmount ->
                try {
                    AttoAmount
                        .from(
                            unit = AttoUnit.RAW,
                            string = rawAmount,
                        ).toString(AttoUnit.ATTO)
                } catch (_: Exception) {
                    null
                }
            }
        _state.emit(
            state.value.copy(
                amountString = amountAtto ?: state.value.sendFromUiState.amountString,
                address = parsed.address,
                priceUsd = homeRepository.getPriceUsd(),
                isUsdMode = false,
                showAmountError = false,
                showAddressError = false,
            ),
        )
    }

    suspend fun send(): Boolean {
        try {
            val amount =
                state.value.sendConfirmUiState.amount
                    ?.let(::toAttoAmount)
                    ?: throw IllegalStateException("Invalid amount")

            val block =
                walletManagerRepository.state
                    .value!!
                    .send(
                        receiverAddress =
                            AttoAddress.parse(
                                state.value
                                    .sendConfirmUiState
                                    .address!!,
                            ),
                        amount = amount,
                    )

            _state.emit(
                state.value.copy(
                    operationResult = SendTransactionUiState.SendOperationResult.SUCCESS,
                    sendBlock = block,
                ),
            )

            return true
        } catch (ex: Exception) {
            println(ex.message)
            _state.emit(
                state.value.copy(
                    operationResult = SendTransactionUiState.SendOperationResult.FAILURE,
                ),
            )

            return false
        }
    }

    suspend fun toggleInputMode() {
        _state.emit(
            state.value.copy(
                isUsdMode = !state.value.isUsdMode,
                amountString = null,
            ),
        )
    }

    suspend fun clearTransactionData() =
        _state.emit(
            state.value.copy(
                amount = null,
                amountString = null,
                address = null,
                operationResult = SendTransactionUiState.SendOperationResult.UNKNOWN,
                showAmountError = false,
                showAddressError = false,
                isUsdMode = false,
            ),
        )

    suspend fun checkTransactionData(): Boolean {
        val rawAmount =
            try {
                state.value
                    .sendFromUiState
                    .amountString
                    ?.toBigDecimal()
            } catch (ex: NumberFormatException) {
                null
            }

        val priceUsd = homeRepository.getPriceUsd()

        val hasUsdPrice = priceUsd != null && priceUsd != BigDecimal.ZERO

        // USD mode is only valid when a live price is available for conversion.
        val amount =
            if (state.value.isUsdMode && rawAmount != null && hasUsdPrice) {
                rawAmount
                    .divide(
                        priceUsd,
                        DecimalMode(decimalPrecision = 30, roundingMode = RoundingMode.ROUND_HALF_CEILING),
                    ).roundToDigitPositionAfterDecimalPoint(18, RoundingMode.ROUND_HALF_CEILING)
            } else {
                rawAmount
            }

        val amountCheckResult = amount != null && (!state.value.isUsdMode || hasUsdPrice)

        val addressCheckResult =
            AttoAddress.isValid(
                state.value
                    .sendFromUiState
                    .address
                    .orEmpty(),
            )

        _state.emit(
            state.value.copy(
                amount = amount,
                priceUsd = priceUsd,
                showAmountError = !amountCheckResult,
                showAddressError = !addressCheckResult,
            ),
        )

        return amountCheckResult && addressCheckResult
    }

    suspend fun setElapsedMs(ms: Long) {
        _state.emit(
            state.value.copy(
                elapsedMs = ms,
            ),
        )
    }

    suspend fun showLoader() {
        _state.emit(
            state.value.copy(
                showLoader = true,
            ),
        )
    }

    suspend fun hideLoader() {
        _state.emit(
            state.value.copy(
                showLoader = false,
            ),
        )
    }

    private fun toAttoAmount(amount: BigDecimal): AttoAmount =
        AttoAmount.from(
            unit = AttoUnit.ATTO,
            string = amount.toStringExpanded(),
        )
}
