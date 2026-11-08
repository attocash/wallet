package cash.atto.wallet.repository

import cash.atto.wallet.datasource.AccountEntry
import cash.atto.wallet.datasource.AccountEntryDao

internal class AccountEntryTestDao : AccountEntryDao {
    private val entries = mutableListOf<AccountEntry>()
    var beforeSave: suspend () -> Unit = {}
    var afterSave: suspend () -> Unit = {}
    var saveAttempts = 0
    val pageRequests = mutableListOf<Pair<Long?, Int>>()

    override suspend fun save(entry: AccountEntry) {
        saveAttempts++
        beforeSave()
        entries.removeAll { it.hash.contentEquals(entry.hash) }
        entries.add(entry)
        afterSave()
    }

    override suspend fun last(publicKey: ByteArray): String? = matching(publicKey).firstOrNull()?.entry

    override suspend fun listBefore(
        publicKey: ByteArray,
        beforeHeightExclusive: Long,
        limit: Int,
    ): List<String> {
        pageRequests.add(beforeHeightExclusive to limit)
        return matching(publicKey).filter { it.height < beforeHeightExclusive }.take(limit).map { it.entry }
    }

    override suspend fun listRecent(
        publicKey: ByteArray,
        limit: Int,
    ): List<String> {
        pageRequests.add(null to limit)
        return matching(publicKey).take(limit).map { it.entry }
    }

    override suspend fun summaryRows(publicKey: ByteArray): List<AccountEntryDao.SummaryRow> =
        matching(publicKey).map {
            AccountEntryDao.SummaryRow(it.height, it.blockType, it.balanceRaw, it.previousBalanceRaw)
        }

    private fun matching(publicKey: ByteArray): List<AccountEntry> =
        entries.filter { it.publicKey.contentEquals(publicKey) }.sortedByDescending { it.height }
}
