package cash.atto.wallet.model

import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoBlockType
import cash.atto.commons.AttoUnit
import cash.atto.wallet.support.testAccountEntry
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionHistorySummaryTest {
    @Test
    fun `send contributes to sent total`() {
        val summary =
            testAccountEntry(
                height = 5UL,
                blockType = AttoBlockType.SEND,
                previousBalanceAtto = "10",
                balanceAtto = "7.5",
            ).toTransactionHistorySummary()

        assertEquals(1, summary.totalTransactions)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "2.5"), summary.totalSent)
        assertEquals(AttoAmount.MIN, summary.totalReceived)
    }

    @Test
    fun `open is counted as received`() {
        val summary =
            testAccountEntry(
                height = 1UL,
                blockType = AttoBlockType.OPEN,
                previousBalanceAtto = "0",
                balanceAtto = "42",
            ).toTransactionHistorySummary()

        assertEquals(1, summary.totalTransactions)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "42"), summary.totalReceived)
        assertEquals(AttoAmount.MIN, summary.totalSent)
    }

    @Test
    fun `change contributes only to transaction count`() {
        val summary =
            testAccountEntry(
                height = 9UL,
                blockType = AttoBlockType.CHANGE,
                previousBalanceAtto = "42",
                balanceAtto = "42",
            ).toTransactionHistorySummary()

        assertEquals(1, summary.totalTransactions)
        assertEquals(AttoAmount.MIN, summary.totalReceived)
        assertEquals(AttoAmount.MIN, summary.totalSent)
    }

    @Test
    fun `folded summaries preserve large precise amounts`() {
        val summary =
            listOf(
                testAccountEntry(
                    height = 1UL,
                    blockType = AttoBlockType.OPEN,
                    previousBalanceAtto = "0",
                    balanceAtto = "123456789.123456789",
                ),
                testAccountEntry(
                    height = 2UL,
                    blockType = AttoBlockType.RECEIVE,
                    previousBalanceAtto = "123456789.123456789",
                    balanceAtto = "123456790.12345679",
                ),
                testAccountEntry(
                    height = 3UL,
                    blockType = AttoBlockType.SEND,
                    previousBalanceAtto = "123456790.12345679",
                    balanceAtto = "123456780.12345679",
                ),
            ).fold(TransactionHistorySummary()) { acc, entry ->
                acc + entry.toTransactionHistorySummary()
            }

        assertEquals(3, summary.totalTransactions)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "123456790.12345679"), summary.totalReceived)
        assertEquals(AttoAmount.from(AttoUnit.ATTO, "10"), summary.totalSent)
    }
}
