package cash.atto.wallet.interactor

import cash.atto.commons.toUint8Array
import cash.atto.commons.utils.SecureRandom
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.interactor.utils.CryptoKey
import cash.atto.wallet.interactor.utils.TextDecoder
import cash.atto.wallet.interactor.utils.getSubtleCryptoInstance
import cash.atto.wallet.repository.WalletStorageException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.await
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import kotlin.io.encoding.Base64

actual class SeedAESInteractor(
    private val saltDataSource: SaltDataSource,
) {
    private val json = Json
    private val argon2 = Argon2KeyDerivation()

    actual suspend fun encryptSeed(
        seed: String,
        password: String,
        purpose: EncryptedDataPurpose,
    ): String {
        val salt = SecureRandom.randomByteArray(16u)
        val iv = SecureRandom.randomByteArray(12u)
        val envelope =
            Envelope(
                version = 2,
                purpose = purpose.value,
                kdf = ARGON2,
                iterations = DEFAULT_ARGON2_ITERATIONS,
                salt = Base64.encode(salt),
                iv = Base64.encode(iv),
                ciphertext = "",
                argonVersion = 19,
                memoryKiB = DEFAULT_ARGON2_MEMORY_KIB,
                parallelism = DEFAULT_ARGON2_PARALLELISM,
            )
        val key = deriveEnvelopeKey(password, salt, envelope)
        val encrypted =
            getSubtleCryptoInstance()
                .encrypt(
                    algorithm = encryptionAlgorithm(iv.toUint8Array(), associatedData(envelope).toUint8Array()),
                    key = key,
                    data = seed.encodeToByteArray().toUint8Array(),
                ).await<Uint8Array>()
        currentCoroutineContext().ensureActive()
        return json.encodeToString(
            envelope.copy(ciphertext = arrayBufferToBase64(encrypted)),
        )
    }

    actual suspend fun decryptSeed(
        encryptedSeed: String,
        password: String,
        purpose: EncryptedDataPurpose,
    ): DecryptedWalletData? {
        // Legacy records are plain Base64. A malformed envelope must never fall back to the legacy KDF.
        return try {
            val legacy = !encryptedSeed.trimStart().startsWith("{")
            var needsMigration = legacy
            val decrypted =
                if (legacy) {
                    val salt = saltDataSource.get()?.encodeToByteArray() ?: return null
                    val key = derivePbkdf2Key(password, salt, 10_000, 128)
                    getSubtleCryptoInstance()
                        .decrypt(
                            algorithm = encryptionAlgorithm(salt.toUint8Array(), null),
                            key = key,
                            data = Base64.decode(encryptedSeed).toUint8Array(),
                        ).await<Uint8Array>()
                } else {
                    val envelope = json.decodeFromString<Envelope>(encryptedSeed)
                    validateEnvelope(envelope)
                    require(envelope.purpose == purpose.value)
                    // Different version-2 KDF settings do not trigger rewrites on unlock.
                    needsMigration = envelope.version < 2
                    val salt = Base64.decode(envelope.salt)
                    val iv = Base64.decode(envelope.iv)
                    val ciphertext = Base64.decode(envelope.ciphertext)
                    require(salt.size == 16 && iv.size == 12 && ciphertext.size >= 16)
                    val key = deriveEnvelopeKey(password, salt, envelope)
                    getSubtleCryptoInstance()
                        .decrypt(
                            algorithm = encryptionAlgorithm(iv.toUint8Array(), associatedData(envelope).toUint8Array()),
                            key = key,
                            data = ciphertext.toUint8Array(),
                        ).await<Uint8Array>()
                }
            currentCoroutineContext().ensureActive()
            DecryptedWalletData(TextDecoder().decode(decrypted), needsMigration = needsMigration)
        } catch (error: CancellationException) {
            throw error
        } catch (error: WalletStorageException) {
            throw error
        } catch (_: Throwable) {
            // WebCrypto authentication failures and malformed stored data are unlock rejections.
            currentCoroutineContext().ensureActive()
            null
        }
    }

    private fun validateEnvelope(envelope: Envelope) {
        when (envelope.version) {
            1 -> {
                require(
                    envelope.kdf == PBKDF2 &&
                        envelope.iterations == 600_000 &&
                        envelope.argonVersion == null &&
                        envelope.memoryKiB == null &&
                        envelope.parallelism == null,
                )
            }

            2 -> {
                require(envelope.kdf == ARGON2 && envelope.argonVersion == 19)
                val memoryKiB = requireNotNull(envelope.memoryKiB)
                val parallelism = requireNotNull(envelope.parallelism)
                require(envelope.iterations in 1..MAX_ARGON2_ITERATIONS)
                require(parallelism in 1..MAX_ARGON2_PARALLELISM)
                require(memoryKiB in (8 * parallelism)..MAX_ARGON2_MEMORY_KIB)
            }

            else -> {
                error("Unsupported encryption version")
            }
        }
    }

    private suspend fun deriveEnvelopeKey(
        password: String,
        salt: ByteArray,
        envelope: Envelope,
    ): CryptoKey {
        if (envelope.version == 1) return derivePbkdf2Key(password, salt, envelope.iterations, 256)
        val bytes =
            argon2.derive(
                password,
                salt,
                envelope.iterations,
                checkNotNull(envelope.memoryKiB),
                checkNotNull(envelope.parallelism),
            )
        return try {
            getSubtleCryptoInstance()
                .importKey(
                    format = "raw",
                    keyData = bytes,
                    algorithm = aesAlgorithm(),
                    extractable = false,
                    keyUsages = keyUsages("encrypt", "decrypt"),
                ).await<CryptoKey>()
        } finally {
            clearBytes(bytes)
        }
    }

    private suspend fun derivePbkdf2Key(
        password: String,
        salt: ByteArray,
        iterations: Int,
        bits: Int,
    ): CryptoKey {
        val crypto = getSubtleCryptoInstance()
        val baseKey =
            crypto
                .importKey(
                    format = "raw",
                    keyData = password.encodeToByteArray().toUint8Array(),
                    algorithm = pbkdf2Algorithm(),
                    extractable = false,
                    keyUsages = keyUsages("deriveBits"),
                ).await<CryptoKey>()
        val derived =
            crypto
                .deriveBits(
                    algorithm = derivationAlgorithm(salt.toUint8Array(), iterations),
                    baseKey = baseKey,
                    length = bits,
                ).await<ArrayBuffer>()
        return crypto
            .importKey(
                format = "raw",
                keyData = Uint8Array(derived),
                algorithm = aesAlgorithm(),
                extractable = false,
                keyUsages = keyUsages("encrypt", "decrypt"),
            ).await<CryptoKey>()
    }

    private fun associatedData(envelope: Envelope): ByteArray =
        when (envelope.version) {
            1 -> {
                "cash.atto.wallet:1:${envelope.purpose}:$PBKDF2:600000:AES-256-GCM"
            }

            else -> {
                "cash.atto.wallet:2:${envelope.purpose}:$ARGON2:${envelope.argonVersion}:" +
                    "${envelope.memoryKiB}:${envelope.iterations}:${envelope.parallelism}:AES-256-GCM"
            }
        }.encodeToByteArray()

    @Serializable
    private data class Envelope(
        val version: Int,
        val purpose: String,
        val kdf: String,
        val iterations: Int,
        val salt: String,
        val iv: String,
        val ciphertext: String,
        val argonVersion: Int? = null,
        val memoryKiB: Int? = null,
        val parallelism: Int? = null,
    )

    private companion object {
        const val PBKDF2 = "PBKDF2-HMAC-SHA256"
        const val ARGON2 = "Argon2id"
        const val DEFAULT_ARGON2_ITERATIONS = 3
        const val DEFAULT_ARGON2_MEMORY_KIB = 65_536
        const val DEFAULT_ARGON2_PARALLELISM = 1

        // Read limits bound unauthenticated resource requests independently of new-write defaults.
        // Keep previously issued settings readable when changing the defaults.
        const val MAX_ARGON2_ITERATIONS = 10
        const val MAX_ARGON2_MEMORY_KIB = 262_144
        const val MAX_ARGON2_PARALLELISM = 4
    }
}

private fun pbkdf2Algorithm(): JsAny = js("({name: 'PBKDF2'})")

private fun aesAlgorithm(): JsAny = js("({name: 'AES-GCM'})")

private fun derivationAlgorithm(
    salt: Uint8Array,
    iterations: Int,
): JsAny = js("({name: 'PBKDF2', hash: 'SHA-256', salt: salt, iterations: iterations})")

private fun encryptionAlgorithm(
    iv: Uint8Array,
    additionalData: Uint8Array?,
): JsAny = js("({name: 'AES-GCM', iv: iv, tagLength: 128, ...(additionalData ? {additionalData: additionalData} : {})})")

private fun arrayBufferToBase64(buffer: Uint8Array): String =
    js("window.btoa(Array.from(new Uint8Array(buffer), byte => String.fromCharCode(byte)).join(''))")

private fun keyUsages(vararg values: String): JsArray<JsString> =
    JsArray<JsString>().also { array ->
        values.forEachIndexed { index, value -> array[index] = value.toJsString() }
    }
