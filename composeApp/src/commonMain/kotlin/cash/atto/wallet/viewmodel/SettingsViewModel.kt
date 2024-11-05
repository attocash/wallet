package cash.atto.wallet.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBox
import androidx.lifecycle.ViewModel
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.settings_backup
import attowallet.composeapp.generated.resources.settings_contacts
import attowallet.composeapp.generated.resources.settings_load
import attowallet.composeapp.generated.resources.settings_logout
import attowallet.composeapp.generated.resources.settings_notifications
import attowallet.composeapp.generated.resources.settings_representative
import attowallet.composeapp.generated.resources.settings_security
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.toAddress
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.settings.ProfileUiState
import cash.atto.wallet.uistate.settings.SettingItemUiState
import cash.atto.wallet.uistate.settings.SettingsListUiState
import cash.atto.wallet.uistate.settings.SettingsUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class SettingsViewModel(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    private val _state = MutableStateFlow(SettingsUiState.PREVIEW)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = SettingsUiState(
                profileUiState = ProfileUiState.DEFAULT,
                settingsListUiState = settingsList()
            )

            appStateRepository.state.collect { appState ->
                if (appState.publicKey != null) {
                    _state.emit(state.value.copy(
                        profileUiState = ProfileUiState(
                            name = "Main Account",
                            hash = appState.publicKey
                                .toAddress(AttoAlgorithm.V1)
                                .value
                        )
                    ))
                }
            }
        }
    }

    fun handleBackupNavigation() = viewModelScope.launch {
        _state.emit(state.value.copy(navigateToBackup = false))
    }

    fun handleRepresentativeNavigation() = viewModelScope.launch {
        _state.emit(state.value.copy(navigateToRepresentative = false))
    }

    fun hideLogoutDialog() = viewModelScope.launch {
        _state.emit(state.value.copy(showLogoutDialog = false))
    }

    fun logout() = viewModelScope.launch {
        appStateRepository.deleteKeys()
    }

    private fun navigateToBackup() = viewModelScope.launch {
        _state.emit(state.value.copy(navigateToBackup = true))
    }

    private fun navigateToRepresentative() = viewModelScope.launch {
        _state.emit(state.value.copy(navigateToRepresentative = true))
    }

    private fun showLogoutDialog() = viewModelScope.launch {
        _state.emit(state.value.copy(showLogoutDialog = true))
    }

    private suspend fun settingsList() = SettingsListUiState(listOf(
        SettingItemUiState(
            icon = Icons.Filled.Refresh,
            title = getString(Res.string.settings_backup)
        ) { navigateToBackup() },
        SettingItemUiState(
            icon = Icons.Filled.Home,
            title = getString(Res.string.settings_representative)
        ) { navigateToRepresentative() },
        SettingItemUiState(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            title = getString(Res.string.settings_logout)
        ) { showLogoutDialog() }
    ))
}