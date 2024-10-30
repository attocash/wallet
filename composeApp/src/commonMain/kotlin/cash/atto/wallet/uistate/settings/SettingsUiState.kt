package cash.atto.wallet.uistate.settings

data class SettingsUiState(
    val profileUiState: ProfileUiState,
    val settingsListUiState: SettingsListUiState,
    val navigateToBackup: Boolean = false,
    val navigateToRepresentative: Boolean = false,
    val showLogoutDialog: Boolean = false
) {
    companion object {
        val PREVIEW = SettingsUiState(
            ProfileUiState.DEFAULT,
            SettingsListUiState.PREVIEW
        )
    }
}