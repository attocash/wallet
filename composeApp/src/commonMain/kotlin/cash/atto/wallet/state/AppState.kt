package cash.atto.wallet.state

import cash.atto.commons.AttoMnemonic
import cash.atto.commons.toPrivateKey
import cash.atto.commons.toPublicKey
import cash.atto.commons.toSeed

data class AppState(
    val mnemonic: AttoMnemonic?,
    val index: UInt = 0U
) {
    val seed = mnemonic?.toSeed()
    val privateKey = seed?.toPrivateKey(index)
    val publicKey = privateKey?.toPublicKey()


    companion object {
        val DEFAULT = AppState(null)
//        val DEFAULT = AppState(AttoMnemonic("ring mask spirit scissors best differ mean pet print century loyal major brain path already version jaguar rescue elder slender anxiety behind leg pigeon"))
    }
}