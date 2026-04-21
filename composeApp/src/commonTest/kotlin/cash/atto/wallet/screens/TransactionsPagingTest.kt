package cash.atto.wallet.screens

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransactionsPagingTest {
    @Test
    fun `load more triggers near the end of a loaded list`() {
        assertTrue(
            shouldLoadMoreTransactions(
                hasMore = true,
                isLoadingMore = false,
                loadedTransactionsCount = 50,
                lastVisibleItemIndex = 8,
                totalItemsCount = 10,
            ),
        )
    }

    @Test
    fun `load more stays off while already loading or when history is exhausted`() {
        assertFalse(
            shouldLoadMoreTransactions(
                hasMore = false,
                isLoadingMore = false,
                loadedTransactionsCount = 50,
                lastVisibleItemIndex = 8,
                totalItemsCount = 10,
            ),
        )
        assertFalse(
            shouldLoadMoreTransactions(
                hasMore = true,
                isLoadingMore = true,
                loadedTransactionsCount = 50,
                lastVisibleItemIndex = 8,
                totalItemsCount = 10,
            ),
        )
    }

    @Test
    fun `filtered boundary still loads more based on visible list end`() {
        assertTrue(
            shouldLoadMoreTransactions(
                hasMore = true,
                isLoadingMore = false,
                loadedTransactionsCount = 40,
                lastVisibleItemIndex = 2,
                totalItemsCount = 4,
            ),
        )
    }

    @Test
    fun `no loaded transactions means no load more trigger`() {
        assertFalse(
            shouldLoadMoreTransactions(
                hasMore = true,
                isLoadingMore = false,
                loadedTransactionsCount = 0,
                lastVisibleItemIndex = 2,
                totalItemsCount = 4,
            ),
        )
    }
}
