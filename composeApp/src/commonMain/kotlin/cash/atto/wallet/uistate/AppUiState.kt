package cash.atto.wallet.uistate

data class AppUiState(
    val shownScreen: ShownScreen
) {
    enum class ShownScreen {
        LOADER, WELCOME, OVERVIEW
    }

    companion object {
        val DEFAULT = AppUiState(ShownScreen.LOADER)
    }
}