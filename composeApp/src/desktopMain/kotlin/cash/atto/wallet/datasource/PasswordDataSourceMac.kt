package cash.atto.wallet.datasource

/**
 * TODO: Create common interface for storage
 */
class PasswordDataSourceMac : PasswordDataSourceDesktopImpl {

    private val macCred = MacCred()

    override suspend fun getPassword(seed: String): String? {
        return try {
            macCred.getPassword()
        } catch (ex: Exception) {
            null
        }
    }

    override suspend fun setPassword(seed: String, password: String) {
        macCred.storePassword(password)
    }
}