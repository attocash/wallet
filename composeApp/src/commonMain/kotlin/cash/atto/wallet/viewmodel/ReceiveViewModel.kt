package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.toAddress
import cash.atto.wallet.repository.HomeRepository
import cash.atto.wallet.repository.WalletManagerRepository
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReceiveViewModel(
    private val walletManagerRepository: WalletManagerRepository,
    private val homeRepository: HomeRepository,
) : ViewModel() {
    private val _address = MutableStateFlow<String?>(null)
    val address = _address.asStateFlow()
    private val _priceUsd = MutableStateFlow<BigDecimal?>(null)
    val priceUsd = _priceUsd.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            walletManagerRepository.publicKeyState.collect { publicKey ->
                _address.emit(
                    publicKey
                        ?.toAddress(AttoAlgorithm.V1)
                        ?.value,
                )
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            homeRepository.homeResponse.collect {
                _priceUsd.emit(homeRepository.getPriceUsd())
            }
        }
    }
}
