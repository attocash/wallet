package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.TermsAndConditionsRepository
import cash.atto.wallet.repository.WalletStorageException
import cash.atto.wallet.uistate.secret.CreatePasswordUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class CreatePasswordViewModel(
    private val appStateRepository: AppStateRepository,
    private val checkPasswordInteractor: CheckPasswordInteractor,
    private val termsAndConditionsRepository: TermsAndConditionsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CreatePasswordUIState.DEFAULT)
    val state = _state.asStateFlow()
    val termsAndConditionsAccepted = termsAndConditionsRepository.accepted

    suspend fun setTermsAndConditionsAccepted(accepted: Boolean) {
        termsAndConditionsRepository.setCurrentTermsAccepted(accepted)
    }

    suspend fun setPassword(password: String?) {
        _state.emit(
            state.value.copy(
                password = password,
            ),
        )
    }

    suspend fun setPasswordConfirm(passwordConfirm: String?) {
        _state.emit(
            state.value.copy(
                passwordConfirm = passwordConfirm,
            ),
        )
    }

    suspend fun savePassword(): Boolean {
        if (!termsAndConditionsAccepted.first()) return false
        var checkResult = checkPasswordInteractor.invoke(state.value.password)
        if (checkResult == CreatePasswordUIState.PasswordCheckState.VALID) {
            checkResult = checkPasswordsMatch()
        }

        _state.emit(
            state.value.copy(
                checkState = checkResult,
                storageError = null,
            ),
        )

        if (checkResult == CreatePasswordUIState.PasswordCheckState.VALID) {
            try {
                appStateRepository.savePassword(state.value.password!!)
            } catch (error: WalletStorageException) {
                _state.emit(state.value.copy(storageError = error.message))
                return false
            }
        }

        return checkResult == CreatePasswordUIState.PasswordCheckState.VALID
    }

    suspend fun clearPassword() {
        _state.emit(CreatePasswordUIState.DEFAULT)
    }

    private fun checkPasswordsMatch(): CreatePasswordUIState.PasswordCheckState =
        with(state.value) {
            if (password != passwordConfirm) {
                CreatePasswordUIState.PasswordCheckState.NON_MATCHING
            } else {
                CreatePasswordUIState.PasswordCheckState.VALID
            }
        }
}
