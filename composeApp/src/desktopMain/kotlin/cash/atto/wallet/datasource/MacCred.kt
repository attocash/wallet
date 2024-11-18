package cash.atto.wallet.datasource

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
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
        itemRef: PointerByReference?
    ): Int

    fun SecKeychainFindGenericPassword(
        keychain: Pointer?,
        serviceNameLength: Int,
        serviceName: String,
        accountNameLength: Int,
        accountName: String,
        passwordLength: IntArray?,
        passwordData: PointerByReference?,
        itemRef: PointerByReference?
    ): Int

    fun SecKeychainItemDelete(itemRef: Pointer): Int

    fun SecKeychainItemFreeContent(
        attrList: Pointer?,
        data: Pointer?
    ): Int
}


@Structure.FieldOrder("name", "flags", "attributes")
class MacSecretSchema(key: String) : Structure() {

    private val schemaName = "atto_wallet_${key}_schema"

    companion object {
        const val SECRET_SCHEMA_NONE = 0
        const val SECRET_SCHEMA_ATTRIBUTE_STRING = 0
    }

    @JvmField
    var name: String = schemaName

    @JvmField
    var flags: Int = SECRET_SCHEMA_NONE

    @JvmField
    var attributes: Array<Attribute> = arrayOf(
        Attribute("key_type", SECRET_SCHEMA_ATTRIBUTE_STRING),
        Attribute(key, SECRET_SCHEMA_ATTRIBUTE_STRING)
    )

    @Structure.FieldOrder("name", "type")
    class Attribute() : Structure() {
        @JvmField
        var name: String = ""  // Default value for name

        @JvmField
        var type: Int = 0  // Default value for type (0 for string)

        constructor(name: String, type: Int) : this() {
            this.name = name
            this.type = type
        }
    }

    init {
        this.write()  // Write the structure
    }
}


class MacCred() {
    val serviceName = "Atto Wallet"

    private val seedSecretSchema = SecretSchema(SEED_SCHEMA_KEY)
    private val passwordSecretSchema = SecretSchema(PASSWORD_SCHEMA_KEY)

    fun getSeed() = get(seedSecretSchema)
    fun storeSeed(seed: String) = store(seedSecretSchema, seed)
    fun deleteSeed() = delete(seedSecretSchema)

    fun getPassword() = get(passwordSecretSchema)
    fun storePassword(password: String) = store(passwordSecretSchema, password)
    fun deletePassword() = delete(passwordSecretSchema)


    fun store(schema: SecretSchema, password: String): Boolean {
        val passwordData = password.toByteArray(StandardCharsets.UTF_8)
        val result = Security.INSTANCE.SecKeychainAddGenericPassword(
            null,
            serviceName.length,
            serviceName,
            schema.name.length,
            schema.name,
            passwordData.size,
            passwordData,
            null
        )
        if (result != 0) {
            throw IllegalStateException("Failed to store password. Error code: $result")
        }
        return true
    }

    fun get(schema: SecretSchema): String? {
        val passwordLength = IntArray(1)
        val passwordData = PointerByReference()
        val itemRef = PointerByReference()

        val result = Security.INSTANCE.SecKeychainFindGenericPassword(
            null,
            serviceName.length,
            serviceName,
            schema.name.length,
            schema.name,
            passwordLength,
            passwordData,
            itemRef
        )

        return if (result == 0) {
            val passwordBytes = passwordData.value.getByteArray(0, passwordLength[0])
            Security.INSTANCE.SecKeychainItemFreeContent(null, passwordData.value)
            String(passwordBytes, StandardCharsets.UTF_8)
        } else {
            null
        }
    }

    fun delete(schema: SecretSchema): Boolean {
        val itemRef = PointerByReference()
        val result = Security.INSTANCE.SecKeychainFindGenericPassword(
            null,
            serviceName.length,
            serviceName,
            schema.name.length,
            schema.name,
            null,
            null,
            itemRef
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
        private const val SEED_SCHEMA_KEY = "seed"
        private const val PASSWORD_SCHEMA_KEY = "password"
    }
}