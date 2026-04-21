package cash.atto.wallet.model

import cash.atto.commons.AttoBlockType
import cash.atto.wallet.support.testAccountEntry
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountEntryCollectionsTest {
    @Test
    fun `mergeAccountEntries sorts by descending height and removes duplicates`() {
        val current =
            listOf(
                testAccountEntry(height = 5UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "0", balanceAtto = "5"),
                testAccountEntry(height = 3UL, blockType = AttoBlockType.SEND, previousBalanceAtto = "5", balanceAtto = "3"),
            )
        val incoming =
            listOf(
                testAccountEntry(height = 4UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "3", balanceAtto = "4"),
                testAccountEntry(
                    height = 3UL,
                    blockType = AttoBlockType.SEND,
                    previousBalanceAtto = "5",
                    balanceAtto = "3",
                    hashHex = "0000000000000000000000000000000000000000000000000000000000000003",
                ),
            )

        val merged = mergeAccountEntries(current, incoming)

        assertEquals(listOf(5UL, 4UL, 3UL), merged.map { it.height.value })
    }

    @Test
    fun `mergeAccountEntries applies limit after sorting`() {
        val current =
            listOf(
                testAccountEntry(height = 10UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "0", balanceAtto = "10"),
                testAccountEntry(height = 8UL, blockType = AttoBlockType.RECEIVE, previousBalanceAtto = "10", balanceAtto = "18"),
            )
        val incoming =
            listOf(
                testAccountEntry(height = 9UL, blockType = AttoBlockType.SEND, previousBalanceAtto = "18", balanceAtto = "17"),
                testAccountEntry(height = 7UL, blockType = AttoBlockType.CHANGE, previousBalanceAtto = "17", balanceAtto = "17"),
            )

        val merged = mergeAccountEntries(current, incoming, limit = 3)

        assertEquals(listOf(10UL, 9UL, 8UL), merged.map { it.height.value })
    }

    @Test
    fun `mergeAccountEntries keeps existing entry when the same hash reappears`() {
        val existing =
            testAccountEntry(
                height = 6UL,
                blockType = AttoBlockType.RECEIVE,
                previousBalanceAtto = "0",
                balanceAtto = "6",
                hashHex = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            )
        val duplicate =
            testAccountEntry(
                height = 12UL,
                blockType = AttoBlockType.SEND,
                previousBalanceAtto = "6",
                balanceAtto = "1",
                hashHex = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            )

        val merged = mergeAccountEntries(listOf(existing), duplicate)

        assertEquals(listOf(6UL), merged.map { it.height.value })
        assertEquals(AttoBlockType.RECEIVE, merged.single().blockType)
    }
}
