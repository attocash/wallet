package cash.atto.wallet.datasource

import kotlinx.coroutines.flow.Flow

actual class SeedDataSource {
    actual val seed: Flow<String?>
        get() = TODO("Not yet implemented")

    actual suspend fun setSeed(seed: String) {
    }

    actual suspend fun clearSeed() {
    }
}