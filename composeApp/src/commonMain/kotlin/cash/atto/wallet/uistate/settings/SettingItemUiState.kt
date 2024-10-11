package cash.atto.wallet.uistate.settings

import androidx.compose.ui.graphics.vector.ImageVector

data class SettingItemUiState(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit
)