package cash.atto.wallet.repository

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoBlock
import cash.atto.commons.AttoInstant
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoWork
import cash.atto.commons.AttoWorkTarget
import cash.atto.commons.getTarget
import cash.atto.commons.worker.AttoWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

internal class PersistentWorkCachingWorker(
    private val network: AttoNetwork,
    private val workCache: PersistentWorkCache,
    private val delegate: AttoWorker,
    context: CoroutineContext = Dispatchers.Default,
) : AttoWorker {
    private class WorkRequest(
        val target: AttoWorkTarget,
    )

    private val scope = CoroutineScope(context + SupervisorJob(context[Job]))
    private val inFlightMutex = Mutex()
    private val inFlightRequests = mutableMapOf<AttoPublicKey, WorkRequest>()

    override suspend fun work(
        threshold: ULong,
        target: AttoWorkTarget,
    ): AttoWork = delegate.work(threshold, target)

    override suspend fun work(
        network: AttoNetwork,
        timestamp: AttoInstant,
        target: AttoWorkTarget,
    ): AttoWork = delegate.work(network, timestamp, target)

    override suspend fun work(block: AttoBlock): AttoWork {
        val target = block.getTarget()
        val cachedWork =
            workCache.getValid(
                publicKey = block.publicKey,
                network = block.network,
                timestamp = block.timestamp,
                target = target,
            )
        workCache.clear(block.publicKey)
        val work = cachedWork ?: delegate.work(block)

        cacheNextWork(block)
        return work
    }

    override fun close() {
        scope.cancel()
        delegate.close()
    }

    fun cacheNextWork(
        publicKey: AttoPublicKey,
        account: AttoAccount?,
    ) {
        val target = nextWorkTarget(account = account, publicKey = publicKey)
        cacheWork(
            publicKey = publicKey,
            target = target,
            description = target.toString(),
        )
    }

    private fun cacheNextWork(block: AttoBlock) {
        cacheWork(
            publicKey = block.publicKey,
            target = AttoWorkTarget(block.hash.value),
            description = block.hash.toString(),
        )
    }

    private fun cacheWork(
        publicKey: AttoPublicKey,
        target: AttoWorkTarget,
        description: String,
    ) {
        scope.launch {
            val timestamp = AttoInstant.now()
            if (workCache.hasValid(publicKey, network, timestamp, target)) {
                return@launch
            }

            val request =
                inFlightMutex.withLock {
                    if (inFlightRequests[publicKey]?.target == target) {
                        null
                    } else {
                        WorkRequest(target).also { inFlightRequests[publicKey] = it }
                    }
                } ?: return@launch

            try {
                val work = delegate.work(network, timestamp, target)
                inFlightMutex.withLock {
                    if (inFlightRequests[publicKey] === request) {
                        workCache.save(
                            publicKey = publicKey,
                            work = work,
                        )
                    }
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                println("Failed to cache work for $description: ${ex.message}")
            } finally {
                inFlightMutex.withLock {
                    if (inFlightRequests[publicKey] === request) {
                        inFlightRequests.remove(publicKey)
                    }
                }
            }
        }
    }
}
