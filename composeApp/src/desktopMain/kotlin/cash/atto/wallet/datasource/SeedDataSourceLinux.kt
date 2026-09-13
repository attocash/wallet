package cash.atto.wallet.datasource

class SeedDataSourceLinux : SeedDataSourceDesktopImpl {
    private val linuxCred = LinuxCred()

    override suspend fun getSeed(): String? = linuxCred.getSeed()

    override suspend fun setSeed(seed: String) {
        linuxCred.storeSeed(seed)
    }

    override suspend fun clearSeed() {
        linuxCred.deleteSeed()
    }
}
