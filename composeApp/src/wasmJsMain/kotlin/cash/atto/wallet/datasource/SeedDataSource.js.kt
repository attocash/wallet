package cash.atto.wallet.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.browser.localStorage
import org.w3c.dom.get

actual class SeedDataSource {

    private val _seedChannel = Channel<String?>()
    actual val seed = _seedChannel.consumeAsFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            _seedChannel.send(localStorage[SEED_KEY])
        }
    }

    actual suspend fun setSeed(seed: String) {
        localStorage.setItem(SEED_KEY, seed)
        _seedChannel.send(seed)
    }

    actual suspend fun clearSeed() {
        localStorage.removeItem(SEED_KEY)
        _seedChannel.send(null)
    }

    companion object {
        private const val SEED_KEY = "seed"
    }
}