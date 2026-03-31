package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.toAddress
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.HomeRepository
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReceiveViewModel(
    private val appStateRepository: AppStateRepository,
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    private val _address = MutableStateFlow<String?>(null)
    val address = _address.asStateFlow()
    private val _priceUsd = MutableStateFlow<BigDecimal?>(null)
    val priceUsd = _priceUsd.asStateFlow()

    init {
        viewModelScope.launch {
            appStateRepository.state.collect { appState ->
                _address.emit(
                    appState.getPublicKey()
                        ?.toAddress(AttoAlgorithm.V1)
                        ?.value
                )
            }
        }

        viewModelScope.launch {
            homeRepository.homeResponse.collect {
                _priceUsd.emit(homeRepository.getPriceUsd())
            }
        }
    }
}
