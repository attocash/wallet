package cash.atto.wallet.repository

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.wallet.AttoAccountEntryRepository
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.createAccountEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.serialization.json.Json

class PersistentAccountEntryRepository(
    appDatabase: AppDatabase
) : AttoAccountEntryRepository {
    private val dao = appDatabase.accountEntryDao()

    private val flow = MutableSharedFlow<AttoAccountEntry>()

    override suspend fun save(entry: AttoAccountEntry) {
        val json = Json.encodeToString(entry)
        dao.save(
            createAccountEntry(
                entry.hash.value,
                entry.publicKey.value,
                entry.height.value.toLong(),
                json
            )
        )
        flow.emit(entry)
    }

    override suspend fun stream(publicKey: AttoPublicKey): Flow<AttoAccountEntry> {
        val entries: List<AttoAccountEntry> =
            dao.list(publicKey.value).map { Json.decodeFromString(it) }
        return entries.asFlow()
    }

    override suspend fun last(publicKey: AttoPublicKey): AttoAccountEntry? {
        return stream(publicKey).lastOrNull()
    }

    fun flow(publicKey: AttoPublicKey): Flow<AttoAccountEntry> {
        return flow.filter { it.publicKey == publicKey }
    }
}