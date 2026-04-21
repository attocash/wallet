package cash.atto.wallet.uistate.transactions

import cash.atto.wallet.uistate.overview.TransactionUiState

data class TransactionsUiState(
    val loadedTransactions: List<TransactionUiState> = emptyList(),
    val isLoadingInitial: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val fullHistorySummary: TransactionsSummaryUiState = TransactionsSummaryUiState(),
)

data class TransactionsSummaryUiState(
    val totalTransactions: Int = 0,
    val totalReceivedText: String = "+0",
    val totalSentText: String = "-0",
    val netChangeText: String = "+0",
    val isLoading: Boolean = true,
)
