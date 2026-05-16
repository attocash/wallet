package cash.atto.wallet.repository

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoHeight
import cash.atto.commons.AttoInstant
import cash.atto.commons.AttoKeyIndex
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoReceivable
import cash.atto.commons.AttoSendBlock
import cash.atto.commons.fromHexToByteArray
import cash.atto.commons.toAttoIndex
import cash.atto.commons.toSigner
import cash.atto.wallet.model.AccountPreference
import cash.atto.wallet.model.AccountPreferenceStatus
import cash.atto.wallet.model.WorkPreference
import cash.atto.wallet.model.WorkSourcePreference
import cash.atto.wallet.state.AppState
import cash.atto.wallet.worker.isLocalWorkerSupported
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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

private data class WalletPreferencesSnapshot(
    val appState: AppState,
    val accounts: Map<String, AccountPreference>,
    val work: WorkPreference,
)

class WalletManagerRepository(
    private val appStateRepository: AppStateRepository,
    private val preferencesRepository: PreferencesRepository,
    private val persistentAccountEntryRepository: PersistentAccountEntryRepository,
    private val workCache: PersistentWorkCache,
    private val network: AttoNetwork,
) {
    private val retryDelay = 10.seconds
    private val sessionFactory = WalletSessionFactory(network, workCache, retryDelay)

    private val _accountState = MutableStateFlow<AttoAccount?>(null)
    val accountState = _accountState.asStateFlow()
    private val _publicKeyState = MutableStateFlow<AttoPublicKey?>(null)
    val publicKeyState = _publicKeyState.asStateFlow()
    private val _accountsState = MutableStateFlow<List<WalletAccountState>>(emptyList())
    val accountsState = _accountsState.asStateFlow()
    private val _selectedAccountIndexState = MutableStateFlow(MAIN_ACCOUNT_INDEX)
    val selectedAccountIndexState = _selectedAccountIndexState.asStateFlow()
    private val _workReadyState = MutableStateFlow(false)
    val workReadyState = _workReadyState.asStateFlow()
    private val _pendingReceivablesState = MutableStateFlow(PendingReceivablesState.EMPTY)
    val pendingReceivablesState = _pendingReceivablesState.asStateFlow()
    private val receiveJobPauseCount = MutableStateFlow(0)

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var walletSession: WalletSession? = null
    private var selectedAccountIndex: AttoKeyIndex = MAIN_ACCOUNT_INDEX

    init {
        scope.launch {
            combine(
                appStateRepository.state,
                preferencesRepository.state
                    .map { preferences ->
                        preferences.normalizedAccounts()
                    }.distinctUntilChanged(),
                preferencesRepository.work,
            ) { appState, accountPreferences, work ->
                WalletPreferencesSnapshot(
                    appState = appState,
                    accounts = accountPreferences,
                    work = work,
                )
            }.collectLatest { preferences ->
                replaceWalletSession(
                    appState = preferences.appState,
                    accountPreferences = preferences.accounts,
                    work = preferences.work,
                )
            }
        }

        scope.launch {
            combine(
                selectedAccountIndexState,
                workCache.version,
            ) { index, _ ->
                walletSession?.hasReadyWork(index) ?: false
            }.collect {
                _workReadyState.emit(it)
            }
        }
    }

    suspend fun send(
        receiverAddress: AttoAddress,
        amount: AttoAmount,
        timestampProvider: suspend () -> AttoInstant,
    ): AttoSendBlock {
        val session = walletSession ?: throw IllegalStateException("Wallet is not ready yet")
        val block =
            session.send(
                index = selectedAccountIndex,
                receiverAddress = receiverAddress,
                amount = amount,
                timestampProvider = timestampProvider,
            )
        emitSelectedAccount(session)

        return block
    }

    fun pauseReceiveJob(): () -> Unit {
        receiveJobPauseCount.update { it + 1 }
        var resumed = false

        return resume@{
            if (resumed) return@resume

            resumed = true
            receiveJobPauseCount.update { (it - 1).coerceAtLeast(0) }
        }
    }

    suspend fun nodeTimeDifference(currentTime: AttoInstant): Long? = walletSession?.nodeTimeDifference(currentTime)

    suspend fun changeRepresentative(representative: AttoAddress) {
        try {
            val session = walletSession ?: return
            session.changeRepresentative(selectedAccountIndex, representative)
            emitSelectedAccount(session)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

    suspend fun selectAccount(index: UInt) {
        val accountIndex = index.toAttoIndex()
        val session = walletSession
        val activeInPreferences = preferencesRepository.state.value.accountStatus(index) == AccountPreferenceStatus.ACTIVATED
        if (session != null && !session.isActive(accountIndex) && !activeInPreferences) {
            return
        }

        selectedAccountIndex = accountIndex
        _selectedAccountIndexState.emit(accountIndex)
        if (session != null && session.address(accountIndex) != null) {
            emitSelectedAccount(session)
        }
    }

    suspend fun addAccount(name: String?) {
        val index = preferencesRepository.addAccount() ?: return
        nameAccount(index, name.orEmpty())
        selectAccount(index)
    }

    suspend fun setAccountActive(
        index: UInt,
        active: Boolean,
    ) {
        val status =
            if (active) {
                AccountPreferenceStatus.ACTIVATED
            } else {
                AccountPreferenceStatus.DEACTIVATED
            }
        val session = walletSession
        if (!active && selectedAccountIndex.value == index && session != null) {
            session
                .activeIndexes()
                .firstOrNull { it.value != index }
                ?.let {
                    selectedAccountIndex = it
                    _selectedAccountIndexState.emit(it)
                }
        }

        preferencesRepository.setAccountStatus(index, status)
    }

    suspend fun nameAccount(
        index: UInt,
        name: String,
    ) {
        val address = addressForIndex(index) ?: return
        preferencesRepository.saveAddressLabel(
            address = address.toString(),
            label = name,
        )
    }

    private suspend fun replaceWalletSession(
        appState: AppState,
        accountPreferences: Map<String, AccountPreference>,
        work: WorkPreference,
    ) {
        val effectiveWork = work.supportedOnCurrentPlatform()

        walletSession?.close()
        walletSession = null
        _accountState.emit(null)
        _publicKeyState.emit(null)
        _accountsState.emit(emptyList())
        _workReadyState.emit(false)
        _pendingReceivablesState.emit(PendingReceivablesState.EMPTY)

        val session =
            sessionFactory.create(
                appState = appState,
                accountPreferences = accountPreferences,
                work = effectiveWork,
                signerIndex = MAIN_ACCOUNT_INDEX,
            ) ?: return
        walletSession = session

        if (!session.isActive(selectedAccountIndex)) {
            selectedAccountIndex = session.activeIndexes().firstOrNull() ?: MAIN_ACCOUNT_INDEX
        }

        _selectedAccountIndexState.emit(selectedAccountIndex)
        session.cacheNextWorkForActiveAccounts()
        emitAccounts(session)
        emitSelectedAccount(session)

        startAccountCollector(session)
        startAccountEntrySaver(session)
        startReceivableCollector(session)
        startBackgroundReceiver(session, effectiveWork)
    }

    private fun startAccountCollector(session: WalletSession) {
        session.launch {
            session.accountStream().collect { account ->
                session.updateAccount(account)
                session.cacheNextWork(account)
                emitAccounts(session)
                if (session.isAccount(selectedAccountIndex, account)) {
                    emitSelectedAccount(session)
                }
            }
        }
    }

    private fun startAccountEntrySaver(session: WalletSession) {
        session.launch {
            val entries =
                session.accountEntryStream { address ->
                    persistentAccountEntryRepository.last(address.publicKey)?.height?.next()
                        ?: AttoHeight.MIN
                }

            entries.collect { message ->
                retryWalletOperation("save account entry ${message.value.hash}", retryDelay) {
                    persistentAccountEntryRepository.save(message.value)
                    message.acknowledge()
                }
            }
        }
    }

    private fun startReceivableCollector(session: WalletSession) {
        session.launch {
            retryWalletOperation("stream receivables", retryDelay) {
                session.receivableStream().collect { receivable ->
                    enqueueReceivable(receivable)
                }
            }
        }
    }

    private fun startBackgroundReceiver(
        session: WalletSession,
        work: WorkPreference,
    ) {
        session.launch {
            while (isActive) {
                val nextReceivable = pendingReceivablesState.value.receivables.firstOrNull()

                if (nextReceivable == null) {
                    delay(1_000)
                    continue
                }

                try {
                    receiveJobPauseCount.first { it == 0 }
                    session.receive(nextReceivable, defaultRepresentatives.random())
                    emitAccounts(session)
                    emitSelectedAccount(session)
                    dequeueReceivable(nextReceivable.hash.toString())
                    delay(work.receiverRateLimit)
                } catch (ex: CancellationException) {
                    throw ex
                } catch (ex: Exception) {
                    println("Failed to receive ${nextReceivable.hash}: ${ex.message}")
                    delay(retryDelay)
                }
            }
        }
    }

    private suspend fun emitAccounts(session: WalletSession) {
        _accountsState.emit(session.accountStates())
    }

    private suspend fun emitSelectedAccount(session: WalletSession) {
        val publicKey = session.publicKey(selectedAccountIndex)
        val account = session.account(selectedAccountIndex)

        _publicKeyState.emit(publicKey)
        _accountState.emit(account)
        _workReadyState.emit(session.hasReadyWork(selectedAccountIndex))
    }

    private suspend fun addressForIndex(index: UInt): AttoAddress? {
        val accountIndex = index.toAttoIndex()
        walletSession?.address(accountIndex)?.let { return it }
        return appStateRepository.state.value
            .getSeed()
            ?.toSigner(accountIndex)
            ?.address
    }

    private fun enqueueReceivable(receivable: AttoReceivable) {
        val hash = receivable.hash.toString()
        _pendingReceivablesState.update { state ->
            if (state.receivables.any { it.hash.toString() == hash }) {
                state
            } else {
                PendingReceivablesState(state.receivables + receivable)
            }
        }
    }

    private fun dequeueReceivable(hash: String) {
        _pendingReceivablesState.update { state ->
            PendingReceivablesState(
                state.receivables.filterNot { it.hash.toString() == hash },
            )
        }
    }

    private companion object {
        val MAIN_ACCOUNT_INDEX: AttoKeyIndex = 0U.toAttoIndex()
    }
}

private suspend fun WorkPreference.supportedOnCurrentPlatform(): WorkPreference =
    if (source == WorkSourcePreference.LOCAL && !isLocalWorkerSupported()) {
        WorkPreference.forSource(WorkSourcePreference.REMOTE)
    } else {
        normalized()
    }
