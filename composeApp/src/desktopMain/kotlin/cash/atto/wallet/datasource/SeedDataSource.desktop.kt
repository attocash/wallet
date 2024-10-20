package cash.atto.wallet.datasource

import cash.atto.wallet.PlatformType
import cash.atto.wallet.getPlatform
import kotlinx.coroutines.flow.Flow

actual class SeedDataSource {

    private val dataSourceDesktopImpl = when (getPlatform().type) {
        PlatformType.WINDOWS -> SeedDataSourceWindows()
        else -> UnsafeSeedDataSource()
    }

    actual val seed: Flow<String?>
        get() = dataSourceDesktopImpl.seed

    actual suspend fun setSeed(seed: String) = dataSourceDesktopImpl.setSeed(seed)
    actual suspend fun clearSeed() = dataSourceDesktopImpl.clearSeed()
}