package cash.atto.wallet.state

import cash.atto.commons.AttoPrivateKey
import cash.atto.commons.AttoPublicKey

data class AppState(
    val privateKey: AttoPrivateKey?,
    val publicKey: AttoPublicKey?
) {
    companion object {
        val DEFAULT = AppState(null,null)
    }
}