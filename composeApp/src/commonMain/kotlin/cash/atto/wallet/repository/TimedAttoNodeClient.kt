package cash.atto.wallet.repository

import cash.atto.commons.AttoTransaction
import cash.atto.commons.node.AttoNodeClient
import cash.atto.wallet.state.UnlockedWallet
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.time.TimeSource

internal class TimedAttoNodeClient(
    private val delegate: AttoNodeClient,
    private val unlockedWallet: UnlockedWallet,
) : AttoNodeClient by delegate {
    var lastPublishMs: Long? = null
        private set

    fun resetLastPublishMs() {
        lastPublishMs = null
    }

    override suspend fun publish(transaction: AttoTransaction) {
        unlockedWallet.ensureActive()
        currentCoroutineContext().ensureActive()
        val started = TimeSource.Monotonic.markNow()
        delegate.publish(transaction)
        lastPublishMs = started.elapsedNow().inWholeMilliseconds
    }
}
