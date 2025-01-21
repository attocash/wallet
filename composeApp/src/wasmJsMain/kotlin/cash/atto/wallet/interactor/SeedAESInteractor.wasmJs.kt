package cash.atto.wallet.interactor

import cash.atto.commons.toUint8Array
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.interactor.utils.CryptoKey
import cash.atto.wallet.interactor.utils.TextDecoder
import cash.atto.wallet.interactor.utils.getSubtleCryptoInstance
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

internal fun transformKeyAlgorithm(salt: Uint8Array): JsAny = js("""
    ({ 
        "name": "pbkdf2",
        "hash": "sha-256",
        "salt": salt,
        "iterations": 100000,
        "length": 256
    })
""")

internal fun deriveBitsAlgorithm(salt: Uint8Array): JsAny = js("""
    ({ 
        "name": "pbkdf2",
        "hash": "sha-256",
        "salt": salt,
        "iterations": 10000
    })
""")

internal fun generateCryptoKeyAlgorithm(): JsAny = js("""
    ("AES-GCM")
""")

internal fun encryptionAlgorithm(salt: Uint8Array): JsAny = js("""
    ({ 
        "name": "AES-GCM",
        "iv": salt
    })
""")

internal fun arrayBufferToBase64(buffer: Uint8Array): String = js("""
    {
      var binary = '';
      var bytes = new Uint8Array(buffer);
      var len = bytes.byteLength;
      for (var i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
      }
      return window.btoa(binary);
    }
""")

internal fun base64ToArrayBuffer(base64: String): Uint8Array = js("""
    {
      var binary_string = window.atob(base64);
      var len = binary_string.length;
      var bytes = new Uint8Array(len);
      for (var i = 0; i < len; i++) {
        bytes[i] = binary_string.charCodeAt(i);
      }
      return bytes.buffer;
    }
""")

actual class SeedAESInteractor(
    private val saltDataSource: SaltDataSource
) {

    actual suspend fun encryptSeed(seed: String, password: String): String {
        val salt = saltDataSource.get()
        val key = getCryptoKey(password)
        val crypto = getSubtleCryptoInstance()

        val encrypted = crypto.encrypt(
            algorithm = encryptionAlgorithm(salt.toByteArray().toUint8Array()),
            key = key,
            data = seed.toByteArray().toUint8Array()
        ).await<Uint8Array>()

        return arrayBufferToBase64(encrypted)
    }

    actual suspend fun decryptSeed(
        encryptedSeed: String,
        password: String
    ): String {
        try {
            val salt = saltDataSource.get()
            val key = getCryptoKey(password)
            val crypto = getSubtleCryptoInstance()

            val decrypted = crypto.decrypt(
                algorithm = encryptionAlgorithm(salt.toByteArray().toUint8Array()),
                key = key,
                data = base64ToArrayBuffer(encryptedSeed)
            ).await<Uint8Array>()

            return TextDecoder().decode(decrypted)
        }
        catch (ex: Throwable) {
            return ""
        }
    }

    private suspend fun getCryptoKey(password: String): CryptoKey {
        val salt = saltDataSource.get()
        val crypto = getSubtleCryptoInstance()

        val paddedKey = crypto.importKey(
            format = "raw",
            keyData = password.toByteArray().toUint8Array(),
            algorithm = transformKeyAlgorithm(salt.toByteArray().toUint8Array()),
            extractable = false,
            keyUsages = mapKeyUsages(arrayOf("deriveBits")),
        ).await<CryptoKey>()

        val derivedBits = crypto.deriveBits(
            algorithm = deriveBitsAlgorithm(salt.toByteArray().toUint8Array()),
            baseKey = paddedKey,
            128
        ).await<ArrayBuffer>()

        val cryptoKey = crypto.importKey(
            format = "raw",
            keyData = Uint8Array(derivedBits),
            algorithm = generateCryptoKeyAlgorithm(),
            extractable = true,
            keyUsages = mapKeyUsages(arrayOf("encrypt", "decrypt")),
        ).await<CryptoKey>()

        return cryptoKey
    }

    private fun mapKeyUsages(keyUsages: Array<String>): JsArray<JsString> =
        JsArray<JsString>().also {
            keyUsages.forEachIndexed { index, value ->
                it[index] = value.toJsString()
            }
        }
}