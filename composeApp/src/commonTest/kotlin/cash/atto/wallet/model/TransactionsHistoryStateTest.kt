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
        // Given
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

        // When
        val state =
            TransactionsHistoryState()
                .withInitialPage(partialPage, pageSize = 50)
                .withSummary(TransactionHistorySnapshot(fullSummary, throughHeight = 6UL))

        // Then
        assertEquals(2, state.loadedEntries.size)
        assertEquals(6, state.fullHistorySummary.totalTransactions)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "20"), state.fullHistorySummary.totalReceived)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "7"), state.fullHistorySummary.totalSent)
        assertFalse(state.isSummaryLoading)
    }

    @Test
    fun `initial page retains notifications delivered while the page query was pending`() {
        // Given
        val initial = testAccountEntry(height = 4UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "0", balanceAtto = "4")
        val committed = testAccountEntry(height = 5UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "4", balanceAtto = "7")
        val pending = TransactionsHistoryState().withLiveEntry(committed)

        // When
        val loaded = pending.withInitialPage(listOf(initial), pageSize = 1)
        val completed = loaded.withSummary(TransactionHistorySnapshot(initial.toTransactionHistorySummary(), throughHeight = 4UL))

        // Then
        assertEquals(listOf(5UL, 4UL), completed.loadedEntries.map { it.height.value })
        assertEquals(4L, completed.nextBeforeHeightExclusive)
        assertTrue(completed.hasMore)
        assertEquals(2, completed.fullHistorySummary.totalTransactions)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "7"), completed.fullHistorySummary.totalReceived)
    }

    @Test
    fun `notification already included in persisted snapshot does not increment totals`() {
        // Given
        val initial = testAccountEntry(height = 4UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "0", balanceAtto = "4")
        val committed = testAccountEntry(height = 5UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "4", balanceAtto = "7")
        val snapshot =
            TransactionHistorySnapshot(
                summary = initial.toTransactionHistorySummary() + committed.toTransactionHistorySummary(),
                throughHeight = 5UL,
            )
        val state = TransactionsHistoryState().withInitialPage(listOf(initial), pageSize = 50).withSummary(snapshot)

        // When
        val notified = state.withLiveEntry(committed)

        // Then
        assertEquals(listOf(5UL, 4UL), notified.loadedEntries.map { it.height.value })
        assertEquals(snapshot.summary, notified.fullHistorySummary)
    }

    @Test
    fun `snapshot completion retains notifications committed after its watermark`() {
        // Given
        val initial = testAccountEntry(height = 4UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "0", balanceAtto = "4")
        val next = testAccountEntry(height = 5UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "4", balanceAtto = "7")
        val snapshot = TransactionHistorySnapshot(initial.toTransactionHistorySummary(), throughHeight = 4UL)
        val pending = TransactionsHistoryState().withInitialPage(listOf(initial), pageSize = 50).withLiveEntry(next)

        // When
        val completed = pending.withSummary(snapshot)

        // Then
        assertEquals(2, completed.fullHistorySummary.totalTransactions)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "7"), completed.fullHistorySummary.totalReceived)
        assertEquals(completed.fullHistorySummary, completed.withLiveEntry(next).fullHistorySummary)
    }

    @Test
    fun `replayed persisted entry outside loaded page leaves snapshot totals unchanged`() {
        // Given
        val recent = testAccountEntry(height = 10UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "9", balanceAtto = "10")
        val replay = testAccountEntry(height = 2UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "1", balanceAtto = "2")
        val summary = TransactionHistorySummary(totalTransactions = 10, totalReceived = AttoAmount.from(AttoUnit.ATTO, "10"))
        val state =
            TransactionsHistoryState()
                .withInitialPage(listOf(recent), pageSize = 1)
                .withSummary(TransactionHistorySnapshot(summary, throughHeight = 10UL))

        // When
        val replayed = state.withLiveEntry(replay)

        // Then
        assertEquals(listOf(10UL, 2UL), replayed.loadedEntries.map { it.height.value })
        assertEquals(summary, replayed.fullHistorySummary)
        assertEquals(10L, replayed.nextBeforeHeightExclusive)
        assertTrue(replayed.hasMore)
    }

    @Test
    fun `out of order notifications above fixed snapshot watermark each count once`() {
        // Given
        val first = testAccountEntry(height = 1UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "0", balanceAtto = "1")
        val second = testAccountEntry(height = 2UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "1", balanceAtto = "3")
        val third = testAccountEntry(height = 3UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "3", balanceAtto = "6")
        val state = TransactionsHistoryState().withSummary(TransactionHistorySnapshot(first.toTransactionHistorySummary(), 1UL))

        // When
        val notified = state.withLiveEntry(third).withLiveEntry(second).withLiveEntry(third)

        // Then
        assertEquals(3, notified.fullHistorySummary.totalTransactions)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "6"), notified.fullHistorySummary.totalReceived)
        assertEquals(listOf(3UL, 2UL), notified.loadedEntries.map { it.height.value })
    }

    @Test
    fun `empty initial page clears cursor`() {
        val state = TransactionsHistoryState().withInitialPage(emptyList(), pageSize = 50)

        assertTrue(state.loadedEntries.isEmpty())
        assertNull(state.nextBeforeHeightExclusive)
        assertFalse(state.hasMore)
    }
}
