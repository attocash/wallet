package cash.atto.wallet.repository

import cash.atto.commons.AttoMnemonic
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.datasource.TempSeedDataSource
import cash.atto.wallet.interactor.SeedAESInteractor
import cash.atto.wallet.state.AppState
import cash.atto.wallet.state.AppState.AuthState
import cash.atto.wallet.state.UnlockedWallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppStateRepository internal constructor(
    private val keyStore: WalletKeyStore,
    private val tempSeedDataSource: TempSeedDataSource,
    private val scope: CoroutineScope,
) {
    constructor(
        seedDataSource: SeedDataSource,
        tempSeedDataSource: TempSeedDataSource,
        passwordDataSource: PasswordDataSource,
        seedAESInteractor: SeedAESInteractor,
    ) : this(
        PlatformWalletKeyStore(seedDataSource, passwordDataSource, seedAESInteractor),
        tempSeedDataSource,
        CoroutineScope(Dispatchers.Default),
    )

    private val _state = MutableStateFlow(AppState.DEFAULT)
    val state = _state.asStateFlow()

    // Authentication changes and storage writes share one ordering, including lock during unlock.
    private val mutex = Mutex()
    private var expiryJob: Job? = null

    init {
        scope.launch {
            mutex.withLock {
                if (state.value.authState == AuthState.UNKNOWN) {
                    _state.value = AppState(authState = keyStore.authState())
                }
            }
        }
    }

    suspend fun generateNewSecret(): List<String> =
        mutex.withLock {
            val mnemonic = AttoMnemonic.generate()
            revoke(AuthState.NEW_ACCOUNT)
            tempSeedDataSource.seed = mnemonic.words.joinToString(" ")
            mnemonic.words
        }

    suspend fun importSecret(secret: List<String>) =
        mutex.withLock {
            val mnemonic = AttoMnemonic.fromWords(secret)
            revoke(AuthState.NEW_ACCOUNT)
            tempSeedDataSource.seed = mnemonic.words.joinToString(" ")
        }

    suspend fun submitPassword(password: String): Boolean =
        mutex.withLock {
            val mnemonic = keyStore.unlock(password) ?: return@withLock false
            startSession(mnemonic, password)
            true
        }

    suspend fun savePassword(password: String) =
        mutex.withLock {
            check(state.value.authState == AuthState.NEW_ACCOUNT || state.value.authState == AuthState.NO_PASSWORD) {
                "No wallet is awaiting a password"
            }
            val mnemonic = keyStore.savePassword(tempSeedDataSource.seed, password)
            startSession(mnemonic, password)
        }

    suspend fun deleteKeys() =
        mutex.withLock {
            revoke(AuthState.NO_SEED)
            keyStore.clear()
        }

    suspend fun lock() =
        mutex.withLock {
            revoke(if (state.value.authState == AuthState.NO_SEED) AuthState.NO_SEED else AuthState.SESSION_INVALID)
        }

    private fun revoke(authState: AuthState) {
        expiryJob?.cancel()
        expiryJob = null
        state.value.unlockedWallet?.close()
        tempSeedDataSource.seed = null
        _state.value = AppState(authState = authState)
    }

    private fun startSession(
        mnemonic: AttoMnemonic,
        password: String,
    ) {
        revoke(AuthState.SESSION_INVALID)
        val unlockedWallet = UnlockedWallet(mnemonic, password, scope.coroutineContext)
        _state.value = AppState(authState = AuthState.SESSION_VALID, unlockedWallet = unlockedWallet)
        expiryJob =
            scope.launch {
                delay(SESSION_DURATION)
                mutex.withLock {
                    if (state.value.unlockedWallet === unlockedWallet) {
                        revoke(AuthState.SESSION_INVALID)
                    }
                }
            }
    }

    companion object {
        private const val SESSION_DURATION = 20 * 60 * 1000L
    }
}
