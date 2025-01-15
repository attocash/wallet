package cash.atto.wallet.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

actual class SeedDataSource {

    private val _seedChannel = Channel<String?>()
    actual val seed = _seedChannel.consumeAsFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            _seedChannel.send(null)
        }
    }

    actual suspend fun setSeed(seed: String) = _seedChannel.send(seed)
    actual suspend fun clearSeed() = _seedChannel.send(null)
}