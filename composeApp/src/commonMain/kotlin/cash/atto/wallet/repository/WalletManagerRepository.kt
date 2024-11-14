package cash.atto.wallet.repository

import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoNetwork
import cash.atto.commons.gatekeeper.AttoAuthenticator
import cash.atto.commons.gatekeeper.attoBackend
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
import cash.atto.wallet.state.AppState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WalletManagerRepository(
    private val appStateRepository: AppStateRepository
) {

    private val _state = MutableStateFlow<AttoWalletManager?>(null)
    val state = _state.asStateFlow()

    private val appStateCollectorScope = CoroutineScope(Dispatchers.Default)
    private var appStateCollectorJob: Job? = null

    init {
        appStateCollectorJob = appStateCollectorScope.launch {
            appStateRepository.state.collect {
                try {
                    state.value?.close()
                }
                catch (_: CancellationException) {}

                _state.emit(createWalletManager(it))
            }
        }
    }

    suspend fun changeRepresentative(representative: AttoAddress) {
        try {
            state.value?.change(representative)
        }
        catch (ex: Exception) {
            println(ex.message)
        }
    }

    private suspend fun createWalletManager(
        appState: AppState
    ): AttoWalletManager? {
        if (appState.privateKey == null)
            return null

        val signer = appState.privateKey.toSigner()
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