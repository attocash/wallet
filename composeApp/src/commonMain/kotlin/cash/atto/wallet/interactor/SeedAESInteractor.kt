package cash.atto.wallet.interactor

expect class SeedAESInteractor {
    suspend fun encryptSeed(
        seed: String,
        password: String,
        purpose: EncryptedDataPurpose,
    ): String

    suspend fun decryptSeed(
        encryptedSeed: String,
        password: String,
        purpose: EncryptedDataPurpose,
    ): DecryptedWalletData?
}
