package cash.atto.wallet.datasource

interface PasswordDataSourceDesktopImpl {
    suspend fun getPassword(seed: String): String?
    suspend fun setPassword(seed: String, password: String)
}