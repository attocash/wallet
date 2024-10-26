package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoUnit
import cash.atto.commons.toAddress
import cash.atto.commons.wallet.AttoTransactionRepository
import cash.atto.commons.wallet.AttoWalletManager
import cash.atto.wallet.interactor.CreateWalletManagerInteractor
import cash.atto.wallet.repository.AppStateRepository
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
    private val transactionRepository: AttoTransactionRepository,
    private val createWalletManagerInteractor: CreateWalletManagerInteractor
) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState.DEFAULT)
    val state = _state.asStateFlow()

    private var accountCollectorJob: Job? = null
    private var transactionsCollectorJob: Job? = null

    private val accountCollectorScope = CoroutineScope(Dispatchers.Default)
    private val transactionsCollectorScope = CoroutineScope(Dispatchers.Default)

    private val walletState = MutableStateFlow<AttoWalletManager?>(null)

    init {
        CoroutineScope(Dispatchers.Default).launch {
            _state.value = OverviewUiState.empty()

            appStateRepository.state.collect {
                walletState.emit(createWalletManagerInteractor.invoke(it))
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
            walletState.filterNotNull().collect { wallet ->
                println("Wallet ${AttoAddress(AttoAlgorithm.V1, wallet.publicKey)} is ready")
                accountCollectorJob?.cancel()
                accountCollectorJob = accountCollectorScope.launch {
                    wallet.accountFlow.collect { account ->
                        println("Account $account")
                        val transactions = transactionRepository.list(account.publicKey)

                        _state.emit(
                            state.value.copy(
                                balance = account.balance
                                    .toString(AttoUnit.ATTO)
                                    .toBigDecimal(),
                                transactions = transactions
                            )
                        )
                    }
                }

                transactionsCollectorJob?.cancel()
                transactionsCollectorJob = transactionsCollectorScope.launch {
                    wallet.transactionFlow.collect { transaction ->
                        transactionRepository.save(transaction)

                        _state.emit(
                            state.value.copy(
                                transactions = state.value.transactions + transaction
                            )
                        )
                    }
                }
            }
        }
    }
}