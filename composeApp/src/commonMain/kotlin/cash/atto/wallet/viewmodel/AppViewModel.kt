package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.state.AppState
import cash.atto.wallet.uistate.AppUiState
import cash.atto.wallet.uistate.secret.CreatePasswordUIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val appStateRepository: AppStateRepository,
    private val checkPasswordInteractor: CheckPasswordInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(AppUiState.DEFAULT)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            appStateRepository.state.collect {
                _state.emit(
                    AppUiState(
                        shownScreen = when (it.authState) {
                            AppState.AuthState.NO_PASSWORD -> AppUiState.ShownScreen.PASSWORD_CREATE
                            AppState.AuthState.NO_SEED -> AppUiState.ShownScreen.WELCOME
                            AppState.AuthState.SESSION_INVALID -> AppUiState.ShownScreen.PASSWORD_ENTER
                            AppState.AuthState.SESSION_VALID -> AppUiState.ShownScreen.OVERVIEW
                            AppState.AuthState.UNKNOWN -> AppUiState.ShownScreen.LOADER
                        }
                    )
                )
            }
        }
    }

    suspend fun enterPassword(password: String?): Boolean {
        val checkResult = checkPasswordInteractor.invoke(password)
        if (checkResult == CreatePasswordUIState.PasswordCheckState.VALID)
            return appStateRepository.submitPassword(password!!)

        return false
    }
}