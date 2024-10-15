package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoUnit
import cash.atto.commons.toSigner
import cash.atto.commons.wallet.AttoClient
import cash.atto.commons.wallet.AttoTransactionRepository
import cash.atto.commons.wallet.AttoWalletManager
import cash.atto.commons.wallet.AttoWalletViewer
import cash.atto.commons.wallet.AttoWorkCache
import cash.atto.commons.wallet.createAtto
import cash.atto.commons.wallet.inMemory
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.state.AppState
import cash.atto.wallet.uistate.overview.OverviewHeaderUiState
import cash.atto.wallet.uistate.overview.OverviewUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class OverviewViewModel(
    private val appStateRepository: AppStateRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState.DEFAULT)
    val state = _state.asStateFlow()

    private val walletState = MutableStateFlow<AttoWalletManager?>(null)

    init {
        CoroutineScope(Dispatchers.Default).launch {
            _state.value = OverviewUiState.empty()

            appStateRepository.state.collect {
                walletState.emit(createWalletManager(it))
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            walletState.filterNotNull().collect { wallet ->
                wallet.accountFlow.collect { account ->
                    _state.emit(
                        state.value.copy(
                            headerUiState = OverviewHeaderUiState(
                                attoCoins = account.balance
                                    .toString(AttoUnit.ATTO).toBigDecimal()
                            )
                        )
                    )
                }
            }
        }
    }
}

private fun createWalletManager(state: AppState): AttoWalletManager {
    require(state.privateKey != null) {}
    val signer = state.privateKey.toSigner()
    val client = AttoClient.createAtto(AttoNetwork.DEV, signer)
    val transactionRepository = AttoTransactionRepository.inMemory() // TODO persist
    val viewer = AttoWalletViewer(signer.publicKey, client, transactionRepository)
    val workCache = AttoWorkCache.inMemory()
    val walletManager = AttoWalletManager(viewer, signer, client, workCache) {
        AttoAddress(AttoAlgorithm.V1, signer.publicKey)
    }
    walletManager.start()
    return walletManager
}