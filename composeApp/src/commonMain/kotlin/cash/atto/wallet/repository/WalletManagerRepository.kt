package cash.atto.wallet.repository

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoHeight
import cash.atto.commons.AttoKeyIndex
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoReceivable
import cash.atto.commons.AttoSendBlock
import cash.atto.commons.fromHexToByteArray
import cash.atto.commons.gatekeeper.AttoAuthenticator
import cash.atto.commons.gatekeeper.attoBackend
import cash.atto.commons.node.AttoNodeClient
import cash.atto.commons.node.monitor.AttoAccountMonitor
import cash.atto.commons.node.monitor.createAccountMonitor
import cash.atto.commons.node.monitor.toAccountEntryMonitor
import cash.atto.commons.toAttoAmount
import cash.atto.commons.toAttoIndex
import cash.atto.commons.toSigner
import cash.atto.commons.wallet.AttoWallet
import cash.atto.commons.wallet.AttoWalletAccount
import cash.atto.commons.wallet.create
import cash.atto.commons.worker.AttoWorker
import cash.atto.commons.worker.cached
import cash.atto.commons.worker.retry
import cash.atto.wallet.state.AppState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private val defaultRepresentatives =
    listOf(
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
    private val retryDelay = 10.seconds

    private val _accountState = MutableStateFlow<AttoAccount?>(null)
    val accountState = _accountState.asStateFlow()
    private val _publicKeyState = MutableStateFlow<AttoPublicKey?>(null)
    val publicKeyState = _publicKeyState.asStateFlow()
    private val _pendingReceivablesState = MutableStateFlow(PendingReceivablesState.EMPTY)
    val pendingReceivablesState = _pendingReceivablesState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var walletSession: WalletSession? = null

    init {
        scope.launch {
            appStateRepository.state.collectLatest { appState ->
                replaceWalletSession(appState)
            }
        }
    }

    suspend fun send(
        receiverAddress: AttoAddress,
        amount: AttoAmount,
    ): AttoSendBlock {
        val session = walletSession ?: throw IllegalStateException("Wallet is not ready yet")
        val account = session.walletAccount.account ?: throw IllegalStateException("Account is not open yet")

        require(receiverAddress.publicKey != session.publicKey) { "You can't send $amount to yourself" }
        if (amount > account.balance) {
            throw IllegalStateException("${account.balance} balance is not enough to send $amount")
        }

        val transaction =
            session.wallet.send(
                index = MAIN_ACCOUNT_INDEX,
                receiverAddress = receiverAddress,
                amount = amount,
            )

        emitAccount(session.walletAccount.account)

        return transaction.block as? AttoSendBlock
            ?: throw IllegalStateException("Expected send block but received ${transaction.block::class}")
    }

    suspend fun changeRepresentative(representative: AttoAddress) {
        try {
            val session = walletSession ?: return
            session.wallet.change(MAIN_ACCOUNT_INDEX, representative)
            emitAccount(session.walletAccount.account)
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

    private suspend fun replaceWalletSession(appState: AppState) {
        walletSession?.close()
        walletSession = null
        _accountState.emit(null)
        _publicKeyState.emit(null)
        _pendingReceivablesState.emit(PendingReceivablesState.EMPTY)

        val session = createWalletSession(appState) ?: return
        walletSession = session
        _publicKeyState.emit(session.publicKey)
        val account = session.walletAccount.account
        if (account != null) {
            session.cacheNextWork(account)
        }
        emitAccount(account)

        startAccountCollector(session)
        startAccountEntrySaver(session)
        startReceivableCollector(session)
        startBackgroundReceiver(session)
    }

    private suspend fun createWalletSession(appState: AppState): WalletSession? {
        val seed = appState.getSeed() ?: return null
        val signer = seed.toSigner(MAIN_ACCOUNT_INDEX)
        val authenticator = AttoAuthenticator.attoBackend(network, signer)
        val client = AttoNodeClient.attoBackend(network, authenticator)
        val worker =
            PersistentWorkCachingWorker(
                network = network,
                workCache = workCache,
                delegate = AttoWorker.attoBackend(network, authenticator).retry(retryDelay).cached(),
            )
        val wallet = AttoWallet.create(client, worker, seed)
        val walletAccount =
            retrying("open wallet account") {
                wallet.openAccount(MAIN_ACCOUNT_INDEX)
            }
        val accountMonitor = client.createAccountMonitor()
        accountMonitor.monitor(walletAccount.address)

        return WalletSession(
            wallet = wallet,
            walletAccount = walletAccount,
            client = client,
            accountMonitor = accountMonitor,
            worker = worker,
        )
    }

    private fun startAccountCollector(session: WalletSession) {
        session.launch {
            while (isActive) {
                try {
                    session.accountStream().collect { account ->
                        session.walletAccount.account = account
                        session.cacheNextWork(account)
                        emitAccount(account)
                    }
                    return@launch
                } catch (ex: CancellationException) {
                    throw ex
                } catch (ex: Exception) {
                    println("Failed to stream account ${session.publicKey}: ${ex.message}")
                    delay(retryDelay)
                }
            }
        }
    }

    private fun startAccountEntrySaver(session: WalletSession) {
        val accountEntryMonitor =
            session.accountMonitor.toAccountEntryMonitor { address ->
                persistentAccountEntryRepository.last(address.publicKey)?.height?.next()
                    ?: AttoHeight.MIN
            }

        session.launch {
            accountEntryMonitor.stream().collect { message ->
                retrying("save account entry ${message.value.hash}") {
                    persistentAccountEntryRepository.save(message.value)
                    message.acknowledge()
                }
            }
        }
    }

    private fun startReceivableCollector(session: WalletSession) {
        session.launch {
            session.accountMonitor.receivableStream(minAmount = 1UL.toAttoAmount()).collect { receivable ->
                enqueueReceivable(receivable)
            }
        }
    }

    private fun startBackgroundReceiver(session: WalletSession) {
        session.launch {
            while (isActive) {
                val nextReceivable = pendingReceivablesState.value.receivables.firstOrNull()

                if (nextReceivable == null) {
                    delay(1_000)
                    continue
                }

                try {
                    session.wallet.receive(nextReceivable, defaultRepresentatives.random())
                    emitAccount(session.walletAccount.account)
                    dequeueReceivable(nextReceivable.hash.toString())
                    delay(receiveInterval)
                } catch (ex: Exception) {
                    println("Failed to receive ${nextReceivable.hash}: ${ex.message}")
                    delay(retryDelay)
                }
            }
        }
    }

    private suspend fun emitAccount(account: AttoAccount?) {
        _accountState.emit(account)
    }

    private suspend fun enqueueReceivable(receivable: AttoReceivable) {
        val hash = receivable.hash.toString()
        if (_pendingReceivablesState.value.receivables.any { it.hash.toString() == hash }) {
            return
        }

        _pendingReceivablesState.emit(
            PendingReceivablesState(
                _pendingReceivablesState.value.receivables + receivable,
            ),
        )
    }

    private suspend fun dequeueReceivable(hash: String) {
        _pendingReceivablesState.emit(
            PendingReceivablesState(
                _pendingReceivablesState.value.receivables.filterNot { it.hash.toString() == hash },
            ),
        )
    }

    private suspend fun <T> retrying(
        description: String,
        action: suspend () -> T,
    ): T {
        while (currentCoroutineContext().isActive) {
            try {
                return action()
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                println("Failed to $description: ${ex.message}")
                delay(retryDelay)
            }
        }
        throw CancellationException("Cancelled while trying to $description")
    }

    private class WalletSession(
        val wallet: AttoWallet,
        val walletAccount: AttoWalletAccount,
        val client: AttoNodeClient,
        val accountMonitor: AttoAccountMonitor,
        private val worker: PersistentWorkCachingWorker,
    ) {
        private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        val publicKey: AttoPublicKey = walletAccount.address.publicKey

        fun launch(block: suspend CoroutineScope.() -> Unit): Job = scope.launch(block = block)

        @OptIn(ExperimentalCoroutinesApi::class)
        fun accountStream() =
            accountMonitor.membershipFlow().flatMapLatest { addresses ->
                if (addresses.isEmpty()) {
                    emptyFlow()
                } else {
                    client.accountStream(addresses)
                }
            }

        suspend fun cacheNextWork(account: AttoAccount) {
            worker.cacheNextWork(account)
        }

        fun close() {
            scope.cancel()
            worker.close()
        }
    }

    private companion object {
        val MAIN_ACCOUNT_INDEX: AttoKeyIndex = 0U.toAttoIndex()
    }
}
