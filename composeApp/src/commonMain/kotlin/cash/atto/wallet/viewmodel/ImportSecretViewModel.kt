package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.secret.ImportSecretUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImportSecretViewModel(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ImportSecretUiState.DEFAULT)
    val state = _state.asStateFlow()

    suspend fun updateInput(value: String) = _state.emit(
        ImportSecretUiState(value)
    )

    suspend fun importWallet() {
        state.value
            .input
            ?.let {
                appStateRepository.importSecret(
                    it.split(' ')
                )
            }

    }
}