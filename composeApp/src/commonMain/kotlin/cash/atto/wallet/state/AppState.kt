package cash.atto.wallet.state

import cash.atto.commons.AttoMnemonic
import cash.atto.commons.toPrivateKey
import cash.atto.commons.toPublicKey
import cash.atto.commons.toSeed

data class AppState(
    val mnemonic: AttoMnemonic?,
    val authState: AuthState,
    val password: String?,
    val index: UInt = 0U,
) {
    val seed = mnemonic?.toSeed()
    val privateKey = seed?.toPrivateKey(index)
    val publicKey = privateKey?.toPublicKey()

    enum class AuthState {
        UNKNOWN,
        NO_PASSWORD,
        NO_SEED,
        SESSION_INVALID,
        SESSION_VALID;
    }

    companion object {
        val DEFAULT = AppState(
            mnemonic = null,
            authState = AuthState.UNKNOWN,
            password = null
        )
    }
}