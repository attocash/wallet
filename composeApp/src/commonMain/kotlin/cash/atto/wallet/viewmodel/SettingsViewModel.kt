package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.toAddress
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.settings.ProfileUiState
import cash.atto.wallet.uistate.settings.SettingsListUiState
import cash.atto.wallet.uistate.settings.SettingsUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState.PREVIEW)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _state.value = SettingsUiState(
                profileUiState = ProfileUiState.DEFAULT,
                settingsListUiState = SettingsListUiState.settingsList()
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
}