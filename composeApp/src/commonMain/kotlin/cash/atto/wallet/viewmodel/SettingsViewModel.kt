package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.toAddress
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.settings.ProfileUiState
import cash.atto.wallet.uistate.settings.SettingsUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val appStateRepository: AppStateRepository,
) : ViewModel() {
    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    private var appStateCollectorJob: Job? = null

    private val _state = MutableStateFlow(SettingsUiState.PREVIEW)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value =
                SettingsUiState(
                    profileUiState = ProfileUiState.DEFAULT,
                )

            delay(100)

            appStateCollectorJob?.cancel()
            appStateCollectorJob =
                viewModelScope.launch {
                    appStateRepository.state.collect { appState ->
                        appState.getPublicKey()?.let {
                            _state.emit(
                                state.value.copy(
                                    profileUiState =
                                        ProfileUiState(
                                            name = "Main Account",
                                            hash = it.toAddress(AttoAlgorithm.V1).value,
                                        ),
                                ),
                            )
                        }
                    }
                }
        }
    }

    fun hideLogoutDialog() =
        viewModelScope.launch {
            _state.emit(state.value.copy(showLogoutDialog = false))
        }

    fun lock() =
        viewModelScope.launch {
            appStateRepository.lock()
        }

    fun logout() =
        viewModelScope.launch {
            appStateRepository.deleteKeys()
        }

    fun showLogoutDialog() =
        viewModelScope.launch {
            _state.emit(state.value.copy(showLogoutDialog = true))
        }
}
