package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.secret.SecretPhraseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackupSecretViewModel(
    private val appStateRepository: AppStateRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SecretPhraseUiState.DEFAULT)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appStateRepository.state.collect { appState ->
                _state.value =
                    SecretPhraseUiState(
                        words = appState.mnemonic?.words.orEmpty(),
                        hidden = true,
                    )
            }
        }
    }

    override fun onCleared() {
        _state.value = SecretPhraseUiState.DEFAULT
        super.onCleared()
    }

    fun hideSecretPhrase() =
        viewModelScope.launch {
            _state.emit(state.value.copy(hidden = true))
        }

    fun showSecretPhrase() =
        viewModelScope.launch {
            _state.emit(state.value.copy(hidden = false))
        }
}
