package cash.atto.wallet.repository

import cash.atto.commons.AttoTransaction
import cash.atto.commons.node.AttoNodeClient
import kotlin.time.TimeSource

internal class TimedAttoNodeClient(
    private val delegate: AttoNodeClient,
) : AttoNodeClient by delegate {
    var lastPublishMs: Long? = null
        private set

    fun resetLastPublishMs() {
        lastPublishMs = null
    }

    override suspend fun publish(transaction: AttoTransaction) {
        val started = TimeSource.Monotonic.markNow()
        delegate.publish(transaction)
        lastPublishMs = started.elapsedNow().inWholeMilliseconds
    }
}
