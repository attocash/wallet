package cash.atto.wallet.datasource

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.ptr.PointerByReference

interface LibSecret : Library {
    companion object {
        val INSTANCE: LibSecret = Native.load("secret-1", LibSecret::class.java)
    }

    fun secret_password_store_sync(
        schema: Pointer,
        collection: String,
        label: String,
        password: String,
        cancellable: Pointer?,
        error: PointerByReference?,
        vararg attributes: String
    ): Boolean

    fun secret_password_lookup_sync(
        schema: Pointer,
        cancellable: Pointer?,
        error: PointerByReference?,
        vararg attributes: String
    ): String?

    fun secret_password_clear_sync(
        schema: Pointer,
        cancellable: Pointer?,
        error: PointerByReference?,
        vararg attributes: String
    ): Boolean
}


@Structure.FieldOrder("name", "flags", "attributes")
class SecretSchema(key: String) : Structure() {

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


class LinuxCred {
    private val seedSecretSchema = SecretSchema(SEED_SCHEMA_KEY)
    private val passwordSecretSchema = SecretSchema(PASSWORD_SCHEMA_KEY)

    fun getSeed() = get(seedSecretSchema)
    fun storeSeed(seed: String) = store(seedSecretSchema, seed)
    fun deleteSeed() = delete(seedSecretSchema)

    fun getPassword() = get(passwordSecretSchema)
    fun storePassword(password: String) = store(passwordSecretSchema, password)
    fun deletePassword() = delete(passwordSecretSchema)

    private fun get(schema: SecretSchema): String? {
        val error = PointerByReference()
        val result = LibSecret.INSTANCE.secret_password_lookup_sync(
            schema.pointer,
            null,
            error,
        )


        return result
    }

    private fun store(schema: SecretSchema, seed: String): Boolean {
        val error = PointerByReference()
        val result = LibSecret.INSTANCE.secret_password_store_sync(
            schema.pointer,
            "default",
            "Atto Wallet",
            seed,
            null,
            error,
        )

        if (get(schema) == null) {
            throw IllegalStateException("It wasn't possible to store the seed")
        }

        return result
    }

    private fun delete(schema: SecretSchema): Boolean {
        val error = PointerByReference()
        val result = LibSecret.INSTANCE.secret_password_clear_sync(
            schema.pointer,
            null,
            error,
        )

        if (get(schema) != null) {
            throw IllegalStateException("It wasn't possible to delete the seed")
        }

        return result
    }

    companion object {
        private const val SEED_SCHEMA_KEY = "seed"
        private const val PASSWORD_SCHEMA_KEY = "password"
    }
}