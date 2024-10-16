package cash.atto.wallet.uistate

data class AppUiState(
    val skipWelcome: Boolean
) {
    companion object {
        val DEFAULT = AppUiState(false)
    }
}