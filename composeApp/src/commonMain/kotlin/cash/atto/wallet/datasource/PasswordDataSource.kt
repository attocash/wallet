package cash.atto.wallet.datasource

expect class PasswordDataSource {
    suspend fun getPassword(seed: String): String?
    suspend fun setPassword(seed: String, password: String)
}