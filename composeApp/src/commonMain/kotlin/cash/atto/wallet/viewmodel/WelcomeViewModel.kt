package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.repository.HomeRepository
import cash.atto.wallet.uistate.welcome.WelcomeMetricsUiState
import cash.atto.wallet.uistate.welcome.toWelcomeMetricsUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(WelcomeMetricsUiState.DEFAULT)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            homeRepository.homeResponse.collect { homeResponse ->
                _state.emit(
                    homeResponse?.toWelcomeMetricsUiState() ?: WelcomeMetricsUiState.DEFAULT
                )
            }
        }
    }
}
