package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.PlatformType
import cash.atto.wallet.getPlatform
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.TermsAndConditionsRepository
import cash.atto.wallet.repository.WalletStorageException
import cash.atto.wallet.state.AppState
import cash.atto.wallet.uistate.AppUiState
import cash.atto.wallet.uistate.secret.CreatePasswordUIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppViewModel(
    private val appStateRepository: AppStateRepository,
    private val checkPasswordInteractor: CheckPasswordInteractor,
    private val termsAndConditionsRepository: TermsAndConditionsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AppUiState.DEFAULT)
    val state = _state.asStateFlow()

    private val authenticationError = MutableStateFlow<String?>(null)

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            combine(
                appStateRepository.state,
                termsAndConditionsRepository.accepted,
                authenticationError,
            ) { appState, termsAndConditionsAccepted, error ->
                AppUiState(
                    shownScreen = appState.shownScreen(),
                    authenticationError = error,
                    termsAndConditionsAccepted = termsAndConditionsAccepted,
                )
            }.collect { uiState ->
                _state.emit(uiState)
            }
        }
    }

    suspend fun enterPassword(password: String?): Boolean {
        authenticationError.value = null
        if (!termsAndConditionsRepository.accepted.first()) return false
        val checkResult = checkPasswordInteractor.invoke(password)
        if (checkResult == CreatePasswordUIState.PasswordCheckState.VALID) {
            return try {
                appStateRepository.submitPassword(password!!)
            } catch (error: WalletStorageException) {
                authenticationError.value = error.message
                false
            }
        }

        return false
    }

    suspend fun setTermsAndConditionsAccepted(accepted: Boolean) {
        termsAndConditionsRepository.setCurrentTermsAccepted(accepted)
    }

    fun logout() {
        scope.launch {
            appStateRepository.deleteKeys()
        }
    }
}

private fun AppState.shownScreen(): AppUiState.ShownScreen =
    when (authState) {
        AppState.AuthState.NEW_ACCOUNT -> {
            AppUiState.ShownScreen.WELCOME
        }

        AppState.AuthState.NO_PASSWORD -> {
            if (getPlatform().type == PlatformType.WEB) {
                AppUiState.ShownScreen.PASSWORD_ENTER
            } else {
                AppUiState.ShownScreen.PASSWORD_CREATE
            }
        }

        AppState.AuthState.NO_SEED -> {
            AppUiState.ShownScreen.WELCOME
        }

        AppState.AuthState.SESSION_INVALID -> {
            AppUiState.ShownScreen.PASSWORD_ENTER
        }

        AppState.AuthState.SESSION_VALID -> {
            AppUiState.ShownScreen.OVERVIEW
        }

        AppState.AuthState.UNKNOWN -> {
            AppUiState.ShownScreen.LOADER
        }
    }
