package cash.atto.wallet.uistate.desktop

data class MainScreenUiState(
    val balanceChipUiState: BalanceChipUiState,
    val isWalletInitialized: Boolean = false,
) {
    companion object {
        val DEFAULT =
            MainScreenUiState(
                balanceChipUiState = BalanceChipUiState.DEFAULT,
                isWalletInitialized = false,
            )
    }
}
