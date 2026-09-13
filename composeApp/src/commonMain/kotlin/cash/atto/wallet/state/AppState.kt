package cash.atto.wallet.state

import cash.atto.commons.toPrivateKey
import cash.atto.commons.toPublicKey

data class AppState(
    val authState: AuthState,
    val unlockedWallet: UnlockedWallet? = null,
    val index: UInt = 0U,
) {
    val mnemonic get() = authenticatedWallet()?.mnemonic
    val password get() = authenticatedWallet()?.password

    private fun authenticatedWallet() = unlockedWallet.takeIf { authState == AuthState.SESSION_VALID }

    suspend fun getSeed() = authenticatedWallet()?.takeIf { it.mnemonic != null }?.getSeed()

    suspend fun getPrivateKey() = getSeed()?.toPrivateKey(index)

    suspend fun getPublicKey() = getPrivateKey()?.toPublicKey()

    enum class AuthState {
        UNKNOWN,
        NEW_ACCOUNT,
        NO_PASSWORD,
        NO_SEED,
        SESSION_INVALID,
        SESSION_VALID,
    }

    companion object {
        val DEFAULT =
            AppState(
                authState = AuthState.UNKNOWN,
            )
    }
}
