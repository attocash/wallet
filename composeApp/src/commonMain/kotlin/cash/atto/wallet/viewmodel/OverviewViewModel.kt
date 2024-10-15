package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoUnit
import cash.atto.wallet.repository.AccountsRepository
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.uistate.overview.OverviewHeaderUiState
import cash.atto.wallet.uistate.overview.OverviewUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class OverviewViewModel(
    private val appStateRepository: AppStateRepository,
    private val accountsRepository: AccountsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OverviewUiState.DEFAULT)
    val state = _state.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            _state.value = OverviewUiState.empty()

            appStateRepository.state.collect {
                accountsRepository.openSocket(it)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            accountsRepository.accountState.collect {
                _state.emit(
                    state.value.copy(
                        headerUiState = OverviewHeaderUiState(
                            attoCoins = it?.balance
                                ?.toString(AttoUnit.ATTO)?.toBigDecimal()
                                ?: BigDecimal.ZERO
                        )
                    )
                )
            }
        }
    }
}