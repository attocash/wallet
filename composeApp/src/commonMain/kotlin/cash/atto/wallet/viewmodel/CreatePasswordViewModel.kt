package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.secret.CreatePasswordUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreatePasswordViewModel(
    private val appStateRepository: AppStateRepository,
    private val checkPasswordInteractor: CheckPasswordInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePasswordUIState.DEFAULT)
    val state = _state.asStateFlow()

    suspend fun setPassword(password: String?) {
        _state.emit(state.value.copy(
            password = password
        ))
    }

    suspend fun setPasswordConfirm(passwordConfirm: String?) {
        _state.emit(state.value.copy(
            passwordConfirm = passwordConfirm
        ))
    }

    suspend fun savePassword(): Boolean {
        var checkResult = checkPasswordInteractor.invoke(state.value.password)
        if (checkResult == CreatePasswordUIState.PasswordCheckState.VALID)
            checkResult = checkPasswordsMatch()

        _state.emit(
            state.value.copy(
                checkState = checkResult
            )
        )

        if (checkResult == CreatePasswordUIState.PasswordCheckState.VALID)
            appStateRepository.savePassword(state.value.password!!)

        return checkResult == CreatePasswordUIState.PasswordCheckState.VALID
    }

    suspend fun clearPassword() {
        _state.emit(CreatePasswordUIState.DEFAULT)
    }

    private fun checkPasswordsMatch() : CreatePasswordUIState.PasswordCheckState =
        with (state.value) {
            if (password != passwordConfirm)
                CreatePasswordUIState.PasswordCheckState.NON_MATCHING
            else CreatePasswordUIState.PasswordCheckState.VALID
        }
}