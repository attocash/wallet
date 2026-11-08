package cash.atto.wallet.repository

import cash.atto.commons.AttoInstant
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoWork
import cash.atto.commons.AttoWorkTarget
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.Work
import cash.atto.wallet.datasource.WorkDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PersistentWorkCache internal constructor(
    private val dao: WorkDao,
) {
    constructor(appDatabase: AppDatabase) : this(appDatabase.workDao())

    private val mutex = Mutex()
    private val memoryCache = mutableMapOf<AttoPublicKey, AttoWork>()
    private val _version = MutableStateFlow(0L)

    val version: StateFlow<Long> = _version.asStateFlow()

    suspend fun getValid(
        publicKey: AttoPublicKey,
        network: AttoNetwork,
        timestamp: AttoInstant,
        target: AttoWorkTarget,
    ): AttoWork? =
        mutex.withLock {
            val cached = memoryCache[publicKey]
            if (cached != null) {
                if (AttoWork.isValid(network, timestamp, target, cached.value)) {
                    return@withLock cached
                }
                memoryCache.remove(publicKey)
            }

            val stored =
                dao
                    .get(publicKey.value)
                    ?.let { AttoWork(it.value) }
                    ?.takeIf { AttoWork.isValid(network, timestamp, target, it.value) }

            if (stored != null) {
                memoryCache[publicKey] = stored
            }
            stored
        }

    suspend fun hasValid(
        publicKey: AttoPublicKey,
        network: AttoNetwork,
        timestamp: AttoInstant,
        target: AttoWorkTarget,
    ): Boolean = getValid(publicKey, network, timestamp, target) != null

    suspend fun save(
        publicKey: AttoPublicKey,
        work: AttoWork,
    ) {
        mutex.withLock {
            dao.set(
                Work(
                    publicKey = publicKey.value,
                    value = work.value,
                ),
            )
            memoryCache[publicKey] = work
            markChanged()
        }
    }

    suspend fun clear(publicKey: AttoPublicKey) {
        mutex.withLock {
            dao.clear(publicKey.value)
            memoryCache.remove(publicKey)
            markChanged()
        }
    }

    private fun markChanged() {
        _version.value += 1
    }
}
