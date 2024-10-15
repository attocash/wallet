package cash.atto.wallet.repository

import cash.atto.commons.AttoMnemonic
import cash.atto.wallet.state.AppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStateRepository {
    private val _state = MutableStateFlow(AppState.DEFAULT)
    val state = _state.asStateFlow()

    suspend fun generateNewSecret(): List<String> {
        val mnemonic = AttoMnemonic.generate()
        setKeys(mnemonic)
        return mnemonic.words
    }

    private suspend fun setKeys(
        mnemonic: AttoMnemonic,
    ) {
        _state.emit(
            AppState(
                mnemonic = mnemonic,
            )
        )
    }

    suspend fun deleteKeys() {
        _state.emit(AppState(null))
    }
}