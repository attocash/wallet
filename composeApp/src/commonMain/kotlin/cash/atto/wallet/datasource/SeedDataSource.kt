package cash.atto.wallet.datasource

import kotlinx.coroutines.flow.Flow

expect class SeedDataSource {
    val seed: Flow<String?>
    suspend fun setSeed(seed: String)
    suspend fun clearSeed()
}