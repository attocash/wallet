package cash.atto.wallet.state

import cash.atto.commons.AttoMnemonic
import cash.atto.commons.toPrivateKey
import cash.atto.commons.toPublicKey
import cash.atto.commons.toSeed

data class AppState(
    val mnemonic: AttoMnemonic?,
    val authState: AUTH_STATE,
    val index: UInt = 0U
) {
    val seed = mnemonic?.toSeed()
    val privateKey = seed?.toPrivateKey(index)
    val publicKey = privateKey?.toPublicKey()

    enum class AUTH_STATE {
        UNKNOWN, LOGGED, UNLOGGED
    }

    companion object {
        val DEFAULT = AppState(
            mnemonic = null,
            authState = AUTH_STATE.UNKNOWN
        )
    }
}