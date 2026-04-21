package cash.atto.wallet.model

import cash.atto.commons.AttoAccountEntry

data class AccountEntryHistorySnapshot(
    val entries: List<AttoAccountEntry>,
    val summary: TransactionHistorySummary,
)
