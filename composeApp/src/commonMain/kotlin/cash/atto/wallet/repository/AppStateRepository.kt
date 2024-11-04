package cash.atto.wallet.repository

import cash.atto.commons.AttoMnemonic
import cash.atto.wallet.state.AppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SeedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppStateRepository(
    private val seedDataSource: SeedDataSource,
    private val passwordDataSource: PasswordDataSource
) {
    private val _state = MutableStateFlow(AppState.DEFAULT)
    val state = _state.asStateFlow()

    private val sessionScope = CoroutineScope(Dispatchers.IO)
    private var sessionJob: Job? = null

    init {
        CoroutineScope(Dispatchers.Default).launch {
            seedDataSource.seed.collect { seed ->
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
        seedDataSource.setSeed(
            mnemonic.words.joinToString(" ")
        )

        setAuthState(AppState.AuthState.NEW_ACCOUNT)

        return mnemonic.words
    }

    suspend fun importSecret(secret: List<String>) {
        seedDataSource.setSeed(secret.joinToString(" "))
    }

    suspend fun submitPassword(password: String): Boolean {
        if (password == state.value.password) {
            startSession()
            return true
        }

        return false
    }

    suspend fun savePassword(password: String) = state.value
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

    private suspend fun setMnemonic(
        mnemonic: AttoMnemonic?,
    ) {
        _state.emit(state.value.copy(
            mnemonic = mnemonic
        ))
    }

    private suspend fun setAuthState(authState: AppState.AuthState) {
        _state.emit(state.value.copy(
            authState = authState
        ))
    }

    private suspend fun setPassword(password: String?) {
        _state.emit(state.value.copy(
            password = password
        ))
    }

    private suspend fun startSession() {
        _state.emit(state.value.copy(
            authState = AppState.AuthState.SESSION_VALID
        ))

        sessionJob?.cancel()
        sessionJob = sessionScope.launch {
            delay(SESSION_DURATION)

            _state.emit(state.value.copy(
                authState = AppState.AuthState.SESSION_INVALID
            ))
        }
    }

    suspend fun deleteKeys() {
        seedDataSource.clearSeed()
    }

    companion object {
        private const val SESSION_DURATION = 20 * 60 * 1000L
    }
}