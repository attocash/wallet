package cash.atto.wallet.repository

import cash.atto.commons.AttoMnemonic
import cash.atto.wallet.PlatformType
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.datasource.TempSeedDataSource
import cash.atto.wallet.getPlatform
import cash.atto.wallet.interactor.SeedAESInteractor
import cash.atto.wallet.state.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch

class AppStateRepository(
    private val seedDataSource: SeedDataSource,
    private val tempSeedDataSource: TempSeedDataSource,
    private val passwordDataSource: PasswordDataSource,
    private val seedAESInteractor: SeedAESInteractor
) {
    private val _state = MutableStateFlow(AppState.DEFAULT)
    val state = _state.asStateFlow()

    private val sessionScope = CoroutineScope(Dispatchers.Default)
    private var sessionJob: Job? = null

    init {
        CoroutineScope(Dispatchers.Default).launch {
            seedDataSource.seed.collect { seed ->
                if (getPlatform().type == PlatformType.WEB) {
                    if (seed != null) {
                        setAuthState(AppState.AuthState.NO_PASSWORD)
                        setEncryptedSeed(seed)
                    } else {
                        setAuthState(AppState.AuthState.NO_SEED)
                        setEncryptedSeed(null)
                        setMnemonic(null)
                    }

                    return@collect
                }

                val mnemonic = seed?.let { AttoMnemonic(it) }
                setMnemonic(mnemonic)

                seed?.let {
                    val password = passwordDataSource.getPassword(it)
                    if (state.value.authState != AppState.AuthState.NEW_ACCOUNT &&
                        state.value.authState != AppState.AuthState.SESSION_VALID
                    ) {
                        setPassword(password)
                        setAuthState(
                            password?.let {
                                AppState.AuthState.SESSION_INVALID
                            } ?: AppState.AuthState.NO_PASSWORD
                        )
                    }
                } ?: setAuthState(AppState.AuthState.NO_SEED)
            }
        }
    }

    suspend fun generateNewSecret(): List<String> {
        val mnemonic = AttoMnemonic.generate()
        val seed = mnemonic.words.joinToString(" ")

        // If in web, don't store the seed yet
        if (getPlatform().type == PlatformType.WEB)
            tempSeedDataSource.seed = seed
        else seedDataSource.setSeed(seed)

        setAuthState(AppState.AuthState.NEW_ACCOUNT)

        return mnemonic.words
    }

    suspend fun importSecret(secret: List<String>) {
        val seed = secret.joinToString(" ")

        // If in web, don't store the seed yet
        if (getPlatform().type == PlatformType.WEB)
            tempSeedDataSource.seed = seed
        else seedDataSource.setSeed(seed)

        val password = passwordDataSource.getPassword(seed)
        if (password == null)
            setAuthState(AppState.AuthState.NEW_ACCOUNT)
    }

    suspend fun submitPassword(password: String): Boolean {
        when (getPlatform().type) {
            PlatformType.WEB -> {
                val decrypted = seedAESInteractor
                    .decryptSeed(
                        encryptedSeed = state.value.encryptedSeed.orEmpty(),
                        password = password
                    )

                try {
                    val mnemonic = AttoMnemonic(decrypted)
                    setMnemonic(mnemonic)
                    startSession()

                    return true
                }
                catch (ex: Exception) {
                    return false
                }
            }
            else -> {
                if (password == state.value.password) {
                    startSession()
                    return true
                }

                return false
            }
        }

        return false
    }

    suspend fun savePassword(password: String) {
        // If the platform is web, we store the seed
        if (getPlatform().type == PlatformType.WEB) {
            tempSeedDataSource.seed?.let {
                seedDataSource.setSeed(
                    seedAESInteractor.encryptSeed(it, password)
                )
            }
        }

        state.value
            .mnemonic
            ?.words
            ?.let {
                passwordDataSource.setPassword(
                    seed = it.joinToString(" "),
                    password = password
                )

                setPassword(password)
                startSession()
            }
    }

    suspend fun deleteKeys() {
        seedDataSource.clearSeed()
    }

    private suspend fun setEncryptedSeed(
        encryptedSeed: String?
    ) {
        _state.emit(
            state.value.copy(
                encryptedSeed = encryptedSeed
            )
        )
    }

    private suspend fun setMnemonic(
        mnemonic: AttoMnemonic?,
    ) {
        _state.emit(
            state.value.copy(
                mnemonic = mnemonic
            )
        )
    }

    private suspend fun setAuthState(authState: AppState.AuthState) {
        _state.emit(
            state.value.copy(
                authState = authState
            )
        )
    }

    private suspend fun setPassword(password: String?) {
        _state.emit(
            state.value.copy(
                password = password
            )
        )
    }

    private suspend fun startSession() {
        _state.emit(
            state.value.copy(
                authState = AppState.AuthState.SESSION_VALID
            )
        )

        sessionJob?.cancel()
        sessionJob = sessionScope.launch {
            delay(SESSION_DURATION)

            _state.emit(
                state.value.copy(
                    authState = AppState.AuthState.SESSION_INVALID
                )
            )
        }
    }

    companion object {
        private const val SESSION_DURATION = 20 * 60 * 1000L
    }
}