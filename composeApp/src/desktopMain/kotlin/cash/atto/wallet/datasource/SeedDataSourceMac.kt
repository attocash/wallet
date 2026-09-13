package cash.atto.wallet.datasource

class SeedDataSourceMac : SeedDataSourceDesktopImpl {
    private val macCred = MacCred()

    override suspend fun getSeed(): String? = macCred.getSeed()

    override suspend fun setSeed(seed: String) {
        macCred.storeSeed(seed)
    }

    override suspend fun clearSeed() {
        macCred.deleteSeed()
    }
}
