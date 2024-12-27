package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.toAddress
import cash.atto.commons.wallet.AttoWalletManager
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.uistate.settings.RepresentativeUIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RepresentativeViewModel(
    private val walletManagerRepository: WalletManagerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RepresentativeUIState.DEFAULT)
    val state = _state.asStateFlow()

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    init {
        viewModelScope.launch {
            walletManagerRepository.state
                .collect { updateRepresentative(it) }
        }
    }

    suspend fun setRepresentative(address: String): Boolean {
        if (checkAddress(address)) {
            walletManagerRepository.changeRepresentative(
                AttoAddress.parse(address)
            )

            updateRepresentative(walletManagerRepository.state.value)

            return true
        }

        return false
    }

    private suspend fun checkAddress(address: String): Boolean {
        val result = AttoAddress.isValid(address)

        _state.emit(
            state.value.copy(showError = !result)
        )

        return result
    }

    private suspend fun updateRepresentative(
        walletManager: AttoWalletManager?
    ) {
        if (walletManager?.account == null)
            return

        _state.emit(
            RepresentativeUIState(
                walletManager.account!!
                    .representativePublicKey
                    .toAddress(AttoAlgorithm.V1)
                    .value
            )
        )
    }
}