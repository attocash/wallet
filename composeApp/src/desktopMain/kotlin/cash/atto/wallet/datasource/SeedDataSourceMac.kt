package cash.atto.wallet.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class SeedDataSourceMac : SeedDataSourceDesktopImpl {

    private val macCred = MacCred()

    private val _seedChannel = Channel<String?>()
    override val seed = _seedChannel.consumeAsFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            getSeed()
        }
    }

    override suspend fun setSeed(seed: String) {
        macCred.storeSeed(seed)
        getSeed()
    }

    override suspend fun clearSeed() {
        macCred.deleteSeed()
        getSeed()
    }

    private suspend fun getSeed() {
        try {
            _seedChannel.send(
                macCred.getSeed()
            )
        } catch (ex: Exception) {
            _seedChannel.send(null)
        }
    }
}