package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.uistate.settings.SettingsListUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsListUiState.PREVIEW)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _state.value = SettingsListUiState.settingsList()
        }
    }
}