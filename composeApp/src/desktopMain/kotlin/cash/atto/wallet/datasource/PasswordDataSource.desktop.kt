package cash.atto.wallet.datasource

import cash.atto.wallet.PlatformType
import cash.atto.wallet.getPlatform

actual class PasswordDataSource {

    private val dataSourceDesktopImpl = when (getPlatform().type) {
        PlatformType.WINDOWS -> PasswordDataSourceWindows()
        PlatformType.LINUX -> PasswordDataSourceLinux()
        else -> throw UnsupportedOperationException("Unsupported platform ${getPlatform()}")
    }

    actual suspend fun getPassword(seed: String) =
        dataSourceDesktopImpl.getPassword(seed)

    actual suspend fun setPassword(seed: String, password: String) =
        dataSourceDesktopImpl.setPassword(seed, password)
}