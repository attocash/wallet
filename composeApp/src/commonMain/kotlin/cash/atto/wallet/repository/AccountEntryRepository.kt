package cash.atto.wallet.repository

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.wallet.AttoAccountEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AccountEntryRepository : AttoAccountEntryRepository {
    private val mutex = Mutex()
    private val entryMap = mutableMapOf<AttoPublicKey, MutableList<AttoAccountEntry>>()
    private val flow = MutableSharedFlow<AttoAccountEntry>()

    override suspend fun save(entry: AttoAccountEntry) {
        val publicKey = entry.publicKey
        mutex.withLock {
            val entries = entryMap[publicKey] ?: mutableListOf()
            entries.add(entry)
            entryMap[publicKey] = entries
            flow.emit(entry)
        }
    }

    override suspend fun list(publicKey: AttoPublicKey): List<AttoAccountEntry> {
        mutex.withLock {
            return entryMap[publicKey]?.toList() ?: emptyList()
        }
    }

    override suspend fun last(publicKey: AttoPublicKey): AttoAccountEntry? {
        return list(publicKey).lastOrNull()
    }

    fun flow(publicKey: AttoPublicKey): Flow<AttoAccountEntry> {
        return flow.filter { it.publicKey == publicKey }
    }

    suspend fun clear() {
        mutex.withLock {
            entryMap.clear()
        }
    }
}