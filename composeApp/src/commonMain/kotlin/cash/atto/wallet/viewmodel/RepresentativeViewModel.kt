package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoPrivateKey
import cash.atto.commons.toPublicKey
import cash.atto.commons.toSigner
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.RepresentativeRepository
import cash.atto.wallet.uistate.settings.RepresentativeUIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RepresentativeViewModel(
    private val appStateRepository: AppStateRepository,
    private val representativeRepository: RepresentativeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RepresentativeUIState.DEFAULT)
    val state = _state.asStateFlow()

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    init {
        viewModelScope.launch {
            appStateRepository.state.collect { appState ->
                appState.privateKey?.let { privateKey ->
                    collectRepresentative(privateKey)
                }
            }
        }
    }

    suspend fun setRepresentative(address: String) {
        with (representativeRepository.state.value.wallet ?: return) {
            representativeRepository.setRepresentative(this, address)
        }
    }

    private suspend fun collectRepresentative(
        wallet: AttoPrivateKey
    ) {
        val publicKey = wallet.toPublicKey().toString()
        representativeRepository.getRepresentative(publicKey)

        representativeRepository.state.collect { representativeState ->
            if (representativeState.representative != null) {
                _state.emit(RepresentativeUIState(
                    representativeState.representative
                ))
            } else {
                val signer = wallet.toSigner()
                representativeRepository.setRepresentative(
                    wallet = publicKey,
                    address = AttoAddress(
                        AttoAlgorithm.V1,
                        signer.publicKey
                    ).toString()
                )
            }
        }
    }
}