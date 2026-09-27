package cash.atto.wallet.datasource

import cash.atto.wallet.repository.WalletStorageException
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.w3c.dom.events.Event

internal object BrowserStorage {
    private val changes = MutableStateFlow(0L)

    fun get(key: String): String? = localStorage.getItem(key)

    fun set(
        key: String,
        value: String,
    ) {
        mutate {
            if (get(key) != value) localStorage.setItem(key, value)
        }
    }

    fun remove(key: String) {
        mutate { localStorage.removeItem(key) }
    }

    // Local writes and other tabs both invalidate readers; collection owns the browser listener.
    fun updates(): Flow<Unit> =
        callbackFlow {
            val listener: (Event) -> Unit = { trySend(Unit) }
            window.addEventListener("storage", listener)
            val localUpdates = launch { changes.collect { send(Unit) } }
            awaitClose {
                localUpdates.cancel()
                window.removeEventListener("storage", listener)
            }
        }

    private inline fun mutate(block: () -> Unit) {
        try {
            block()
            changes.value++
        } catch (error: Throwable) {
            throw WalletStorageException("Could not update wallet storage. Please try again.", error)
        }
    }
}
