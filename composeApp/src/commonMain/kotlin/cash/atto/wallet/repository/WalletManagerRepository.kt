package cash.atto.wallet.repository

import cash.atto.commons.*
import cash.atto.commons.gatekeeper.AttoAuthenticator
import cash.atto.commons.gatekeeper.attoBackend
import cash.atto.commons.wallet.*
import cash.atto.commons.worker.AttoWorker
import cash.atto.commons.worker.attoBackend
import cash.atto.wallet.state.AppState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


private val defaultRepresentatives = listOf(
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("6AA1B03B44706F74BD9E4E56A2D4A48E267A22472ABA35EDC06C3B2D81813133".fromHexToByteArray())
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("89A4A906C0E12AC7CE2F47E76FD4BA01F31F468DE93F4C880F060E346DFE8BD8".fromHexToByteArray())
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("F398E841D500B9697804D956476120BF235A74A34C4367CC6CDEA53A1268A9BC".fromHexToByteArray())
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("B3536E6858291B18432AEEF6127134FB6D0C4786A7EFCC97D05427338945E657".fromHexToByteArray())
    ),
)

class WalletManagerRepository(
    private val appStateRepository: AppStateRepository,
    private val accountEntryRepository: AccountEntryRepository,
    private val network: AttoNetwork,
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
                } catch (_: CancellationException) {
                }

                _state.emit(createWalletManager(it))
            }
        }
    }

    suspend fun changeRepresentative(representative: AttoAddress) {
        try {
            state.value?.change(representative)
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

    private suspend fun createWalletManager(
        appState: AppState
    ): AttoWalletManager? {
        if (appState.privateKey == null)
            return null

        val signer = appState.privateKey.toSigner()
        val authenticator = AttoAuthenticator.attoBackend(network, signer)
        val client = AttoNodeClient.attoBackend(network, authenticator)
        accountEntryRepository.clear()
        val walletManager = AttoWalletManager(
            viewer = AttoWalletViewer(
                publicKey = signer.publicKey,
                client = client,
                accountEntryRepository = accountEntryRepository,
            ),
            signer = signer,
            client = client,
            worker = AttoWorker.attoBackend(authenticator),
            workCache = AttoWorkCache.inMemory()
        ) {
            defaultRepresentatives.random()
        }

        walletManager.start()

        return walletManager
    }
}