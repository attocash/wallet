package cash.atto.wallet.model

import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoBlockType
import cash.atto.commons.AttoUnit
import cash.atto.wallet.support.testAccountEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TransactionsHistoryStateTest {
    @Test
    fun `initial page sets cursor and hasMore`() {
        val page =
            listOf(
                testAccountEntry(height = 10UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "0", balanceAtto = "10"),
                testAccountEntry(height = 9UL, blockType = AttoBlockType.SEND, previousBalanceAtto = "10", balanceAtto = "9"),
            )

        val state = TransactionsHistoryState().withInitialPage(page, pageSize = 2)

        assertEquals(listOf(10UL, 9UL), state.loadedEntries.map { it.height.value })
        assertEquals(9L, state.nextBeforeHeightExclusive)
        assertTrue(state.hasMore)
    }

    @Test
    fun `older page advances cursor and stops when page is short`() {
        val initial =
            TransactionsHistoryState().withInitialPage(
                page =
                    listOf(
                        testAccountEntry(height = 10UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "0", balanceAtto = "10"),
                        testAccountEntry(height = 9UL, blockType = AttoBlockType.SEND, previousBalanceAtto = "10", balanceAtto = "9"),
                    ),
                pageSize = 2,
            )

        val next =
            initial.withOlderPage(
                page =
                    listOf(
                        testAccountEntry(height = 8UL, blockType = AttoBlockType.CHANGE, previousBalanceAtto = "9", balanceAtto = "9"),
                    ),
                pageSize = 2,
            )

        assertEquals(listOf(10UL, 9UL, 8UL), next.loadedEntries.map { it.height.value })
        assertEquals(8L, next.nextBeforeHeightExclusive)
        assertFalse(next.hasMore)
    }

    @Test
    fun `live duplicate does not change loaded entries or summary`() {
        val seedEntry =
            testAccountEntry(
                height = 5UL,
                blockType = AttoBlockType.RECEIVE,
                previousBalanceAtto = "0",
                balanceAtto = "5",
                hashHex = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            )
        val duplicate =
            testAccountEntry(
                height = 12UL,
                blockType = AttoBlockType.SEND,
                previousBalanceAtto = "5",
                balanceAtto = "2",
                hashHex = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            )

        val state =
            TransactionsHistoryState(
                loadedEntries = listOf(seedEntry),
                fullHistorySummary =
                    TransactionHistorySummary(
                        totalTransactions = 1,
                        totalReceived = AttoAmount.from(AttoUnit.ATTO, "5"),
                    ),
                isSummaryLoading = false,
            ).withLiveEntry(duplicate)

        assertEquals(listOf(5UL), state.loadedEntries.map { it.height.value })
        assertEquals(1, state.fullHistorySummary.totalTransactions)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "5"), state.fullHistorySummary.totalReceived)
        assertEquals(AttoAmount.MIN, state.fullHistorySummary.totalSent)
    }

    @Test
    fun `full history totals can stay complete while loaded page is partial`() {
        val partialPage =
            listOf(
                testAccountEntry(height = 4UL, blockType = AttoBlockType.SEND, previousBalanceAtto = "15", balanceAtto = "10"),
                testAccountEntry(height = 3UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "10", balanceAtto = "15"),
            )
        val fullSummary =
            TransactionHistorySummary(
                totalTransactions = 6,
                totalReceived = AttoAmount.from(AttoUnit.ATTO, "20"),
                totalSent = AttoAmount.from(AttoUnit.ATTO, "7"),
            )

        val state =
            TransactionsHistoryState()
                .withInitialPage(partialPage, pageSize = 50)
                .withSummary(fullSummary)

        assertEquals(2, state.loadedEntries.size)
        assertEquals(6, state.fullHistorySummary.totalTransactions)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "20"), state.fullHistorySummary.totalReceived)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "7"), state.fullHistorySummary.totalSent)
        assertFalse(state.isSummaryLoading)
    }

    @Test
    fun `empty initial page clears cursor`() {
        val state = TransactionsHistoryState().withInitialPage(emptyList(), pageSize = 50)

        assertTrue(state.loadedEntries.isEmpty())
        assertNull(state.nextBeforeHeightExclusive)
        assertFalse(state.hasMore)
    }
}
