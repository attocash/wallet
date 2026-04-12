package cash.atto.wallet.datasource

actual class PasswordDataSource {
    private val passwordMap = HashMap<String, String>()

    actual suspend fun getPassword(seed: String): String? = passwordMap[seed]

    actual suspend fun setPassword(
        seed: String,
        password: String,
    ) {
        passwordMap[seed] = password
    }
}
