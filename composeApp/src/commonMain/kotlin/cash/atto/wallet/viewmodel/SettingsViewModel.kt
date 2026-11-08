package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.toAddress
import cash.atto.wallet.model.WorkSourcePreference
import cash.atto.wallet.platform.exportTextFile
import cash.atto.wallet.platform.importTextFile
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.PreferencesRepository
import cash.atto.wallet.uistate.settings.ProfileUiState
import cash.atto.wallet.uistate.settings.SettingsUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val appStateRepository: AppStateRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState.PREVIEW)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            _state.value =
                SettingsUiState(
                    profileUiState = ProfileUiState.DEFAULT,
                )

            delay(100)

            viewModelScope.launch(Dispatchers.Default) {
                appStateRepository.state.collect { appState ->
                    appState.getPublicKey()?.let {
                        _state.update { settingsUiState ->
                            settingsUiState.copy(
                                profileUiState =
                                    ProfileUiState(
                                        name = "Main Account",
                                        hash = it.toAddress(AttoAlgorithm.V1).value,
                                    ),
                            )
                        }
                    }
                }
            }

            viewModelScope.launch(Dispatchers.Default) {
                preferencesRepository.workSource.collect { workSource ->
                    _state.update { settingsUiState ->
                        settingsUiState.copy(
                            workSource = workSource,
                        )
                    }
                }
            }
        }
    }

    fun hideLogoutDialog() =
        viewModelScope.launch(Dispatchers.Default) {
            _state.update { it.copy(showLogoutDialog = false) }
        }

    fun exportPreferences() =
        viewModelScope.launch(Dispatchers.Default) {
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
                    if (error is CancellationException) throw error
                    error.message ?: "Unable to export preferences."
                }

            showPreferencesMessage(message)
        }

    fun importPreferences() =
        viewModelScope.launch(Dispatchers.Default) {
            val importedFile =
                runCatching {
                    importTextFile(
                        mimeTypes = listOf("application/json", "text/json"),
                        extensions = listOf(".json"),
                    )
                }.getOrElse { error ->
                    if (error is CancellationException) throw error
                    showPreferencesMessage(error.message ?: "Unable to import preferences.")
                    return@launch
                } ?: return@launch

            val message =
                runCatching {
                    preferencesRepository.importPlainJson(importedFile.content)
                    "Preferences imported from ${importedFile.location}"
                }.getOrElse { error ->
                    if (error is CancellationException) throw error
                    error.message ?: "Unable to import preferences."
                }

            showPreferencesMessage(message)
        }

    fun setWorkSource(workSource: WorkSourcePreference) =
        viewModelScope.launch(Dispatchers.Default) {
            preferencesRepository.setWorkSource(workSource)
        }

    fun clearPreferencesMessage() =
        viewModelScope.launch(Dispatchers.Default) {
            _state.update { it.copy(preferencesMessage = null) }
        }

    fun lock() =
        viewModelScope.launch(Dispatchers.Default) {
            appStateRepository.lock()
        }

    fun logout() =
        viewModelScope.launch(Dispatchers.Default) {
            appStateRepository.deleteKeys()
        }

    fun showLogoutDialog() =
        viewModelScope.launch(Dispatchers.Default) {
            _state.update { it.copy(showLogoutDialog = true) }
        }

    private suspend fun showPreferencesMessage(message: String) {
        _state.update { it.copy(preferencesMessage = message) }
    }
}
