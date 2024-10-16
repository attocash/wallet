package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.AppUiState

class AppViewModel(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    fun getAppState() : AppUiState {
        return AppUiState(
            skipWelcome = appStateRepository.state.value.privateKey != null
        )
    }
}