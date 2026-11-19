package cash.atto.wallet.uistate.overview

data class TransactionListUiState(
    val transactions: List<TransactionUiState>,
    val status: Status = Status.READY,
) {
    enum class Status {
        LOADING,
        READY,
        UNAVAILABLE,
    }

    companion object {
        val DEFAULT = TransactionListUiState(emptyList(), Status.LOADING)
    }
}
