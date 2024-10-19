package cash.atto.wallet.uistate.overview

data class OverviewUiState(
    val headerUiState: OverviewHeaderUiState,
    val transactionListUiState: TransactionListUiState,
    val receiveAddress: String?
) {
    companion object {
        val DEFAULT = OverviewUiState(
            OverviewHeaderUiState.DEFAULT,
            TransactionListUiState.DEFAULT,
            receiveAddress = null
        )

        suspend fun empty() = OverviewUiState(
            OverviewHeaderUiState.DEFAULT,
            TransactionListUiState.empty(),
            receiveAddress = null
        )
    }
}