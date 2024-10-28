package cash.atto.wallet.repository

import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoMnemonic
import cash.atto.wallet.state.AppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import cash.atto.wallet.datasource.SeedDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppStateRepository(
    private val seedDataSource: SeedDataSource
) {
    private val _state = MutableStateFlow(AppState.DEFAULT)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            seedDataSource.seed.collect { seed ->
                val mnemonic = seed?.let { AttoMnemonic(it) }
                setKeys(mnemonic)
            }
        }
    }

    suspend fun generateNewSecret(): List<String> {
        val mnemonic = AttoMnemonic.generate()
        seedDataSource.setSeed(
            mnemonic.words.joinToString(" ")
        )

        return mnemonic.words
    }

    suspend fun importSecret(secret: List<String>) {
        seedDataSource.setSeed(secret.joinToString(" "))
    }

    private suspend fun setKeys(
        mnemonic: AttoMnemonic?,
    ) {
        _state.emit(
            AppState(
                mnemonic = mnemonic,
                authState = if (mnemonic != null)
                    AppState.AUTH_STATE.LOGGED
                else AppState.AUTH_STATE.UNLOGGED
            )
        )
    }

    suspend fun deleteKeys() {
        seedDataSource.clearSeed()
    }
}