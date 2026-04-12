package cash.atto.wallet.datasource

class PasswordDataSourceLinux : PasswordDataSourceDesktopImpl {
    private val linuxCred = LinuxCred()

    override suspend fun getPassword(seed: String): String? =
        try {
            linuxCred.getPassword()
        } catch (ex: Exception) {
            null
        }

    override suspend fun setPassword(
        seed: String,
        password: String,
    ) {
        linuxCred.storePassword(password)
    }
}
