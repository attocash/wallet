package cash.atto.wallet.uistate.desktop

data class MainScreenUiState(
    val balanceChipUiState: BalanceChipUiState
) {
    companion object {
        val DEFAULT = MainScreenUiState(
            balanceChipUiState = BalanceChipUiState.DEFAULT
        )
    }
}