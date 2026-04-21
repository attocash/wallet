package cash.atto.wallet.model

import cash.atto.commons.AttoAccountEntry

data class TransactionsHistoryState(
    val loadedEntries: List<AttoAccountEntry> = emptyList(),
    val nextBeforeHeightExclusive: Long? = null,
    val hasMore: Boolean = true,
    val fullHistorySummary: TransactionHistorySummary = TransactionHistorySummary(),
    val isSummaryLoading: Boolean = true,
) {
    fun withInitialPage(
        page: List<AttoAccountEntry>,
        pageSize: Int,
    ): TransactionsHistoryState =
        copy(
            loadedEntries = mergeAccountEntries(emptyList(), page),
            nextBeforeHeightExclusive =
                page
                    .lastOrNull()
                    ?.height
                    ?.value
                    ?.toLong(),
            hasMore = page.size == pageSize,
        )

    fun withOlderPage(
        page: List<AttoAccountEntry>,
        pageSize: Int,
    ): TransactionsHistoryState =
        copy(
            loadedEntries = mergeAccountEntries(loadedEntries, page),
            nextBeforeHeightExclusive =
                page
                    .lastOrNull()
                    ?.height
                    ?.value
                    ?.toLong(),
            hasMore = page.size == pageSize,
        )

    fun withLiveEntry(entry: AttoAccountEntry): TransactionsHistoryState {
        val mergedEntries = mergeAccountEntries(loadedEntries, entry)
        val isNewEntry = mergedEntries.size != loadedEntries.size

        return copy(
            loadedEntries = mergedEntries,
            fullHistorySummary =
                if (!isSummaryLoading && isNewEntry) {
                    fullHistorySummary + entry.toTransactionHistorySummary()
                } else {
                    fullHistorySummary
                },
        )
    }

    fun withSummary(summary: TransactionHistorySummary): TransactionsHistoryState =
        copy(
            fullHistorySummary = summary,
            isSummaryLoading = false,
        )
}
