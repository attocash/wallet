package cash.atto.wallet.repository

class WalletStorageException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
