package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoUnit
import cash.atto.commons.toAddress
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.PersistentAccountEntryRepository
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.uistate.overview.OverviewUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class OverviewViewModel(
    private val appStateRepository: AppStateRepository,
    private val walletManagerRepository: WalletManagerRepository,
    private val persistentAccountEntryRepository: PersistentAccountEntryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState.DEFAULT)
    val state = _state.asStateFlow()

    private var accountCollectorJob: Job? = null
    private var accountEntriesCollectorJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            _state.value = OverviewUiState.empty()

            appStateRepository.state.collect {
                val receiveAddress = it.publicKey
                    ?.toAddress(AttoAlgorithm.V1)
                    ?.value

                if (receiveAddress != state.value.receiveAddress)
                    clearAccountData()

                _state.emit(
                    state.value.copy(receiveAddress = receiveAddress)
                )
            }
        }

        scope.launch {
            walletManagerRepository.state
                .filterNotNull()
                .collect { wallet ->
                    println(
                        "OverviewViewModel is collecting account information from wallet ${
                            AttoAddress(
                                AttoAlgorithm.V1,
                                wallet.publicKey
                            )
                        }"
                    )
                    accountCollectorJob?.cancel()
                    accountCollectorJob = scope.launch {
                        wallet.accountFlow.collect { account ->
                            println("Account $account")
                            val entries = persistentAccountEntryRepository.list(account.publicKey)

                            _state.emit(
                                state.value.copy(
                                    balance = account.balance
                                        .toString(AttoUnit.ATTO)
                                        .toBigDecimal(),
                                    entries = entries
                                )
                            )
                        }
                    }

                    accountEntriesCollectorJob?.cancel()
                    accountEntriesCollectorJob = scope.launch {
                        persistentAccountEntryRepository.flow(wallet.publicKey).collect { _ ->
                            _state.emit(
                                state.value.copy(
                                    entries = persistentAccountEntryRepository.list(wallet.publicKey)
                                )
                            )
                        }
                    }
                }
        }
    }

    private suspend fun clearAccountData() {
        _state.emit(
            state.value.copy(
                balance = null,
                entries = emptyList()
            )
        )
    }
}