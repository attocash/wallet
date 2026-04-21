package cash.atto.wallet.screens

internal fun shouldLoadMoreTransactions(
    hasMore: Boolean,
    isLoadingMore: Boolean,
    loadedTransactionsCount: Int,
    lastVisibleItemIndex: Int?,
    totalItemsCount: Int,
): Boolean {
    if (!hasMore || isLoadingMore || loadedTransactionsCount == 0) {
        return false
    }

    val lastVisibleIndex = lastVisibleItemIndex ?: return false
    return lastVisibleIndex >= totalItemsCount - 3
}
