package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.uistate.secret.CreatePasswordUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreatePasswordViewModel : ViewModel() {

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

    suspend fun savePassword() {}
}