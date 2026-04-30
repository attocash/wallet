package cash.atto.wallet.repository

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoBlock
import cash.atto.commons.AttoInstant
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoWork
import cash.atto.commons.AttoWorkTarget
import cash.atto.commons.isValid
import cash.atto.commons.worker.AttoWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class PersistentWorkCachingWorker(
    private val network: AttoNetwork,
    private val workCache: PersistentWorkCache,
    private val delegate: AttoWorker,
) : AttoWorker {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override suspend fun work(
        threshold: ULong,
        target: AttoWorkTarget,
    ): AttoWork {
        cachedWork(threshold, target)?.let {
            workCache.clear()
            return it
        }

        workCache.clear()
        return delegate.work(threshold, target).also {
            workCache.save(it)
        }
    }

    override suspend fun work(
        network: AttoNetwork,
        timestamp: AttoInstant,
        target: AttoWorkTarget,
    ): AttoWork {
        cachedWork(network, timestamp, target)?.let {
            workCache.clear()
            return it
        }

        workCache.clear()
        return delegate.work(network, timestamp, target).also {
            workCache.save(it)
        }
    }

    override suspend fun work(block: AttoBlock): AttoWork {
        val cachedWork = workCache.get()
        val work =
            if (cachedWork?.isValid(block) == true) {
                workCache.clear()
                cachedWork
            } else {
                workCache.clear()
                delegate.work(block)
            }

        cacheNextWork(block)
        return work
    }

    override fun close() {
        scope.cancel()
        delegate.close()
    }

    suspend fun cacheNextWork(account: AttoAccount) {
        cacheWork(
            target = AttoWorkTarget(account.lastTransactionHash.value),
            description = account.lastTransactionHash.toString(),
        )
    }

    private suspend fun cachedWork(
        threshold: ULong,
        target: AttoWorkTarget,
    ): AttoWork? =
        workCache
            .get()
            ?.takeIf { AttoWork.isValid(threshold, target, it.value) }

    private suspend fun cachedWork(
        network: AttoNetwork,
        timestamp: AttoInstant,
        target: AttoWorkTarget,
    ): AttoWork? =
        workCache
            .get()
            ?.takeIf { AttoWork.isValid(network, timestamp, target, it.value) }

    private suspend fun cacheNextWork(block: AttoBlock) {
        cacheWork(
            target = AttoWorkTarget(block.hash.value),
            description = block.hash.toString(),
        )
    }

    private suspend fun cacheWork(
        target: AttoWorkTarget,
        description: String,
    ) {
        val timestamp = AttoInstant.now()
        if (cachedWork(network, timestamp, target) != null) {
            return
        }

        workCache.clear()
        scope.launch {
            try {
                val work = delegate.work(network, timestamp, target)
                workCache.save(work)
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                println("Failed to cache work for $description: ${ex.message}")
            }
        }
    }
}
