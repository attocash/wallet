package cash.atto.wallet.datasource

import com.sun.jna.*
import com.sun.jna.ptr.PointerByReference
import java.io.UnsupportedEncodingException

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
class SecretSchema : Structure() {

    companion object {
        const val SECRET_SCHEMA_NONE = 0
        const val SECRET_SCHEMA_ATTRIBUTE_STRING = 0
        val SCHEMA_NAME = "my_app_private_key_schema"
    }

    @JvmField
    var name: String = SCHEMA_NAME

    @JvmField
    var flags: Int = SECRET_SCHEMA_NONE

    @JvmField
    var attributes: Array<Attribute> = arrayOf(
        Attribute("key_type", SECRET_SCHEMA_ATTRIBUTE_STRING),
        Attribute("seed", SECRET_SCHEMA_ATTRIBUTE_STRING)
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
    private val secretSchema = SecretSchema()


    fun getSeed(): String? {
        val error = PointerByReference()
        val result = LibSecret.INSTANCE.secret_password_lookup_sync(
            secretSchema.pointer,
            null,
            error,
            "key_type", "seed"
        )


        return result
    }

    fun store(seed: String): Boolean {
        val error = PointerByReference()
        val result = LibSecret.INSTANCE.secret_password_store_sync(
            secretSchema.pointer,
            "default",
            "Atto Wallet",
            seed,
            null,
            error,
            "key_type", "seed",
        )

        if (getSeed() == null) {
            throw IllegalStateException("It wasn't possible to store the seed")
        }

        return result
    }

    fun delete(): Boolean {
        val error = PointerByReference()
        val result = LibSecret.INSTANCE.secret_password_clear_sync(
            secretSchema.pointer,
            null,
            error,
            "key_type", "seed",
        )

        if (getSeed() != null) {
            throw IllegalStateException("It wasn't possible to delete the seed")
        }


        return result

    }
}
