package cash.atto.wallet.datasource

actual class PasswordDataSource {
    actual suspend fun getPassword(seed: String): String? {
        TODO("Not yet implemented")
    }

    actual suspend fun setPassword(seed: String, password: String) {
    }
}