package cash.atto.wallet.repository

import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoNetwork
import cash.atto.commons.gatekeeper.AttoAuthenticator
import cash.atto.commons.gatekeeper.attoBackend
import cash.atto.commons.toSigner
import cash.atto.commons.wallet.AttoNodeClient
import cash.atto.commons.wallet.AttoTransactionRepository
import cash.atto.commons.wallet.AttoWalletManager
import cash.atto.commons.wallet.AttoWalletViewer
import cash.atto.commons.wallet.AttoWorkCache
import cash.atto.commons.wallet.attoBackend
import cash.atto.commons.wallet.inMemory
import cash.atto.commons.worker.AttoWorker
import cash.atto.commons.worker.attoBackend
import cash.atto.wallet.state.AppState
import cash.atto.wallet.state.RepresentativeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WalletManagerRepository(
    private val appStateRepository: AppStateRepository,
    private val representativeRepository: RepresentativeRepository
) {

    private val _state = MutableStateFlow<AttoWalletManager?>(null)
    val state = _state.asStateFlow()

    private val appStateCollectorScope = CoroutineScope(Dispatchers.Default)
    private var appStateCollectorJob: Job? = null

    private val collectorScope = CoroutineScope(Dispatchers.Default)
    private var collectorJob: Job? = null

    init {
        appStateCollectorJob = appStateCollectorScope.launch {
            appStateRepository.state.collect {
                representativeRepository.getRepresentative(
                    it.publicKey.toString()
                )
            }
        }

        collectorJob = collectorScope.launch {
            appStateRepository.state.combine(
                representativeRepository.state
            ) { appState, representativeState ->
                createWalletManager(appState, representativeState)
            }.collect {
                _state.emit(it)
            }
        }
    }

    private fun createWalletManager(
        appState: AppState,
        representativeState: RepresentativeState
    ): AttoWalletManager? {
        if (appState.privateKey == null || representativeState.representative == null)
            return null

        val signer = appState.privateKey.toSigner()
        val authenticator = AttoAuthenticator.attoBackend(AttoNetwork.DEV, signer)
        val client = AttoNodeClient.attoBackend(AttoNetwork.DEV, authenticator)
        val walletManager = AttoWalletManager(
            viewer = AttoWalletViewer(
                publicKey = signer.publicKey,
                client = client,
                transactionRepository = AttoTransactionRepository.inMemory()
            ),
            signer = signer,
            client = client,
            worker = AttoWorker.attoBackend(authenticator),
            workCache = AttoWorkCache.inMemory()
        ) {
            AttoAddress.parse(representativeState.representative)
//            AttoAddress(AttoAlgorithm.V1, signer.publicKey)
        }

        walletManager.start()

        return walletManager
    }
}