package cash.atto.wallet.uistate.overview

data class OverviewUiState(
    val headerUiState: OverviewHeaderUiState,
    val transactionListUiState: TransactionListUiState
) {
    companion object {
        val DEFAULT = OverviewUiState(
            OverviewHeaderUiState.DEFAULT,
            TransactionListUiState.DEFAULT
        )

        suspend fun empty() = OverviewUiState(
            OverviewHeaderUiState.DEFAULT,
            TransactionListUiState.empty()
        )
    }
}