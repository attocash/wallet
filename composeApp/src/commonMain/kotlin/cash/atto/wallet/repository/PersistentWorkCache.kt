package cash.atto.wallet.repository

import cash.atto.commons.AttoInstant
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoWork
import cash.atto.commons.AttoWorkTarget
import cash.atto.commons.isValid
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.Work
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PersistentWorkCache(
    appDatabase: AppDatabase,
) {
    private val dao = appDatabase.workDao()
    private val _version = MutableStateFlow(0L)

    val version: StateFlow<Long> = _version.asStateFlow()

    suspend fun getValid(
        publicKey: AttoPublicKey,
        network: AttoNetwork,
        timestamp: AttoInstant,
        target: AttoWorkTarget,
    ): AttoWork? = getValid(publicKey.value, network, timestamp, target)

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
        dao.set(
            Work(
                publicKey = publicKey.value,
                value = work.value,
            ),
        )
        markChanged()
    }

    suspend fun clear(publicKey: AttoPublicKey) {
        dao.clear(publicKey.value)
        markChanged()
    }

    private suspend fun getValid(
        key: ByteArray,
        network: AttoNetwork,
        timestamp: AttoInstant,
        target: AttoWorkTarget,
    ): AttoWork? =
        dao.get(key)
            ?.let { AttoWork(it.value) }
            ?.takeIf { AttoWork.isValid(network, timestamp, target, it.value) }

    private fun markChanged() {
        _version.value += 1
    }
}
