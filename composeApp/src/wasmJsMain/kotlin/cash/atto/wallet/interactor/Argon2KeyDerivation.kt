@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.interactor

import cash.atto.commons.toUint8Array
import cash.atto.wallet.repository.WalletStorageException
import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.khronos.webgl.Uint8Array
import org.w3c.dom.Worker
import kotlin.coroutines.resumeWithException

internal class Argon2KeyDerivation {
    // Bound the memory used by concurrent preference writes and authentication to one worker.
    private val mutex = Mutex()

    suspend fun derive(
        password: String,
        salt: ByteArray,
        iterations: Int,
        memoryKiB: Int,
        parallelism: Int,
    ): Uint8Array =
        mutex.withLock {
            try {
                val worker = PasswordCryptoWorker.createArgon2Worker()
                var timeout: Int? = null
                val passwordBytes = password.encodeToByteArray().toUint8Array()
                try {
                    suspendCancellableCoroutine { continuation ->
                        worker.onmessage = { event ->
                            if (continuation.isActive) {
                                val response = event.data?.unsafeCast<Argon2Response>()
                                val key = response?.key
                                if (response?.error == true || key == null || key.length != 32) {
                                    continuation.resumeWithException(WalletStorageException(ERROR_MESSAGE))
                                } else {
                                    continuation.resume(key, onCancellation = { _, value, _ -> clearBytes(value) })
                                }
                            }
                        }
                        worker.onerror = { event ->
                            event.preventDefault()
                            if (continuation.isActive) continuation.resumeWithException(WalletStorageException(ERROR_MESSAGE))
                        }
                        worker.addEventListener("messageerror", {
                            if (continuation.isActive) continuation.resumeWithException(WalletStorageException(ERROR_MESSAGE))
                        })
                        // A browser timer also runs independently of coroutine test clocks.
                        timeout =
                            window.setTimeout({
                                if (continuation.isActive) continuation.resumeWithException(WalletStorageException(ERROR_MESSAGE))
                                null
                            }, 30_000)
                        continuation.invokeOnCancellation { worker.terminate() }
                        worker.postMessage(argon2Request(passwordBytes, salt.toUint8Array(), iterations, memoryKiB, parallelism))
                    }
                } finally {
                    clearBytes(passwordBytes)
                    timeout?.let { window.clearTimeout(it) }
                    worker.terminate()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: WalletStorageException) {
                throw error
            } catch (error: Throwable) {
                throw WalletStorageException(ERROR_MESSAGE, error)
            }
        }

    private companion object {
        const val ERROR_MESSAGE = "Could not process wallet encryption. Please try again."
    }
}

@JsModule("password-crypto-worker")
private external object PasswordCryptoWorker : JsAny {
    fun createArgon2Worker(): Worker
}

private external interface Argon2Response : JsAny {
    val key: Uint8Array?
    val error: Boolean?
}

private fun argon2Request(
    password: Uint8Array,
    salt: Uint8Array,
    iterations: Int,
    memoryKiB: Int,
    parallelism: Int,
): JsAny = js("({password, salt, iterations, memoryKiB, parallelism})")

internal fun clearBytes(bytes: Uint8Array): Unit = js("bytes.fill(0)")
