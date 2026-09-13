package cash.atto.wallet.datasource

class SeedDataSourceWindows : SeedDataSourceDesktopImpl {
    private val winCred = WinCred()

    override suspend fun getSeed(): String? = winCred.getCredential(APP_NAME).ifEmpty { null }

    override suspend fun setSeed(seed: String) {
        winCred.setCredential(target = APP_NAME, userName = USERNAME, password = seed)
    }

    override suspend fun clearSeed() {
        winCred.deleteCredential(APP_NAME)
    }

    companion object {
        private const val APP_NAME = "Atto Wallet"
        private const val USERNAME = "Main Account"
    }
}
