package cash.atto.wallet.repository

import cash.atto.commons.AttoMnemonic
import cash.atto.commons.AttoPrivateKey
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.toPrivateKey
import cash.atto.commons.toPublicKey
import cash.atto.commons.toSeed
import cash.atto.wallet.state.AppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStateRepository {
    private val _state = MutableStateFlow(AppState.DEFAULT)
    val state = _state.asStateFlow()

    suspend fun generateNewSecret(): List<String> {
        val mnemonic = AttoMnemonic.generate()
        val seed = mnemonic.toSeed()
        val privateKey = seed.toPrivateKey(0U)
        val publicKey = privateKey.toPublicKey()

        setKeys(privateKey, publicKey)
        return mnemonic.words
    }

    private suspend fun setKeys(
        private: AttoPrivateKey,
        public: AttoPublicKey
    ) {
        _state.emit(
            AppState(
                privateKey = private,
                publicKey = public
            )
        )
    }

    suspend fun deleteKeys() {
        _state.emit(AppState(null, null))
    }
}