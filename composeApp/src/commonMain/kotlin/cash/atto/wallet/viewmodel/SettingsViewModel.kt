package cash.atto.wallet.viewmodel

import cash.atto.wallet.uistate.settings.SettingsListUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel {

    private val _state = MutableStateFlow(SettingsListUiState.PREVIEW)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _state.value = SettingsListUiState.settingsList()
        }
    }
}