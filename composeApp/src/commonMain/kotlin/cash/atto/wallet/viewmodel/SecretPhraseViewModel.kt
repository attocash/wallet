package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.state.AppState.AuthState
import cash.atto.wallet.uistate.secret.SecretPhraseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SecretPhraseViewModel(
    private val appStateRepository: AppStateRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SecretPhraseUiState.DEFAULT)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val words = appStateRepository.generateNewSecret()
            if (appStateRepository.state.value.authState == AuthState.NEW_ACCOUNT) {
                _state.value = SecretPhraseUiState(words = words, hidden = false)
            }
        }
        viewModelScope.launch {
            appStateRepository.state.collect { appState ->
                if (appState.authState != AuthState.NEW_ACCOUNT) {
                    _state.value = SecretPhraseUiState.DEFAULT
                }
            }
        }
    }

    override fun onCleared() {
        _state.value = SecretPhraseUiState.DEFAULT
        super.onCleared()
    }
}
