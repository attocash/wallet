package cash.atto.wallet.datasource

actual class SeedDataSource {
    actual suspend fun getSeed(): String? = BrowserStorage.get("mnemonic") ?: BrowserStorage.get("seed")

    actual suspend fun setSeed(seed: String) {
        BrowserStorage.set("mnemonic", seed)
        BrowserStorage.remove("seed")
    }

    actual suspend fun clearSeed() {
        BrowserStorage.remove("seed")
        BrowserStorage.remove("mnemonic")
    }
}
