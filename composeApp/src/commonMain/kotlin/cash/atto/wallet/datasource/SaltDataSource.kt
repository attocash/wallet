package cash.atto.wallet.datasource

expect class SaltDataSource {
    suspend fun get(): String
}