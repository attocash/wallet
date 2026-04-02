package cash.atto.wallet.repository

import cash.atto.commons.*
import cash.atto.commons.gatekeeper.AttoAuthenticator
import cash.atto.commons.gatekeeper.attoBackend
import cash.atto.commons.node.AttoNodeClient
import cash.atto.commons.wallet.AttoWalletManager
import cash.atto.commons.wallet.AttoWalletViewer
import cash.atto.commons.worker.AttoWorker
import cash.atto.wallet.state.AppState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.seconds


private val defaultRepresentatives = listOf(
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("d50d27281df93e71e6e7a279fc308d5e90891c27a1129cd0bf24bf94731f547f".fromHexToByteArray()), // 0conservative
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("ebc906cc473fce6a5bc1e58622d6d807cb4bc820ac13169d88f96ee26d29b982".fromHexToByteArray()), // 1prudent
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("e7877ac47b475989f3c24783e2d5c0ffc4943d1d0c48c02ba152305b0d42ff5b".fromHexToByteArray()), // 2cautious
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("e00ba25f78dcd0f229a9335060256cef12609d161daf2fd614dc32c37ccdc56a".fromHexToByteArray()), // 3adventurous
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("A73F0B55A50D3BB89EEAD7B47005FD488CC2F540F342F893B72DD357200B4432".fromHexToByteArray()), // 4conservative2
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("5DF80120502F2EAC7CBF1AF12555B5A82990A66745FF89573D6093F535AC25E8".fromHexToByteArray()), // 5prudent2
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("6C9A4B64BBB6ED51A305D1185D83288A62378DBE761E932E38C05C17623D74B1".fromHexToByteArray()), // 6cautious2
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("1D7948C4D449AC8151D7855F44276308E98C899C6F066EB9324DFFAA15901929".fromHexToByteArray()), // 7adventurous2
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("853380FF0F905B3801F3A32FA193250395B4BD0CA6D83E76EEA7389EC9D4BA10".fromHexToByteArray()), // 8conservative3
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("58D293037FDFE6318AC97215E3E8A202CB27637A17831CBFB1964A4534343C48".fromHexToByteArray()), // 9prudent3
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("45EB652403CA618C5F1CFF92B2BB1F4C7D86B903E880721937F07EA9E0E5648F".fromHexToByteArray()), // 10cautious3
    ),
    AttoAddress(
        AttoAlgorithm.V1,
        AttoPublicKey("096523285E1FE5755DAABB94ECAB124D0E395E1D2E613E97D10AA2717B82A4CE".fromHexToByteArray()), // 11adventurous3
    ),
)

class WalletManagerRepository(
    private val appStateRepository: AppStateRepository,
    private val persistentAccountEntryRepository: PersistentAccountEntryRepository,
    private val workCache: PersistentWorkCache,
    private val network: AttoNetwork,
) {
    private val receiveInterval = 10.seconds

    private val _state = MutableStateFlow<AttoWalletManager?>(null)
    val state = _state.asStateFlow()
    private val _pendingReceivablesState = MutableStateFlow(PendingReceivablesState.EMPTY)
    val pendingReceivablesState = _pendingReceivablesState.asStateFlow()

    private val appStateCollectorScope = CoroutineScope(Dispatchers.Default)
    private var appStateCollectorJob: Job? = null
    private val receivableCollectorScope = CoroutineScope(Dispatchers.Default)
    private var receivableCollectorJob: Job? = null
    private val receiverScope = CoroutineScope(Dispatchers.Default)
    private var receiverJob: Job? = null

    init {
        appStateCollectorJob = appStateCollectorScope.launch {
            appStateRepository.state.collect {
                try {
                    state.value?.close()
                } catch (_: CancellationException) {
                }

                receivableCollectorJob?.cancel()
                receiverJob?.cancel()
                _pendingReceivablesState.emit(PendingReceivablesState.EMPTY)

                val walletManager = createWalletManager(it)
                _state.emit(walletManager)

                if (walletManager != null) {
                    startReceivableCollector(walletManager)
                    startBackgroundReceiver(walletManager)
                }
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
        val privateKey = appState.getPrivateKey() ?: return null
        val signer = privateKey.toSigner()
        val authenticator = AttoAuthenticator.attoBackend(network, signer)
        val client = AttoNodeClient.attoBackend(network, authenticator)

        val walletManager = AttoWalletManager(
            network,
            viewer = AttoWalletViewer(
                publicKey = signer.publicKey,
                client = client,
                accountEntryRepository = persistentAccountEntryRepository,
            ),
            signer = signer,
            client = client,
            worker = AttoWorker.attoBackend(network, authenticator),
            workCache = workCache
        ) {
            defaultRepresentatives.random()
        }

        walletManager.start(autoReceive = false)

        return walletManager
    }

    private fun startReceivableCollector(walletManager: AttoWalletManager) {
        receivableCollectorJob?.cancel()
        receivableCollectorJob = receivableCollectorScope.launch {
            walletManager.receivableFlow.collect { receivable ->
                enqueueReceivable(receivable)
            }
        }
    }

    private fun startBackgroundReceiver(walletManager: AttoWalletManager) {
        receiverJob?.cancel()
        receiverJob = receiverScope.launch {
            while (isActive) {
                val nextReceivable = pendingReceivablesState.value.receivables.firstOrNull()

                if (nextReceivable == null) {
                    delay(1_000)
                    continue
                }

                try {
                    walletManager.receive(nextReceivable)
                    dequeueReceivable(nextReceivable.hash.toString())
                    delay(receiveInterval)
                } catch (ex: Exception) {
                    println("Failed to receive ${nextReceivable.hash}: ${ex.message}")
                    delay(10_000)
                }
            }
        }
    }

    private suspend fun enqueueReceivable(receivable: AttoReceivable) {
        val hash = receivable.hash.toString()
        if (_pendingReceivablesState.value.receivables.any { it.hash.toString() == hash }) {
            return
        }

        _pendingReceivablesState.emit(
            PendingReceivablesState(
                _pendingReceivablesState.value.receivables + receivable
            )
        )
    }

    private suspend fun dequeueReceivable(hash: String) {
        _pendingReceivablesState.emit(
            PendingReceivablesState(
                _pendingReceivablesState.value.receivables.filterNot { it.hash.toString() == hash }
            )
        )
    }
}
