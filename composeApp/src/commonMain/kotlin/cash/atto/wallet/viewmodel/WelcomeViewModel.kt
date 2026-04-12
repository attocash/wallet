package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.repository.MetricsRepository
import cash.atto.wallet.uistate.welcome.WelcomeMetricsUiState
import cash.atto.wallet.uistate.welcome.toWelcomeMetricsUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val metricsRepository: MetricsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(WelcomeMetricsUiState.DEFAULT)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            metricsRepository.metricsResponse.collect { metricsResponse ->
                _state.emit(
                    metricsResponse?.toWelcomeMetricsUiState() ?: WelcomeMetricsUiState.DEFAULT,
                )
            }
        }
    }
}
