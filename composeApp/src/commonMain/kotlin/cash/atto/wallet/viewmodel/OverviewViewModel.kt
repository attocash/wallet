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
import cash.atto.commons.wallet.AttoNodeClient
import cash.atto.commons.wallet.AttoTransactionRepository
import cash.atto.commons.wallet.AttoWalletManager
import cash.atto.commons.wallet.AttoWalletViewer
import cash.atto.commons.wallet.AttoWorkCache
import cash.atto.commons.wallet.attoBackend
import cash.atto.commons.wallet.inMemory
import cash.atto.commons.work.AttoWorker
import cash.atto.commons.work.attoBackend
import cash.atto.wallet.interactor.CreateWalletManagerInteractor
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
    private val createWalletManagerInteractor: CreateWalletManagerInteractor
) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState.DEFAULT)
    val state = _state.asStateFlow()

    private val walletState = MutableStateFlow<AttoWalletManager?>(null)

    init {
        CoroutineScope(Dispatchers.Default).launch {
            _state.value = OverviewUiState.empty()

            appStateRepository.state.collect {
                walletState.emit(createWalletManagerInteractor.invoke(it))
                _state.emit(state.value.copy(
                    receiveAddress = it.publicKey
                        ?.toAddress(AttoAlgorithm.V1)
                        ?.value
                ))
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            walletState.filterNotNull().collect { wallet ->
                println("Wallet ${AttoAddress(AttoAlgorithm.V1, wallet.publicKey)} is ready")
                wallet.accountFlow.collect { account ->
                    println("Account $account")
                    _state.emit(
                        state.value.copy(
                            headerUiState = OverviewHeaderUiState(
                                attoCoins = account.balance
                                    .toString(AttoUnit.ATTO)
                                    .toBigDecimal()
                            )
                        )
                    )
                }
            }
        }
    }
}