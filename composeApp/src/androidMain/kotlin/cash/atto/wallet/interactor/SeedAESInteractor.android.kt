package cash.atto.wallet.interactor

// This is a stub since the class is not used by Android
actual class SeedAESInteractor {
    actual suspend fun encryptSeed(
        seed: String,
        password: String,
        purpose: EncryptedDataPurpose,
    ): String {
        TODO("Not yet implemented")
    }

    actual suspend fun decryptSeed(
        encryptedSeed: String,
        password: String,
        purpose: EncryptedDataPurpose,
    ): DecryptedWalletData? {
        TODO("Not yet implemented")
    }
}
