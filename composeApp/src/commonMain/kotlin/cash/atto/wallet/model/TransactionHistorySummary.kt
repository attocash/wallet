package cash.atto.wallet.model

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoBlockType

data class TransactionHistorySummary(
    val totalTransactions: Int = 0,
    val totalReceived: AttoAmount = AttoAmount.MIN,
    val totalSent: AttoAmount = AttoAmount.MIN,
) {
    operator fun plus(other: TransactionHistorySummary) =
        TransactionHistorySummary(
            totalTransactions = totalTransactions + other.totalTransactions,
            totalReceived = totalReceived + other.totalReceived,
            totalSent = totalSent + other.totalSent,
        )
}

fun AttoAccountEntry.toTransactionHistorySummary() =
    when (blockType) {
        AttoBlockType.SEND -> {
            TransactionHistorySummary(
                totalTransactions = 1,
                totalSent = previousBalance - balance,
            )
        }

        AttoBlockType.RECEIVE,
        AttoBlockType.OPEN,
        -> {
            TransactionHistorySummary(
                totalTransactions = 1,
                totalReceived = balance - previousBalance,
            )
        }

        AttoBlockType.CHANGE -> {
            TransactionHistorySummary(
                totalTransactions = 1,
            )
        }

        else -> {
            TransactionHistorySummary()
        }
    }
