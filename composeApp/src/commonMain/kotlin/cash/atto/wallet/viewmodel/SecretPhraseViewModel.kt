package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.secret.SecretPhraseUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SecretPhraseViewModel(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SecretPhraseUiState.DEFAULT)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _state.value = SecretPhraseUiState(
                words = appStateRepository.generateNewSecret(),
                hidden = false
            )
        }
    }
}