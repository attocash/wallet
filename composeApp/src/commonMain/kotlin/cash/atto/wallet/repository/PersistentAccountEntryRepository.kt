package cash.atto.wallet.repository

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.wallet.AttoAccountEntryRepository
import cash.atto.wallet.datasource.AccountEntry
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.model.AccountEntryHistorySnapshot
import cash.atto.wallet.model.TransactionHistorySummary
import cash.atto.wallet.model.mergeAccountEntries
import cash.atto.wallet.model.toTransactionHistorySummary
import cash.atto.wallet.platform.appendTransactionsCsvRows
import cash.atto.wallet.platform.writeTransactionsCsvHeader
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.buildTransactionListUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.io.Sink
import kotlinx.serialization.json.Json

class PersistentAccountEntryRepository(
    appDatabase: AppDatabase,
) : AttoAccountEntryRepository {
    private val dao = appDatabase.accountEntryDao()

    private val flow = MutableSharedFlow<AttoAccountEntry>()
    private val json = Json
    private var cachedHistory: CachedHistory? = null

    override suspend fun save(entry: AttoAccountEntry) {
        val json = json.encodeToString(entry)
        dao.save(
            AccountEntry(
                hash = entry.hash.value,
                publicKey = entry.publicKey.value,
                height = entry.height.value.toLong(),
                entry = json,
            ),
        )
        cachedHistory =
            cachedHistory?.takeIf { it.matches(entry.publicKey) }?.withEntry(entry)
        flow.emit(entry)
    }

    override suspend fun stream(publicKey: AttoPublicKey): Flow<AttoAccountEntry> {
        val cachedFlow =
            flow {
                emitAll(historySnapshot(publicKey).entries.asFlow())
            }

        return merge(cachedFlow, flow(publicKey))
    }

    override suspend fun last(publicKey: AttoPublicKey): AttoAccountEntry? =
        dao.last(publicKey.value)?.let { json.decodeFromString<AttoAccountEntry>(it) }

    fun flow(publicKey: AttoPublicKey): Flow<AttoAccountEntry> = flow.filter { it.publicKey == publicKey }

    suspend fun list(publicKey: AttoPublicKey): List<AttoAccountEntry> = historySnapshot(publicKey).entries

    suspend fun listBefore(
        publicKey: AttoPublicKey,
        beforeHeightExclusive: Long,
        limit: Int,
    ): List<AttoAccountEntry> =
        decodeEntries(
            dao.listBefore(publicKey.value, beforeHeightExclusive, limit),
        )

    suspend fun listRecent(
        publicKey: AttoPublicKey,
        limit: Int,
    ): List<AttoAccountEntry> =
        decodeEntries(
            dao.listRecent(publicKey.value, limit),
        )

    suspend fun historySnapshot(publicKey: AttoPublicKey): AccountEntryHistorySnapshot {
        val cached = cachedHistory
        if (cached != null && cached.matches(publicKey)) {
            return cached.snapshot
        }

        val snapshot =
            buildHistorySnapshot(
                decodeEntries(
                    dao.list(publicKey.value),
                ),
            )
        cachedHistory = CachedHistory(publicKey.value.copyOf(), snapshot)
        return snapshot
    }

    suspend fun summary(publicKey: AttoPublicKey): TransactionHistorySummary = historySnapshot(publicKey).summary

    suspend fun exportCsv(
        publicKey: AttoPublicKey,
        selectedTypes: Set<TransactionType>,
        addressLabelResolver: (String) -> String? = { null },
        voterLabelResolver: (String) -> String? = { null },
        sink: Sink,
    ) {
        writeTransactionsCsvHeader(sink)

        var beforeHeightExclusive: Long? = null

        while (true) {
            val page =
                if (beforeHeightExclusive == null) {
                    listRecent(publicKey, EXPORT_PAGE_SIZE)
                } else {
                    listBefore(
                        publicKey = publicKey,
                        beforeHeightExclusive = beforeHeightExclusive,
                        limit = EXPORT_PAGE_SIZE,
                    )
                }

            if (page.isEmpty()) {
                sink.flush()
                return
            }

            val transactions =
                buildTransactionListUiState(
                    entries = page,
                    addressLabelResolver = addressLabelResolver,
                    voterLabelResolver = voterLabelResolver,
                ).transactions
                    .filterNotNull()
                    .filter { it.type in selectedTypes }

            appendTransactionsCsvRows(sink, transactions)

            if (page.size < EXPORT_PAGE_SIZE) {
                sink.flush()
                return
            }

            beforeHeightExclusive =
                page
                    .lastOrNull()
                    ?.height
                    ?.value
                    ?.toLong()
        }
    }

    private fun decodeEntries(entries: List<String>): List<AttoAccountEntry> = entries.map { json.decodeFromString<AttoAccountEntry>(it) }

    private fun buildHistorySnapshot(entries: List<AttoAccountEntry>): AccountEntryHistorySnapshot =
        AccountEntryHistorySnapshot(
            entries = entries,
            summary =
                entries.fold(TransactionHistorySummary()) { summary, entry ->
                    summary + entry.toTransactionHistorySummary()
                },
        )

    private data class CachedHistory(
        val publicKey: ByteArray,
        val snapshot: AccountEntryHistorySnapshot,
    ) {
        fun matches(publicKey: AttoPublicKey): Boolean = this.publicKey.contentEquals(publicKey.value)

        fun withEntry(entry: AttoAccountEntry): CachedHistory {
            val mergedEntries = mergeAccountEntries(snapshot.entries, entry)
            if (mergedEntries.size == snapshot.entries.size) {
                return this
            }

            return copy(
                snapshot =
                    snapshot.copy(
                        entries = mergedEntries,
                        summary = snapshot.summary + entry.toTransactionHistorySummary(),
                    ),
            )
        }
    }

    private companion object {
        private const val EXPORT_PAGE_SIZE = 500
    }
}
