package cash.atto.wallet.uistate.settings

data class SettingsUiState(
    val profileUiState: ProfileUiState,
    val settingsListUiState: SettingsListUiState
) {
    companion object {
        val PREVIEW = SettingsUiState(
            ProfileUiState.DEFAULT,
            SettingsListUiState.PREVIEW
        )
    }
}