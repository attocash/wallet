package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoUnit
import cash.atto.commons.toAddress
import cash.atto.commons.wallet.AttoWalletManager
import cash.atto.wallet.interactor.CreateWalletManagerInteractor
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.overview.OverviewHeaderUiState
import cash.atto.wallet.uistate.send.SendFromUiState
import cash.atto.wallet.uistate.send.SendTransactionUiState
import cash.atto.wallet.uistate.settings.ProfileUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class SendTransactionViewModel(
    private val appStateRepository: AppStateRepository,
    private val createWalletManagerInteractor: CreateWalletManagerInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(SendTransactionUiState.DEFAULT)
    val state = _state.asStateFlow()

    private val walletState = MutableStateFlow<AttoWalletManager?>(null)

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    init {
        viewModelScope.launch {
            appStateRepository.state.collect { appState ->
                if (appState.publicKey != null) {
                    walletState.emit(createWalletManagerInteractor.invoke(appState))
                    _state.emit(state.value.copy(
                        sendFromUiState = state.value
                            .sendFromUiState
                            .copy(
                                accountName = "Main Account",
                                accountSeed = appState.publicKey
                                    .toAddress(AttoAlgorithm.V1)
                                    .value
                            )
                    ))
                }
            }
        }

        viewModelScope.launch {
            walletState.filterNotNull().collect { wallet ->
                println("Wallet ${AttoAddress(AttoAlgorithm.V1, wallet.publicKey)} is ready")
                wallet.accountFlow.collect { account ->
                    println("Account $account")
                    _state.emit(
                        state.value.copy(
                            sendFromUiState = state.value
                                .sendFromUiState
                                .copy(
                                    accountBalance = account.balance
                                        .toString(AttoUnit.ATTO)
                                        .toBigDecimal()
                                )
                        )
                    )
                }
            }
        }
    }
//    private suspend fun updateBalance(account: AttoAccount) {}
}