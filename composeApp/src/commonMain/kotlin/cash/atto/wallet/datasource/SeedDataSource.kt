package cash.atto.wallet.datasource

expect class SeedDataSource {
    suspend fun getSeed(): String?

    suspend fun setSeed(seed: String)

    suspend fun clearSeed()
}
