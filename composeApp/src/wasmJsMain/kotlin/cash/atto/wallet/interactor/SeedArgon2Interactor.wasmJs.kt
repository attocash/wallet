package cash.atto.wallet.interactor

import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.interactor.utils.Argon2

actual class SeedArgon2Interactor(
    private val saltDataSource: SaltDataSource
) {
    actual suspend fun encryptSeed(seed: String, password: String): String {
        val salt = saltDataSource.get()
        val hash = Argon2().hash(password)
        println(hash)
        return ""
    }

    actual suspend fun decryptSeed(
        encryptedSeed: String,
        password: String
    ): String {
        return ""
    }
}