package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoMnemonic
import cash.atto.commons.AttoMnemonicException
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.secret.ImportSecretUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImportSecretViewModel(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ImportSecretUiState.DEFAULT)
    val state = _state.asStateFlow()

    suspend fun updateInput(value: String) {
        _state.emit(
            state.value
                .copy(input = value)
        )

        checkWallet()
    }

    suspend fun importWallet(): Boolean {
        if (!checkWallet())
            return false

        state.value
            .input
            ?.let {
                appStateRepository.importSecret(
                    it.split(' ')
                )
            }

        return true
    }

    private suspend fun checkWallet(): Boolean {
        try {
            val mnemonic = AttoMnemonic(state.value.input.orEmpty())
            _state.emit(
                state.value
                    .copy(errorMessage = null)
            )
        } catch (ex: AttoMnemonicException) {
            _state.emit(
                state.value
                    .copy(errorMessage = ex.message)
            )

            return false
        }

        return true
    }
}