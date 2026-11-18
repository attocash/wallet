package cash.atto.wallet.repository

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoBlockType
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.wallet.AttoAccountEntryRepository
import cash.atto.wallet.datasource.AccountEntry
import cash.atto.wallet.datasource.AccountEntryDao
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.model.TransactionHistorySnapshot
import cash.atto.wallet.model.TransactionHistorySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.json.Json

class PersistentAccountEntryRepository internal constructor(
    private val dao: AccountEntryDao,
) : AttoAccountEntryRepository {
    constructor(appDatabase: AppDatabase) : this(appDatabase.accountEntryDao())

    private val committedBatches = MutableSharedFlow<List<AttoAccountEntry>>()
    private val json = Json

    override suspend fun save(entry: AttoAccountEntry) = saveAll(listOf(entry))

    internal suspend fun saveAll(entries: List<AttoAccountEntry>) {
        val rows =
            entries.map { entry ->
                AccountEntry(
                    hash = entry.hash.value,
                    publicKey = entry.publicKey.value,
                    height = entry.height.value.toLong(),
                    entry = json.encodeToString(entry),
                    blockType = entry.blockType.name,
                    balanceRaw = entry.balance.raw.toString(),
                    previousBalanceRaw = entry.previousBalance.raw.toString(),
                )
            }
        dao.saveAll(rows)
        // Hand off the whole commit so the writer need not wait for each observer update.
        committedBatches.emit(entries)
    }

    override suspend fun stream(address: AttoAddress): Flow<AttoAccountEntry> {
        val lastEntryFlow =
            flow {
                last(address)?.let { emit(it) }
            }

        return merge(lastEntryFlow, flow(address.publicKey))
    }

    override suspend fun last(address: AttoAddress): AttoAccountEntry? = last(address.publicKey)

    suspend fun last(publicKey: AttoPublicKey): AttoAccountEntry? =
        dao.last(publicKey.value)?.let { json.decodeFromString<AttoAccountEntry>(it) }

    fun flow(publicKey: AttoPublicKey): Flow<AttoAccountEntry> =
        committedBatches.transform { entries ->
            for (entry in entries) {
                if (entry.publicKey == publicKey) emit(entry)
            }
        }

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

    suspend fun summary(publicKey: AttoPublicKey): TransactionHistorySnapshot {
        val rows = dao.summaryRows(publicKey.value)
        val summary =
            rows.fold(TransactionHistorySummary()) { acc, row ->
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
        return TransactionHistorySnapshot(
            summary = summary,
            throughHeight = rows.maxOfOrNull { it.height.toULong() } ?: 0UL,
        )
    }

    private fun decodeEntries(entries: List<String>): List<AttoAccountEntry> = entries.map { json.decodeFromString<AttoAccountEntry>(it) }
}
