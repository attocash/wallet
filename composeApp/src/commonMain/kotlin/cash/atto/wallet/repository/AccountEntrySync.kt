package cash.atto.wallet.repository

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.node.monitor.AttoHeightMonitor.Message
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val ACCOUNT_ENTRY_BATCH_SIZE = 100
private val accountEntryFlushInterval = 25.milliseconds

internal suspend fun persistAccountEntryStream(
    entries: Flow<Message<AttoAccountEntry>>,
    retryDelay: Duration,
    save: suspend (List<AttoAccountEntry>) -> Unit,
) = coroutineScope {
    val bufferedEntries = entries.buffer(ACCOUNT_ENTRY_BATCH_SIZE).produceIn(this)
    for (first in bufferedEntries) {
        val batch = ArrayList<Message<AttoAccountEntry>>(ACCOUNT_ENTRY_BATCH_SIZE)
        batch.add(first)
        val flushTimer = async(start = CoroutineStart.UNDISPATCHED) { delay(accountEntryFlushInterval) }
        try {
            while (batch.size < ACCOUNT_ENTRY_BATCH_SIZE) {
                val next =
                    select<Message<AttoAccountEntry>?> {
                        flushTimer.onAwait { null }
                        bufferedEntries.onReceiveCatching { result ->
                            result.exceptionOrNull()?.let { throw it }
                            result.getOrNull()
                        }
                    } ?: break
                batch.add(next)
            }
        } finally {
            flushTimer.cancel()
        }

        val values = batch.map { it.value }
        retryWalletOperation("save ${values.size} account entries", retryDelay) { save(values) }
        // The monitor must resume from a committed entry, including after a failed write or restart.
        batch.forEach { it.acknowledge() }
    }
}
