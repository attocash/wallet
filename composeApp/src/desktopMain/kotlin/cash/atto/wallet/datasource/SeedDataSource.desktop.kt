package cash.atto.wallet.datasource

import kotlinx.coroutines.flow.Flow

actual class SeedDataSource {

    private val dataSourceDesktopImpl: SeedDataSourceDesktopImpl

    init {
        dataSourceDesktopImpl = UnsafeSeedDataSource()
    }

    actual val seed: Flow<String?>
        get() = dataSourceDesktopImpl.seed

    actual suspend fun setSeed(seed: String) = dataSourceDesktopImpl.setSeed(seed)
    actual suspend fun clearSeed() = dataSourceDesktopImpl.clearSeed()
}