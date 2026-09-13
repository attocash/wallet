package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoMnemonic
import cash.atto.commons.AttoMnemonicException
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.secret.ImportSecretUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ImportSecretViewModel(
    private val appStateRepository: AppStateRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ImportSecretUiState.DEFAULT)
    val state = _state.asStateFlow()

    suspend fun updateInput(value: String) {
        _state.emit(
            state.value
                .copy(input = value),
        )

        checkWallet(value)
    }

    suspend fun importWallet(): Boolean {
        val input = state.value.input.orEmpty()
        if (!checkWallet(input)) {
            return false
        }

        appStateRepository.importSecret(input.split(' '))
        _state.value = ImportSecretUiState.DEFAULT

        return true
    }

    private suspend fun checkWallet(input: String): Boolean {
        val (isValid, errorMessage) =
            try {
                AttoMnemonic.fromPhrase(input)
                true to null
            } catch (ex: AttoMnemonicException) {
                false to ex.message
            }

        _state.update { current ->
            if (current.input == input) {
                current.copy(errorMessage = errorMessage)
            } else {
                current
            }
        }

        return isValid && state.value.input == input
    }
}
