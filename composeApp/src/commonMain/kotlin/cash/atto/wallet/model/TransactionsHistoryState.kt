package cash.atto.wallet.model

import cash.atto.commons.AttoAccountEntry

data class TransactionsHistoryState(
    val loadedEntries: List<AttoAccountEntry> = emptyList(),
    val nextBeforeHeightExclusive: Long? = null,
    val hasMore: Boolean = true,
    val fullHistorySummary: TransactionHistorySummary = TransactionHistorySummary(),
    val isSummaryLoading: Boolean = true,
    private val summaryThroughHeight: ULong = 0UL,
) {
    fun withInitialPage(
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
                if (!isSummaryLoading && isNewEntry && entry.height.value > summaryThroughHeight) {
                    fullHistorySummary + entry.toTransactionHistorySummary()
                } else {
                    fullHistorySummary
                },
        )
    }

    // Notifications can arrive before or after this snapshot finishes; count only entries beyond its fixed watermark.
    fun withSummary(snapshot: TransactionHistorySnapshot): TransactionsHistoryState =
        copy(
            fullHistorySummary =
                loadedEntries
                    .filter { it.height.value > snapshot.throughHeight }
                    .fold(snapshot.summary) { summary, entry -> summary + entry.toTransactionHistorySummary() },
            isSummaryLoading = false,
            summaryThroughHeight = snapshot.throughHeight,
        )
}
