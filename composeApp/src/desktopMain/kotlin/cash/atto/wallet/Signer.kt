package cash.atto.wallet

import cash.atto.commons.AttoHash
import cash.atto.commons.AttoPrivateKey
import cash.atto.commons.AttoSignature
import cash.atto.commons.sign

class Signer(private val privateKey: AttoPrivateKey) {
    val publicKey = privateKey.toPublicKey()
    fun sign(hash: AttoHash): AttoSignature {
        return privateKey.sign(hash)
    }
}