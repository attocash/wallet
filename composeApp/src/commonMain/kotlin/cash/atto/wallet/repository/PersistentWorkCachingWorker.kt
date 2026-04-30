package cash.atto.wallet.repository

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoBlock
import cash.atto.commons.AttoInstant
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoReceivable
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class PersistentWorkCachingWorker(
    private val network: AttoNetwork,
    private val workCache: PersistentWorkCache,
    private val delegate: AttoWorker,
) : AttoWorker {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val inFlightMutex = Mutex()
    private val inFlightTargets = mutableMapOf<AttoPublicKey, AttoWorkTarget>()

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
        val cachedWork = workCache.get(block.publicKey)
        val work =
            if (cachedWork?.isValid(block) == true) {
                workCache.clear(block.publicKey)
                cachedWork
            } else {
                workCache.clear(block.publicKey)
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
            publicKey = account.publicKey,
            target = AttoWorkTarget(account.lastTransactionHash.value),
            description = account.lastTransactionHash.toString(),
        )
    }

    suspend fun prepareReceiveWork(
        receivable: AttoReceivable,
        account: AttoAccount?,
    ): Boolean {
        val target =
            AttoWorkTarget(
                account
                    ?.lastTransactionHash
                    ?.value
                    ?: receivable.receiverPublicKey.value,
            )

        cacheWork(
            publicKey = receivable.receiverPublicKey,
            target = target,
            description = receivable.hash.toString(),
        )

        return cachedWork(
            publicKey = receivable.receiverPublicKey,
            network = network,
            timestamp = AttoInstant.now(),
            target = target,
        ) != null
    }

    private suspend fun cachedWork(
        publicKey: AttoPublicKey,
        network: AttoNetwork,
        timestamp: AttoInstant,
        target: AttoWorkTarget,
    ): AttoWork? = workCache.getValid(publicKey, network, timestamp, target)

    private suspend fun cacheNextWork(block: AttoBlock) {
        cacheWork(
            publicKey = block.publicKey,
            target = AttoWorkTarget(block.hash.value),
            description = block.hash.toString(),
        )
    }

    private suspend fun cacheWork(
        publicKey: AttoPublicKey,
        target: AttoWorkTarget,
        description: String,
    ) {
        val timestamp = AttoInstant.now()
        if (cachedWork(publicKey, network, timestamp, target) != null) {
            return
        }

        val shouldLaunch =
            inFlightMutex.withLock {
                if (inFlightTargets[publicKey] == target) {
                    false
                } else {
                    inFlightTargets[publicKey] = target
                    true
                }
            }
        if (!shouldLaunch) {
            return
        }

        workCache.clear(publicKey)
        scope.launch {
            try {
                val work = delegate.work(network, timestamp, target)
                val isLatestTarget =
                    inFlightMutex.withLock {
                        inFlightTargets[publicKey] == target
                    }
                if (isLatestTarget) {
                    workCache.save(publicKey, work)
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                println("Failed to cache work for $description: ${ex.message}")
            } finally {
                inFlightMutex.withLock {
                    if (inFlightTargets[publicKey] == target) {
                        inFlightTargets.remove(publicKey)
                    }
                }
            }
        }
    }
}
