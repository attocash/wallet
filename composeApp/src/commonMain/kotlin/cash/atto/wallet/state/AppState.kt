package cash.atto.wallet.state

import cash.atto.commons.AttoMnemonic
import cash.atto.commons.toPrivateKey
import cash.atto.commons.toPublicKey
import cash.atto.commons.toSeed

data class AppState(
    val encryptedSeed: String?,
    val mnemonic: AttoMnemonic?,
    val authState: AuthState,
    val password: String?,
    val index: UInt = 0U
) {
    suspend fun getSeed() = mnemonic?.toSeed()
    suspend fun getPrivateKey() = getSeed()?.toPrivateKey(index)
    suspend fun getPublicKey() = getPrivateKey()?.toPublicKey()

    enum class AuthState {
        UNKNOWN,
        NEW_ACCOUNT,
        NO_PASSWORD,
        NO_SEED,
        SESSION_INVALID,
        SESSION_VALID;
    }

    companion object {
        val DEFAULT = AppState(
            encryptedSeed = null,
            mnemonic = null,
            authState = AuthState.UNKNOWN,
            password = null
        )
    }
}