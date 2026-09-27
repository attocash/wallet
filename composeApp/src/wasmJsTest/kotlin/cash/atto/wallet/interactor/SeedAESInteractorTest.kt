package cash.atto.wallet.interactor

import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.interactor.EncryptedDataPurpose.PREFERENCES
import cash.atto.wallet.interactor.EncryptedDataPurpose.SEED
import cash.atto.wallet.support.ARGON2_MNEMONIC
import cash.atto.wallet.support.ARGON2_MNEMONIC_RECORD
import cash.atto.wallet.support.ARGON2_PREFERENCES
import cash.atto.wallet.support.ARGON2_PREFERENCES_RECORD
import cash.atto.wallet.support.LEGACY_SALT
import cash.atto.wallet.support.decryptEnvelopeIndependently
import cash.atto.wallet.support.encryptLegacy
import cash.atto.wallet.support.maximumArgon2Workers
import cash.atto.wallet.support.observeArgon2Workers
import cash.atto.wallet.support.restoreArgon2Workers
import kotlinx.browser.localStorage
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SeedAESInteractorTest {
    private val crypto = SeedAESInteractor(SaltDataSource())

    @Test
    fun `decryption uses persisted Argon2 parameters and reencryption uses current defaults`() =
        runTest {
            // Given: independent records with different iteration, memory, and lane counts.
            val fixtures =
                listOf(
                    Triple(ARGON2_MNEMONIC_RECORD, SEED, ARGON2_MNEMONIC),
                    Triple(ARGON2_PREFERENCES_RECORD, PREFERENCES, ARGON2_PREFERENCES),
                )
            for ((record, purpose, plaintext) in fixtures) {
                // When
                val decrypted = assertNotNull(crypto.decryptSeed(record, PASSWORD, purpose))
                val rewritten = crypto.encryptSeed(decrypted.plaintext, PASSWORD, purpose)
                val fields = Json.parseToJsonElement(rewritten).jsonObject

                // Then
                assertEquals(plaintext, decrypted.plaintext)
                assertFalse(decrypted.needsMigration)
                assertEquals("3", fields.getValue("iterations").jsonPrimitive.content)
                assertEquals("65536", fields.getValue("memoryKiB").jsonPrimitive.content)
                assertEquals("1", fields.getValue("parallelism").jsonPrimitive.content)
                assertEquals(plaintext, decryptEnvelopeIndependently(rewritten, PASSWORD))
            }
        }

    @Test
    fun `new records use the stronger self contained format and interoperate with WebCrypto`() =
        runTest {
            // Given
            localStorage.removeItem("salt")
            val plaintext = "Synthetic recovery data with unicode: áβ🙂"

            // When
            val record = crypto.encryptSeed(plaintext, PASSWORD, SEED)
            val fields = Json.parseToJsonElement(record).jsonObject
            val decrypted = assertNotNull(crypto.decryptSeed(record, PASSWORD, SEED))

            // Then
            assertEquals("2", fields.getValue("version").jsonPrimitive.content)
            assertEquals("Argon2id", fields.getValue("kdf").jsonPrimitive.content)
            assertEquals("3", fields.getValue("iterations").jsonPrimitive.content)
            assertEquals("65536", fields.getValue("memoryKiB").jsonPrimitive.content)
            assertEquals("1", fields.getValue("parallelism").jsonPrimitive.content)
            assertEquals("19", fields.getValue("argonVersion").jsonPrimitive.content)
            assertEquals("seed", fields.getValue("purpose").jsonPrimitive.content)
            assertEquals(16, Base64.decode(fields.getValue("salt").jsonPrimitive.content).size)
            assertEquals(12, Base64.decode(fields.getValue("iv").jsonPrimitive.content).size)
            assertEquals(plaintext, decrypted.plaintext)
            assertFalse(decrypted.needsMigration)
            assertEquals(plaintext, decryptEnvelopeIndependently(record, PASSWORD))
            assertNull(localStorage.getItem("salt"))
        }

    @Test
    fun `repeated writes and different record purposes each receive a fresh salt and IV`() =
        runTest {
            // Given
            val plaintext = "Synthetic known preference text ".repeat(10)

            // When
            val records =
                listOf(SEED, SEED, PREFERENCES, PREFERENCES).map { purpose ->
                    Json.parseToJsonElement(crypto.encryptSeed(plaintext, PASSWORD, purpose)).jsonObject
                }

            // Then
            for (field in listOf("salt", "iv", "ciphertext")) {
                assertEquals(records.size, records.map { it.getValue(field).jsonPrimitive.content }.toSet().size)
            }
            val seedCipher = Base64.decode(records[0].getValue("ciphertext").jsonPrimitive.content)
            val preferenceCipher = Base64.decode(records[2].getValue("ciphertext").jsonPrimitive.content)
            val known = plaintext.encodeToByteArray()
            val recovered =
                ByteArray(known.size) { index ->
                    (seedCipher[index].toInt() xor preferenceCipher[index].toInt() xor known[index].toInt()).toByte()
                }
            assertNotEquals(plaintext, recovered.decodeToString())
        }

    @Test
    fun `wrong passwords swapped purposes and modified authenticated data are rejected`() =
        runTest {
            // Given
            val record = crypto.encryptSeed("Synthetic seed", PASSWORD, SEED)
            val fields = Json.parseToJsonElement(record).jsonObject

            // When
            val substituted = JsonObject(fields + ("purpose" to JsonPrimitive("preferences"))).toString()
            val ciphertext = Base64.decode(fields.getValue("ciphertext").jsonPrimitive.content)
            ciphertext[0] = (ciphertext[0].toInt() xor 1).toByte()
            val altered = JsonObject(fields + ("ciphertext" to JsonPrimitive(Base64.encode(ciphertext)))).toString()

            // Then
            assertNull(crypto.decryptSeed(record, "WrongPassword7!", SEED))
            assertNull(crypto.decryptSeed(record, PASSWORD, PREFERENCES))
            assertNull(crypto.decryptSeed(substituted, PASSWORD, PREFERENCES))
            assertNull(crypto.decryptSeed(altered, PASSWORD, SEED))
            for (change in listOf("iterations" to 4, "memoryKiB" to 32_768, "parallelism" to 2)) {
                val tampered = JsonObject(fields + (change.first to JsonPrimitive(change.second))).toString()
                assertNull(crypto.decryptSeed(tampered, PASSWORD, SEED))
            }
        }

    @Test
    fun `unsupported or malformed envelopes are rejected before starting a derivation worker`() =
        runTest {
            // Given
            localStorage.setItem("salt", LEGACY_SALT)
            val record = crypto.encryptSeed("Synthetic seed", PASSWORD, SEED)
            val fields = Json.parseToJsonElement(record).jsonObject
            val changes =
                listOf(
                    "version" to JsonPrimitive(3),
                    "kdf" to JsonPrimitive("PBKDF2-HMAC-SHA1"),
                    "iterations" to JsonPrimitive(0),
                    "iterations" to JsonPrimitive(11),
                    "iterations" to JsonPrimitive(10_000),
                    "iterations" to JsonPrimitive(Int.MAX_VALUE),
                    "memoryKiB" to JsonPrimitive(7),
                    "memoryKiB" to JsonPrimitive(262_145),
                    "memoryKiB" to JsonPrimitive(Int.MAX_VALUE),
                    "parallelism" to JsonPrimitive(0),
                    "parallelism" to JsonPrimitive(5),
                    "argonVersion" to JsonPrimitive(16),
                    "iv" to JsonPrimitive(Base64.encode(ByteArray(11))),
                    "salt" to JsonPrimitive(Base64.encode(ByteArray(15))),
                    "ciphertext" to JsonPrimitive("invalid!"),
                )

            observeArgon2Workers()
            try {
                // When / Then
                for (change in changes) {
                    assertNull(crypto.decryptSeed(JsonObject(fields + change).toString(), PASSWORD, SEED))
                }
                assertNull(crypto.decryptSeed("{broken", PASSWORD, SEED))
                assertNull(crypto.decryptSeed("{}", PASSWORD, SEED))
                assertNull(crypto.decryptSeed("invalid!", PASSWORD, SEED))
                assertEquals(0, maximumArgon2Workers())
            } finally {
                restoreArgon2Workers()
            }
        }

    @Test
    fun `legacy records remain readable without creating a missing legacy salt`() =
        runTest {
            // Given
            val plaintext = "Synthetic legacy recovery phrase"
            val record = encryptLegacy(plaintext, PASSWORD)
            localStorage.setItem("salt", LEGACY_SALT)

            // When
            val decrypted = assertNotNull(crypto.decryptSeed(record, PASSWORD, SEED))

            // Then
            assertEquals(plaintext, decrypted.plaintext)
            assertTrue(decrypted.needsMigration)
            assertNull(crypto.decryptSeed(record, "WrongPassword7!", SEED))
            localStorage.removeItem("salt")
            assertNull(crypto.decryptSeed(record, PASSWORD, SEED))
            assertNull(localStorage.getItem("salt"))
        }

    private companion object {
        const val PASSWORD = "Synthetic7!"
    }
}
