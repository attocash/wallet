package cash.atto.wallet.repository

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoBlockType
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.wallet.AttoAccountEntryRepository
import cash.atto.wallet.datasource.AccountEntry
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.model.TransactionHistorySummary
import cash.atto.wallet.platform.appendTransactionsCsvRows
import cash.atto.wallet.platform.writeTransactionsCsvHeader
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.buildTransactionListUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.io.Sink
import kotlinx.serialization.json.Json

class PersistentAccountEntryRepository(
    appDatabase: AppDatabase,
) : AttoAccountEntryRepository {
    private val dao = appDatabase.accountEntryDao()

    private val flow = MutableSharedFlow<AttoAccountEntry>()
    private val json = Json

    private val writeChannel = Channel<AccountEntry>(capacity = Channel.UNLIMITED)

    init {
        CoroutineScope(Dispatchers.Default).launch {
            val batch = mutableListOf<AccountEntry>()
            for (accountEntry in writeChannel) {
                batch.add(accountEntry)
                // Drain all currently buffered entries
                while (true) {
                    val next = writeChannel.tryReceive().getOrNull() ?: break
                    batch.add(next)
                }
                try {
                    dao.saveAll(batch)
                } catch (e: Exception) {
                    println("Failed to batch-save ${batch.size} entries: ${e.message}")
                }
                batch.clear()
            }
        }
    }

    override suspend fun save(entry: AttoAccountEntry) {
        // Emit to the in-memory flow first so the UI updates instantly.
        flow.emit(entry)

        // Queue the DB write for background batched persistence.
        val jsonStr = json.encodeToString(entry)
        writeChannel.send(
            AccountEntry(
                hash = entry.hash.value,
                publicKey = entry.publicKey.value,
                height = entry.height.value.toLong(),
                entry = jsonStr,
                blockType = entry.blockType.name,
                balanceRaw = entry.balance.raw.toString(),
                previousBalanceRaw = entry.previousBalance.raw.toString(),
            ),
        )
    }

    override suspend fun stream(publicKey: AttoPublicKey): Flow<AttoAccountEntry> {
        val lastEntryFlow =
            flow {
                last(publicKey)?.let { emit(it) }
            }

        return merge(lastEntryFlow, flow(publicKey))
    }

    override suspend fun last(publicKey: AttoPublicKey): AttoAccountEntry? =
        dao.last(publicKey.value)?.let { json.decodeFromString<AttoAccountEntry>(it) }

    fun flow(publicKey: AttoPublicKey): Flow<AttoAccountEntry> = flow.filter { it.publicKey == publicKey }

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

    suspend fun summary(publicKey: AttoPublicKey): TransactionHistorySummary {
        val rows = dao.summaryRows(publicKey.value)
        return rows.fold(TransactionHistorySummary()) { acc, row ->
            val blockType =
                try {
                    AttoBlockType.valueOf(row.blockType)
                } catch (_: Exception) {
                    return@fold acc
                }
            val balance = AttoAmount(row.balanceRaw.toULong())
            val previousBalance = AttoAmount(row.previousBalanceRaw.toULong())
            when (blockType) {
                AttoBlockType.SEND -> {
                    acc.copy(
                        totalTransactions = acc.totalTransactions + 1,
                        totalSent = acc.totalSent + (previousBalance - balance),
                    )
                }

                AttoBlockType.RECEIVE, AttoBlockType.OPEN -> {
                    acc.copy(
                        totalTransactions = acc.totalTransactions + 1,
                        totalReceived = acc.totalReceived + (balance - previousBalance),
                    )
                }

                AttoBlockType.CHANGE -> {
                    acc.copy(totalTransactions = acc.totalTransactions + 1)
                }

                else -> {
                    acc
                }
            }
        }
    }

    suspend fun exportCsv(
        publicKey: AttoPublicKey,
        selectedTypes: Set<TransactionType>,
        addressLabelResolver: (String) -> String? = { null },
        voterLabelResolver: (String) -> String? = { null },
        hashLabelResolver: (String) -> String? = { null },
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
                    hashLabelResolver = hashLabelResolver,
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

    private companion object {
        private const val EXPORT_PAGE_SIZE = 500
    }
}
