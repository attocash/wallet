package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoUnit
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.uistate.desktop.BalanceChipUiState
import cash.atto.wallet.uistate.desktop.MainScreenUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val walletManagerRepository: WalletManagerRepository,
    private val settingsViewModel: SettingsViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenUiState.DEFAULT)
    val state = _state.asStateFlow()

    private var accountCollectorJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            walletManagerRepository.state
                .filterNotNull()
                .collect { wallet ->
                    println(
                        "MainViewModel is collecting account information from wallet ${
                            AttoAddress(
                                AttoAlgorithm.V1,
                                wallet.publicKey
                            )
                        }"
                    )

                    accountCollectorJob?.cancel()
                    accountCollectorJob = scope.launch {
                        wallet.accountFlow.collect { account ->
                            handleAccount(account)
                        }
                    }
                }
        }
    }

    private suspend fun handleAccount(account: AttoAccount) {
        println("Account $account")

        _state.emit(
            state.value.copy(
                balanceChipUiState = BalanceChipUiState(
                    account.balance
                        .toString(AttoUnit.ATTO)
                        .toBigDecimal()
                ),
            )
        )
    }
}