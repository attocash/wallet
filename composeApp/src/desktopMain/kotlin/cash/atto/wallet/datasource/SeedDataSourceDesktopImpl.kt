package cash.atto.wallet.datasource

interface SeedDataSourceDesktopImpl {
    suspend fun getSeed(): String?

    suspend fun setSeed(seed: String)

    suspend fun clearSeed()
}
