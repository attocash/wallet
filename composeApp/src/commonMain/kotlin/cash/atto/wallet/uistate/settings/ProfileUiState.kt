package cash.atto.wallet.uistate.settings

data class ProfileUiState(
    val name: String,
    val hash: String
) {
    companion object {
        val DEFAULT = ProfileUiState(
            name = "Loading...",
            hash = "Loading..."
        )
    }
}