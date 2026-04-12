package cash.atto.wallet.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class SeedDataSourceWindows : SeedDataSourceDesktopImpl {
    private val winCred = WinCred()

    private val seedChannel = Channel<String?>()
    override val seed = seedChannel.consumeAsFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            getSeed()
        }
    }

    override suspend fun setSeed(seed: String) {
        winCred.setCredential(
            target = APP_NAME,
            userName = USERNAME,
            password = seed,
        )

        getSeed()
    }

    override suspend fun clearSeed() {
        winCred.deleteCredential(APP_NAME)
        getSeed()
    }

    private suspend fun getSeed() {
        try {
            seedChannel.send(
                winCred
                    .getCredential(APP_NAME)
                    .ifEmpty { null },
            )
        } catch (ex: Exception) {
            seedChannel.send(null)
        }
    }

    companion object {
        private const val APP_NAME = "Atto Wallet"
        private const val USERNAME = "Main Account"
    }
}
