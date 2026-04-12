package cash.atto.wallet.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class SeedDataSourceMac : SeedDataSourceDesktopImpl {
    private val macCred = MacCred()

    private val seedChannel = Channel<String?>()
    override val seed = seedChannel.consumeAsFlow()

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
            seedChannel.send(
                macCred.getSeed(),
            )
        } catch (ex: Exception) {
            seedChannel.send(null)
        }
    }
}
