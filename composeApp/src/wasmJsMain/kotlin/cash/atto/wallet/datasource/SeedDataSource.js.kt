package cash.atto.wallet.datasource

actual class SeedDataSource {
    private val preferences = PreferencesDataSource()

    actual suspend fun getSeed(): String? =
        if (preferences.containsMnemonic()) preferences.getMnemonic() else BrowserStorage.get("mnemonic") ?: BrowserStorage.get("seed")

    actual suspend fun setSeed(seed: String) = BrowserStorage.withWalletLock { preferences.setMnemonicWithoutLock(seed) }

    actual suspend fun clearSeed() = BrowserStorage.withWalletLock { preferences.clearMnemonicWithoutLock() }
}
