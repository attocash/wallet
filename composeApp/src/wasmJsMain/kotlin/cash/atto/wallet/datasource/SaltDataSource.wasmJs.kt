package cash.atto.wallet.datasource

// Only legacy ciphertexts use this shared salt. New records contain their own salt.
actual class SaltDataSource {
    actual suspend fun get(): String? = BrowserStorage.get("salt")
}
