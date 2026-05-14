package cash.atto.wallet.uistate.settings

import cash.atto.wallet.model.WorkSourcePreference

data class SettingsUiState(
    val profileUiState: ProfileUiState,
    val workSource: WorkSourcePreference = WorkSourcePreference.REMOTE,
    val showLogoutDialog: Boolean = false,
    val preferencesMessage: String? = null,
) {
    companion object {
        val PREVIEW =
            SettingsUiState(
                ProfileUiState.DEFAULT,
            )
    }
}
