package cash.atto.wallet.datasource

class PasswordDataSourceLinux : PasswordDataSourceDesktopImpl {

    private val linuxCred = LinuxCred()

    override suspend fun getPassword(seed: String): String? {
        TODO("Not yet implemented")
    }

    override suspend fun setPassword(seed: String, password: String) {
        TODO("Not yet implemented")
    }
}