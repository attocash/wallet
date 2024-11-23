package cash.atto.wallet.uistate.desktop

import cash.atto.wallet.uistate.settings.SettingsUiState

data class MainScreenUiState(
    val balanceChipUiState: BalanceChipUiState,
    val settingsUiState: SettingsUiState
) {
    val navigateToBackup get() = settingsUiState.navigateToBackup
    val navigateToRepresentative get() = settingsUiState.navigateToRepresentative
    val showLogoutDialog get() = settingsUiState.showLogoutDialog

    companion object {
        val DEFAULT = MainScreenUiState(
            balanceChipUiState = BalanceChipUiState.DEFAULT,
            settingsUiState = SettingsUiState.PREVIEW
        )
    }
}