package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.state.AppState
import cash.atto.wallet.uistate.AppUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AppViewModel(
    private val appStateRepository: AppStateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppUiState.DEFAULT)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            _state.emit(getAppState())
        }
    }

    private suspend fun getAppState() : AppUiState {
        val channel = Channel<AppUiState.ShownScreen>()

        CoroutineScope(Dispatchers.Default).launch {
            appStateRepository.state
                .collect { state ->
                    when (state.authState) {
                        AppState.AUTH_STATE.LOGGED -> channel.send(AppUiState.ShownScreen.OVERVIEW)
                        AppState.AUTH_STATE.UNLOGGED -> channel.send(AppUiState.ShownScreen.WELCOME)
                        AppState.AUTH_STATE.UNKNOWN -> {}
                    }
                }
        }

        return AppUiState(
            shownScreen = channel.receive()
        )
    }
}