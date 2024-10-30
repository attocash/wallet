package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.secret.SecretPhraseUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackupSecretViewModel(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SecretPhraseUiState.DEFAULT)
    val state = _state.asStateFlow()

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    init {
        viewModelScope.launch {
            appStateRepository.state.value.mnemonic?.let {
                _state.emit(SecretPhraseUiState(
                    words = it.words,
                    hidden = true
                ))
            }
        }
    }

    fun hideSecretPhrase() = viewModelScope.launch {
        _state.emit(state.value.copy(hidden = true))
    }

    fun showSecretPhrase() = viewModelScope.launch {
        _state.emit(state.value.copy(hidden = false))
    }
}