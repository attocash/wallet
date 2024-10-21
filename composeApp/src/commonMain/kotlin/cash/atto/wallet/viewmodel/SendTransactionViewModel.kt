package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.toAddress
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.send.SendFromUiState
import cash.atto.wallet.uistate.send.SendTransactionUiState
import cash.atto.wallet.uistate.settings.ProfileUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SendTransactionViewModel(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SendTransactionUiState.DEFAULT)
    val state = _state.asStateFlow()

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    init {
        viewModelScope.launch {
            appStateRepository.state.collect { appState ->
                if (appState.publicKey != null) {
                    _state.emit(state.value.copy(
                        sendFromUiState = state.value.sendFromUiState.copy(
                            accountName = "Main Account",
//                            accountSeed = appState.publicKey
//                                .toAddress(AttoAlgorithm.V1)
//                                .value
                        )
                    ))
                }
            }
        }
    }
}