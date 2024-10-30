package cash.atto.wallet.repository

import cash.atto.wallet.datasource.Representative
import cash.atto.wallet.datasource.RepresentativeDao
import cash.atto.wallet.state.RepresentativeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RepresentativeRepository(
    private val representativeDao: RepresentativeDao
) {

    private val _state = MutableStateFlow(RepresentativeState.DEFAULT)
    val state = _state.asStateFlow()

    suspend fun getRepresentative(wallet: String) {
        val representativeAddress = representativeDao
            .getRepresentatives(wallet)
            .firstOrNull()
            ?.address

        _state.emit(RepresentativeState(
            wallet = wallet,
            representative = representativeAddress
        ))
    }

    suspend fun setRepresentative(
        wallet: String,
        address: String
    ) {
        val representative = Representative(
            publicKey = wallet,
            address = address
        )

        representativeDao.createRepresentative(representative)
        getRepresentative(wallet)
    }
}