package cash.atto.wallet.datasource

import cash.atto.commons.toHex
import cash.atto.commons.utils.SecureRandom
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

actual class SaltDataSource {

    actual suspend fun get(): String {
        var salt = localStorage[SALT_KEY]
        if (salt == null) {
            salt = SecureRandom.randomByteArray(16u).toHex()
            localStorage[SALT_KEY] = salt
        }

        return salt
    }

    companion object {
        private const val SALT_KEY = "salt"
    }
}