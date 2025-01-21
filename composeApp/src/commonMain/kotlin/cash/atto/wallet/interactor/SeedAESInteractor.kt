package cash.atto.wallet.interactor

expect class SeedAESInteractor {
    suspend fun encryptSeed(seed: String, password: String): String
    suspend fun decryptSeed(encryptedSeed: String, password: String): String
}