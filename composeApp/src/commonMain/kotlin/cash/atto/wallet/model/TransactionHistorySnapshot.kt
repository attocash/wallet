package cash.atto.wallet.model

/** Absolute persisted totals and the account height included in the same database snapshot. */
data class TransactionHistorySnapshot(
    val summary: TransactionHistorySummary,
    val throughHeight: ULong = 0UL,
)
