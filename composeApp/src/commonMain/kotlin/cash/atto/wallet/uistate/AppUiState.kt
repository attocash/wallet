package cash.atto.wallet.uistate

data class AppUiState(
    val shownScreen: ShownScreen
) {
    enum class ShownScreen {
        LOADER,
        OVERVIEW,
        PASSWORD_CREATE,
        PASSWORD_ENTER,
        WELCOME;
    }

    companion object {
        val DEFAULT = AppUiState(ShownScreen.LOADER)
    }
}