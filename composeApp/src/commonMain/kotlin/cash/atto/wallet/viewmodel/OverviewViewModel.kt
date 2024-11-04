package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoUnit
import cash.atto.commons.gatekeeper.AttoAuthenticator
import cash.atto.commons.gatekeeper.attoBackend
import cash.atto.commons.toAddress
import cash.atto.commons.toSigner
import cash.atto.commons.wallet.AttoAccountEntryRepository
import cash.atto.commons.wallet.AttoNodeClient
import cash.atto.commons.wallet.AttoTransactionRepository
import cash.atto.commons.wallet.AttoWalletManager
import cash.atto.commons.wallet.AttoWalletViewer
import cash.atto.commons.wallet.AttoWorkCache
import cash.atto.commons.wallet.attoBackend
import cash.atto.commons.wallet.inMemory
import cash.atto.commons.worker.AttoWorker
import cash.atto.commons.worker.attoBackend
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.state.AppState
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
//    private val walletManagerRepository: WalletManagerRepository,
    private val accountEntryRepository: AttoAccountEntryRepository,
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
                walletState.emit(createWalletManager(it))
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
//            walletManagerRepository.state
            walletState
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

    private suspend fun createWalletManager(state: AppState): AttoWalletManager? {
        if (state.privateKey == null)
            return null

        val signer = state.privateKey.toSigner()
        val authenticator = AttoAuthenticator.attoBackend(AttoNetwork.DEV, signer)
        val client = AttoNodeClient.attoBackend(AttoNetwork.DEV, authenticator)
        val walletManager = AttoWalletManager(
            viewer = AttoWalletViewer(
                publicKey = signer.publicKey,
                client = client,
                accountEntryRepository = AttoAccountEntryRepository.inMemory(),
                transactionRepository = AttoTransactionRepository.inMemory()
            ),
            signer = signer,
            client = client,
            worker = AttoWorker.attoBackend(authenticator),
            workCache = AttoWorkCache.inMemory()
        ) {
            AttoAddress(AttoAlgorithm.V1, signer.publicKey)
        }

        walletManager.start()

        return walletManager
    }
}