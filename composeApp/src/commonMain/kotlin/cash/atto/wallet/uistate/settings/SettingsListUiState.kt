package cash.atto.wallet.uistate.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBox
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.settings_contacts
import attowallet.composeapp.generated.resources.settings_load
import attowallet.composeapp.generated.resources.settings_logout
import attowallet.composeapp.generated.resources.settings_notifications
import attowallet.composeapp.generated.resources.settings_representative
import attowallet.composeapp.generated.resources.settings_security
import org.jetbrains.compose.resources.getString

data class SettingsListUiState(
    val settings: List<SettingItemUiState>
) {
    companion object {
        suspend fun settingsList() = SettingsListUiState(listOf(
            SettingItemUiState(
                icon = Icons.Outlined.AccountBox,
                title = getString(Res.string.settings_contacts)
            ) {},
            SettingItemUiState(
                icon = Icons.Filled.Notifications,
                title = getString(Res.string.settings_notifications)
            ) {},
            SettingItemUiState(
                icon = Icons.Filled.Warning,
                title = getString(Res.string.settings_security)
            ) {},
            SettingItemUiState(
                icon = Icons.Filled.KeyboardArrowDown,
                title = getString(Res.string.settings_load)
            ) {},
            SettingItemUiState(
                icon = Icons.Filled.Home,
                title = getString(Res.string.settings_representative)
            ) {},
            SettingItemUiState(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = getString(Res.string.settings_logout)
            ) {}
        ))

        val PREVIEW = SettingsListUiState(listOf(
            SettingItemUiState(
                icon = Icons.Filled.Person,
                title = "Contacts"
            ) {}
        ))
    }
}