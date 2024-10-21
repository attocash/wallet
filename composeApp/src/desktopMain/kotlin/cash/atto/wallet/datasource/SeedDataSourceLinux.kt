package cash.atto.wallet.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class SeedDataSourceLinux : SeedDataSourceDesktopImpl {

    private val linuxCred = LinuxCred()

    private val _seedChannel = Channel<String?>()
    override val seed = _seedChannel.consumeAsFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            getSeed()
        }
    }

    override suspend fun setSeed(seed: String) {
        linuxCred.store(seed)
        getSeed()
    }

    override suspend fun clearSeed() {
        linuxCred.delete()
        getSeed()
    }

    private suspend fun getSeed() {
        try {
            _seedChannel.send(
                linuxCred.getSeed()
            )
        } catch (ex: Exception) {
            _seedChannel.send(null)
        }
    }

    companion object {
        private const val APP_NAME = "Atto Wallet"
    }
}