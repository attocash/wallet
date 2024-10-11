package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.uistate.overview.OverviewUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OverviewViewModel : ViewModel() {

    private val _state = MutableStateFlow(OverviewUiState.DEFAULT)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _state.value = OverviewUiState.empty()
        }
    }
}