package cash.atto.wallet.interactor

enum class EncryptedDataPurpose(
    val value: String,
) {
    SEED("seed"),
    PREFERENCES("preferences"),
}
