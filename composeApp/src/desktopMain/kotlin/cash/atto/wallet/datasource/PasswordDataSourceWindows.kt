package cash.atto.wallet.datasource

class PasswordDataSourceWindows : PasswordDataSourceDesktopImpl {

    private val winCred = WinCred()

    override suspend fun getPassword(seed: String): String? {
        return try {
            winCred
                .getCredential("$APP_NAME${seed.hashCode()}")
                .ifEmpty { null }
        } catch (ex: Exception) {
            null
        }
    }

    override suspend fun setPassword(seed: String, password: String) {
        winCred.setCredential(
            target = "$APP_NAME${seed.hashCode()}",
            userName = "$APP_NAME${seed.hashCode()}",
            password = password
        )
    }

    companion object {
        private const val APP_NAME = "Atto Wallet"
    }
}