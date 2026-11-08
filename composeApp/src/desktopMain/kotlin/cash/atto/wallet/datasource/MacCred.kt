@file:Suppress("FunctionName")

package cash.atto.wallet.datasource

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import java.nio.charset.StandardCharsets

interface Security : Library {
    companion object {
        val INSTANCE: Security = Native.load("Security", Security::class.java)
    }

    fun SecKeychainAddGenericPassword(
        keychain: Pointer?,
        serviceNameLength: Int,
        serviceName: String,
        accountNameLength: Int,
        accountName: String,
        passwordLength: Int,
        passwordData: ByteArray,
        itemRef: PointerByReference?,
    ): Int

    fun SecKeychainFindGenericPassword(
        keychain: Pointer?,
        serviceNameLength: Int,
        serviceName: String,
        accountNameLength: Int,
        accountName: String,
        passwordLength: IntArray?,
        passwordData: PointerByReference?,
        itemRef: PointerByReference?,
    ): Int

    fun SecKeychainItemDelete(itemRef: Pointer): Int

    fun SecKeychainItemFreeContent(
        attrList: Pointer?,
        data: Pointer?,
    ): Int
}

class MacCred {
    val serviceName = "Atto Wallet"

    fun getSeed() = get(SEED_ACCOUNT_NAME)

    fun storeSeed(seed: String) = store(SEED_ACCOUNT_NAME, seed)

    fun deleteSeed() = delete(SEED_ACCOUNT_NAME)

    fun getPassword() = get(PASSWORD_ACCOUNT_NAME)

    fun storePassword(password: String) = store(PASSWORD_ACCOUNT_NAME, password)

    fun deletePassword() = delete(PASSWORD_ACCOUNT_NAME)

    private fun store(
        accountName: String,
        password: String,
    ): Boolean {
        val passwordData = password.toByteArray(StandardCharsets.UTF_8)
        val result =
            Security.INSTANCE.SecKeychainAddGenericPassword(
                null,
                serviceName.length,
                serviceName,
                accountName.length,
                accountName,
                passwordData.size,
                passwordData,
                null,
            )
        if (result != 0) {
            throw IllegalStateException("Failed to store password. Error code: $result")
        }
        return true
    }

    private fun get(accountName: String): String? {
        val passwordLength = IntArray(1)
        val passwordData = PointerByReference()
        val itemRef = PointerByReference()

        val result =
            Security.INSTANCE.SecKeychainFindGenericPassword(
                null,
                serviceName.length,
                serviceName,
                accountName.length,
                accountName,
                passwordLength,
                passwordData,
                itemRef,
            )

        return if (result == 0) {
            val passwordBytes = passwordData.value.getByteArray(0, passwordLength[0])
            Security.INSTANCE.SecKeychainItemFreeContent(null, passwordData.value)
            String(passwordBytes, StandardCharsets.UTF_8)
        } else {
            null
        }
    }

    private fun delete(accountName: String): Boolean {
        val itemRef = PointerByReference()
        val result =
            Security.INSTANCE.SecKeychainFindGenericPassword(
                null,
                serviceName.length,
                serviceName,
                accountName.length,
                accountName,
                null,
                null,
                itemRef,
            )

        if (result != 0) {
            throw IllegalStateException("Failed to find item to delete. Error code: $result")
        }

        val deleteResult = Security.INSTANCE.SecKeychainItemDelete(itemRef.value)
        if (deleteResult != 0) {
            throw IllegalStateException("Failed to delete password. Error code: $deleteResult")
        }
        return true
    }

    companion object {
        // These existing Keychain account names must remain stable across wallet versions.
        private const val SEED_ACCOUNT_NAME = "atto_wallet_seed_schema"
        private const val PASSWORD_ACCOUNT_NAME = "atto_wallet_password_schema"
    }
}
