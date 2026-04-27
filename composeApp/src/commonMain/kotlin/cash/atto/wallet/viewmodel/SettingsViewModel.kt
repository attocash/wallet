package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.toAddress
import cash.atto.wallet.platform.exportTextFile
import cash.atto.wallet.platform.importTextFile
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.PreferencesRepository
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
    private val preferencesRepository: PreferencesRepository,
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

    fun exportPreferences() =
        viewModelScope.launch {
            val message =
                runCatching {
                    val export =
                        exportTextFile(
                            fileName = "atto-preferences.json",
                            mimeType = "application/json;charset=utf-8",
                            content = preferencesRepository.exportPlainJson(),
                        )
                    "Preferences exported to ${export.location}"
                }.getOrElse { error ->
                    error.message ?: "Unable to export preferences."
                }

            showPreferencesMessage(message)
        }

    fun importPreferences() =
        viewModelScope.launch {
            val importedFile =
                runCatching {
                    importTextFile(
                        mimeTypes = listOf("application/json", "text/json"),
                        extensions = listOf(".json"),
                    )
                }.getOrElse { error ->
                    showPreferencesMessage(error.message ?: "Unable to import preferences.")
                    return@launch
                } ?: return@launch

            val message =
                runCatching {
                    preferencesRepository.importPlainJson(importedFile.content)
                    "Preferences imported from ${importedFile.location}"
                }.getOrElse { error ->
                    error.message ?: "Unable to import preferences."
                }

            showPreferencesMessage(message)
        }

    fun clearPreferencesMessage() =
        viewModelScope.launch {
            _state.emit(state.value.copy(preferencesMessage = null))
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

    private suspend fun showPreferencesMessage(message: String) {
        _state.emit(state.value.copy(preferencesMessage = message))
    }
}
