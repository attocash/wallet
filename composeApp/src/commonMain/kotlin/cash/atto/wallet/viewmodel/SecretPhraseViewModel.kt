package cash.atto.wallet.viewmodel

import cash.atto.wallet.uistate.secret.SecretPhraseUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SecretPhraseViewModel {

    private val _state = MutableStateFlow(SecretPhraseUiState.DEFAULT)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _state.value = SecretPhraseUiState(
                words = (1..24)
                    .map { "Word$it" }
                    .toList()
            )
        }
    }
}