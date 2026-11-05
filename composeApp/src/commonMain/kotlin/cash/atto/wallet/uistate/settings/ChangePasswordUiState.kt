package cash.atto.wallet.uistate.settings

data class ChangePasswordUiState(
    val saving: Boolean = false,
    val changed: Boolean = false,
    val error: String? = null,
    val incorrectCurrentPassword: Boolean = false,
)
