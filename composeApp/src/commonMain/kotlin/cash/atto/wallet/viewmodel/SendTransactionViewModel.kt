package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoUnit
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.uistate.send.SendTransactionUiState
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class SendTransactionViewModel(
    private val walletManagerRepository: WalletManagerRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SendTransactionUiState.DEFAULT)
    val state = _state.asStateFlow()

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    init {
        viewModelScope.launch {
            walletManagerRepository.state
                .filterNotNull()
                .collect { wallet ->
                    println(
                        "SendTransactionViewModel is collecting account information from wallet ${
                            AttoAddress(
                                AttoAlgorithm.V1,
                                wallet.publicKey
                            )
                        }"
                    )
                    wallet.accountFlow.collect { account ->
                        println("Account $account")
                        _state.emit(
                            state.value.copy(
                                account = account
                            )
                        )
                    }
                }
        }
    }

    suspend fun updateSendInfo(
        amount: String?,
        address: String?
    ) {
        _state.emit(
            state.value.copy(
                amountString = amount,
                address = address
            )
        )
    }

    suspend fun send(): Boolean {
        try {
            walletManagerRepository.state
                .value!!
                .send(
                    receiverAddress = AttoAddress.parse(
                        state.value
                            .sendConfirmUiState
                            .address!!
                    ),
                    amount = AttoAmount.from(
                        unit = AttoUnit.ATTO,
                        string = state.value
                            .sendConfirmUiState
                            .amount
                            .toString()
                    )
                )
        } catch (ex: Exception) {
            println(ex.message)
            _state.emit(
                state.value.copy(
                    operationResult = SendTransactionUiState.SendOperationResult.FAILURE
                )
            )

            return false
        }

        _state.emit(
            state.value.copy(
                operationResult = SendTransactionUiState.SendOperationResult.SUCCESS
            )
        )

        return true
    }

    suspend fun clearTransactionData() = _state.emit(
        state.value.copy(
            amount = null,
            amountString = null,
            address = null,
            showAmountError = false,
            showAddressError = false
        )
    )

    suspend fun checkTransactionData(): Boolean {
        val amount = try {
            state.value
                .sendFromUiState
                .amountString
                ?.toBigDecimal()
        }
        catch(ex: NumberFormatException) {
            null
        }

        val amountCheckResult = amount != null

        val addressCheckResult = AttoAddress.isValid(
            state.value
                .sendFromUiState
                .address
                .orEmpty()
        )

        _state.emit(
            state.value.copy(
                amount = amount,
                showAmountError = !amountCheckResult,
                showAddressError = !addressCheckResult
            )
        )

        return amountCheckResult && addressCheckResult
    }

    suspend fun showLoader() {
        _state.emit(
            state.value.copy(
                showLoader = true
            )
        )
    }

    suspend fun hideLoader() {
        _state.emit(
            state.value.copy(
                showLoader = false
            )
        )
    }
}