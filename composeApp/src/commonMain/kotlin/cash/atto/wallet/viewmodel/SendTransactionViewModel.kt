@file:OptIn(ExperimentalTime::class)

package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoInstant
import cash.atto.commons.AttoUnit
import cash.atto.wallet.model.defaultAccountName
import cash.atto.wallet.repository.HomeRepository
import cash.atto.wallet.repository.PreferencesRepository
import cash.atto.wallet.repository.WalletAccountState
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.ui.AttoPaymentRequests
import cash.atto.wallet.uistate.send.SendTransactionUiState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class SendTransactionViewModel(
    private val walletManagerRepository: WalletManagerRepository,
    private val homeRepository: HomeRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SendTransactionUiState.DEFAULT)
    val state = _state.asStateFlow()

    private val viewModelScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var accounts: List<WalletAccountState> = emptyList()
    private var selectedAccountIndex: UInt = 0U
    private var nodeTimestampPollingJob: Job? = null
    private val nodeTimeDifference = MutableStateFlow<Long?>(null)
    val nodeTimeDifferenceState = nodeTimeDifference.asStateFlow()

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
            walletManagerRepository.accountsState.collect {
                accounts = it
                emitSelectedAccountName()
            }
        }

        viewModelScope.launch {
            walletManagerRepository.selectedAccountIndexState.collect {
                selectedAccountIndex = it.value
                emitSelectedAccountName()
            }
        }

        viewModelScope.launch {
            preferencesRepository.state.collect {
                emitSelectedAccountName()
            }
        }

        viewModelScope.launch {
            walletManagerRepository.accountState.collect { account ->
                if (account == null) {
                    _state.emit(state.value.copy(account = null))
                    return@collect
                }

                println(
                    "SendTransactionViewModel is collecting account information from wallet ${
                        AttoAddress(
                            AttoAlgorithm.V1,
                            account.publicKey,
                        )
                    }",
                )
                println("Account $account")
                _state.emit(
                    state.value.copy(
                        account = account,
                    ),
                )
            }
        }
    }

    private suspend fun emitSelectedAccountName() {
        val selectedAccount = accounts.firstOrNull { it.index.value == selectedAccountIndex }
        val accountName =
            selectedAccount?.let {
                preferencesRepository.getAddressLabel(it.address.toString())
                    ?: defaultAccountName(it.index.value)
            }

        _state.emit(
            state.value.copy(
                accountName = accountName,
            ),
        )
    }

    suspend fun updateSendInfo(
        amount: String?,
        address: String?,
    ) {
        val addressErrorMessage = validateAddress(address)
        _state.emit(
            state.value.copy(
                amountString = amount,
                address = address,
                priceUsd = homeRepository.getPriceUsd(),
                showAmountError = false,
                showAddressError = addressErrorMessage != null,
                addressErrorMessage = addressErrorMessage,
            ),
        )
    }

    suspend fun applyPaymentRequest(paymentRequest: String?) {
        val parsed = AttoPaymentRequests.parse(paymentRequest) ?: return
        val addressErrorMessage = validateAddress(parsed.receiverAddress)
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
                address = parsed.receiverAddress,
                priceUsd = homeRepository.getPriceUsd(),
                isUsdMode = false,
                showAmountError = false,
                showAddressError = addressErrorMessage != null,
                addressErrorMessage = addressErrorMessage,
            ),
        )
    }

    suspend fun send(): Boolean {
        try {
            val amount =
                state.value.sendConfirmUiState.amount
                    ?.let(::toAttoAmount)
                    ?: throw IllegalStateException("Invalid amount")
            val receiverAddress =
                parseAttoAddress(
                    state.value.sendConfirmUiState.address,
                ) ?: throw IllegalStateException("Invalid address")

            val block =
                walletManagerRepository.send(
                    receiverAddress = receiverAddress,
                    amount = amount,
                    timestampProvider = currentNodeTimestampProvider(),
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

    fun startNodeTimestampPolling() {
        if (nodeTimestampPollingJob?.isActive == true) return

        nodeTimestampPollingJob =
            viewModelScope.launch {
                while (true) {
                    try {
                        nodeTimeDifference.value =
                            walletManagerRepository.nodeTimeDifference(
                                Clock.System.now().toAttoInstant(),
                            )
                    } catch (ex: CancellationException) {
                        throw ex
                    } catch (ex: Exception) {
                        println("Failed to prefetch node timestamp: ${ex.message}")
                    }

                    delay(NODE_TIMESTAMP_POLL_INTERVAL)
                }
            }
    }

    fun stopNodeTimestampPolling() {
        nodeTimestampPollingJob?.cancel()
        nodeTimestampPollingJob = null
        nodeTimeDifference.value = null
    }

    private suspend fun currentNodeTimestampProvider(): suspend () -> AttoInstant {
        val diff = nodeTimeDifference.filterNotNull().first()
        return {
            Clock.System
                .now()
                .plus(diff.milliseconds)
                .toAttoInstant()
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
                addressErrorMessage = null,
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
                    ).roundToDigitPositionAfterDecimalPoint(
                        18,
                        RoundingMode.ROUND_HALF_CEILING,
                    )
            } else {
                rawAmount
            }

        val amountCheckResult = amount != null && (!state.value.isUsdMode || hasUsdPrice)

        val destinationAddress =
            state.value
                .sendFromUiState
                .address
                .orEmpty()
        val liveAddressErrorMessage = validateAddress(destinationAddress)
        val addressErrorMessage =
            if (destinationAddress.isBlank()) {
                "Enter a valid ATTO address."
            } else {
                liveAddressErrorMessage
            }
        val addressCheckResult = addressErrorMessage == null

        _state.emit(
            state.value.copy(
                amount = amount,
                priceUsd = priceUsd,
                showAmountError = !amountCheckResult,
                showAddressError = !addressCheckResult,
                addressErrorMessage = addressErrorMessage,
            ),
        )

        return amountCheckResult && addressCheckResult
    }

    private fun validateAddress(address: String?): String? {
        val parsedAddress = parseAttoAddress(address)
        if (address?.trim().isNullOrEmpty()) {
            return null
        }

        if (parsedAddress == null) {
            return "Enter a valid ATTO address."
        }

        val currentAddress = state.value.sendFromUiState.accountSeed ?: return null
        return if (AttoAddress.parse(currentAddress) == parsedAddress) {
            "You cannot send ATTO to your own address."
        } else {
            null
        }
    }

    private fun parseAttoAddress(address: String?): AttoAddress? {
        val trimmed = address?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val candidates =
            listOfNotNull(
                trimmed,
                AttoPaymentRequests.extractBareAddress(trimmed),
                if (trimmed.startsWith("atto://")) null else "atto://$trimmed",
            ).distinct()

        return candidates.firstNotNullOfOrNull { candidate ->
            runCatching { AttoAddress.parse(candidate) }.getOrNull()
        }
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

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }

    private companion object {
        val NODE_TIMESTAMP_POLL_INTERVAL = 1.seconds
    }
}

private fun Instant.toAttoInstant(): AttoInstant = AttoInstant.fromEpochMilliseconds(toEpochMilliseconds())
