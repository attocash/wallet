package cash.atto.wallet.uistate.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

data class SettingsListUiState(
    val settings: List<SettingItemUiState>
) {
    companion object {
        val PREVIEW = SettingsListUiState(listOf(
            SettingItemUiState(
                icon = Icons.Filled.Person,
                title = "Contacts"
            ) {}
        ))
    }
}