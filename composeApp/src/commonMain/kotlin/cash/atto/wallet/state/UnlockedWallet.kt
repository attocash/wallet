package cash.atto.wallet.state

import cash.atto.commons.AttoHash
import cash.atto.commons.AttoKeyIndex
import cash.atto.commons.AttoMnemonic
import cash.atto.commons.AttoSignature
import cash.atto.commons.AttoSigner
import cash.atto.commons.toSeed
import cash.atto.commons.toSigner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext

class UnlockedWallet internal constructor(
    mnemonic: AttoMnemonic,
    password: String,
    context: CoroutineContext = Dispatchers.Default,
) {
    private class Credentials(
        val mnemonic: AttoMnemonic,
        val password: String,
    )

    private val credentials = MutableStateFlow<Credentials?>(Credentials(mnemonic, password))
    internal val job: Job = SupervisorJob(context[Job])
    private val scope = CoroutineScope(context + job)

    init {
        job.invokeOnCompletion { credentials.value = null }
    }

    val mnemonic: AttoMnemonic? get() = credentials.value?.mnemonic
    val password: String? get() = credentials.value?.password

    internal fun ensureActive() {
        job.ensureActive()
        if (credentials.value == null) throw CancellationException("Wallet is locked")
    }

    internal suspend fun getSeed() =
        run {
            ensureActive()
            val seed = checkNotNull(mnemonic).toSeed()
            ensureActive()
            seed
        }

    internal suspend fun signer(index: AttoKeyIndex): AttoSigner {
        val address = getSeed().toSigner(index).address
        ensureActive()
        return object : AttoSigner {
            override val algorithm = address.algorithm
            override val publicKey = address.publicKey
            override val address = address

            override suspend fun sign(hash: AttoHash): AttoSignature =
                this@UnlockedWallet.run {
                    val signature = getSeed().toSigner(index).sign(hash)
                    ensureActive()
                    signature
                }
        }
    }

    internal suspend fun <T> run(action: suspend CoroutineScope.() -> T): T {
        ensureActive()
        val operation = scope.async(block = action)
        return try {
            operation.await()
        } finally {
            operation.cancel()
        }
    }

    internal fun close() {
        credentials.value = null
        job.cancel()
    }
}
