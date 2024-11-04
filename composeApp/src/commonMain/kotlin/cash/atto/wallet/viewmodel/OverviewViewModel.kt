package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoUnit
import cash.atto.commons.toAddress
import cash.atto.commons.wallet.AttoAccountEntryRepository
import cash.atto.wallet.repository.AppStateRepository
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
    private val accountEntryRepository: AttoAccountEntryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState.DEFAULT)
    val state = _state.asStateFlow()

    private var accountCollectorJob: Job? = null
    private var transactionsCollectorJob: Job? = null

    private val accountCollectorScope = CoroutineScope(Dispatchers.Default)
    private val transactionsCollectorScope = CoroutineScope(Dispatchers.Default)

    init {
        CoroutineScope(Dispatchers.Default).launch {
            _state.value = OverviewUiState.empty()

            appStateRepository.state.collect {
                _state.emit(
                    state.value.copy(
                        receiveAddress = it.publicKey
                            ?.toAddress(AttoAlgorithm.V1)
                            ?.value
                    )
                )
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            walletManagerRepository.state
                .filterNotNull()
                .collect { wallet ->
                    println("Wallet ${AttoAddress(AttoAlgorithm.V1, wallet.publicKey)} is ready")
                    accountCollectorJob?.cancel()
                    accountCollectorJob = accountCollectorScope.launch {
                        wallet.accountFlow.collect { account ->
                            println("Account $account")
                            val entries = accountEntryRepository.list(account.publicKey)

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

                    transactionsCollectorJob?.cancel()
                    transactionsCollectorJob = transactionsCollectorScope.launch {
                        wallet.accountEntryFlow.collect { entries ->
                            accountEntryRepository.save(entries)

                            _state.emit(
                                state.value.copy(
                                    entries = state.value.entries + entries
                                )
                            )
                        }
                    }
                }
        }
    }
}