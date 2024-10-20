package cash.atto.wallet.datasource

import kotlinx.coroutines.flow.Flow

interface SeedDataSourceDesktopImpl {
    val seed: Flow<String?>
    suspend fun setSeed(seed: String)
    suspend fun clearSeed()
}