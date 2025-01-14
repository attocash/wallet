package cash.atto.wallet.datasource

actual fun createAccountEntry(
    hash: ByteArray,
    publicKey: ByteArray,
    height: Long,
    entry: String
): AccountEntry {
    TODO("Not yet implemented")
}

actual fun createWork(
    publicKey: ByteArray,
    value: ByteArray
): Work {
    TODO("Not yet implemented")
}