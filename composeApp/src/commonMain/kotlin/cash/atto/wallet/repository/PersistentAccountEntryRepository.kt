package cash.atto.wallet.repository

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.wallet.AttoAccountEntryRepository
import cash.atto.wallet.datasource.AccountEntry
import cash.atto.wallet.datasource.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PersistentAccountEntryRepository(
    appDatabase: AppDatabase
) : AttoAccountEntryRepository {
    private val dao = appDatabase.accountEntryDao()

    private val flow = MutableSharedFlow<AttoAccountEntry>()

    override suspend fun save(entry: AttoAccountEntry) {
        val json = Json.encodeToString(entry)
        dao.save(AccountEntry(entry.hash.value, entry.publicKey.value, entry.height.value.toLong(), json))
        flow.emit(entry)
    }

    override suspend fun list(publicKey: AttoPublicKey): List<AttoAccountEntry> {
        return dao.list(publicKey.value).map { Json.decodeFromString(it) }
    }

    override suspend fun last(publicKey: AttoPublicKey): AttoAccountEntry? {
        return list(publicKey).lastOrNull()
    }

    fun flow(publicKey: AttoPublicKey): Flow<AttoAccountEntry> {
        return flow.filter { it.publicKey == publicKey }
    }
}