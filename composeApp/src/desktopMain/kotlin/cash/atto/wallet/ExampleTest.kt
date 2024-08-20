package cash.atto.wallet

import cash.atto.commons.AttoMnemonic
import cash.atto.commons.toHex
import cash.atto.commons.toPrivateKey
import cash.atto.commons.toSeed
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() {
    val mnemonic = AttoMnemonic.generate()
    println("Created mnemonic: ${mnemonic.words}")

    val seed = mnemonic.toSeed()
    println("Created seed: ${seed.value.toHex()}")

    val privateKey = seed.toPrivateKey(0U)
    println("Created privateKey: ${privateKey.value.toHex()}")
//    val privateKey = AttoPrivateKey("".fromHexToByteArray())

    val publicKey = privateKey.toPublicKey()
    println("Created publicKey: ${publicKey.value.toHex()}")

    val signer = Signer(privateKey)

//    val walletGatekeeperUrl = "https://wallet-gatekeeper.dev.application.atto.cash"
//    val gatekeeperUrl = "https://gatekeeper.dev.application.atto.cash"
    val walletGatekeeperUrl = "http://localhost:8083"
//    val walletGatekeeperUrl = "http://localhost:9090"
    val gatekeeperUrl = "http://localhost:8082"

    val authenticator =
        Authenticator(walletGatekeeperUrl, signer)
    val workerClient = WorkerClient(gatekeeperUrl)

    val accountManager = AccountManager(
        endpoint = gatekeeperUrl,
        signer = signer,
        authenticator = authenticator,
        representative = publicKey,
        workerClient = workerClient,
        autoReceive = true,
    )

    runBlocking {
        accountManager.start()
        delay(Long.MAX_VALUE)
    }
}