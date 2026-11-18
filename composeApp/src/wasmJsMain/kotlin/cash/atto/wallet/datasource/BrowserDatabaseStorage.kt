@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.datasource

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.Worker
import org.w3c.dom.events.Event

internal object BrowserDatabaseStorage {
    private val temporary = MutableStateFlow(false)
    val isTemporary = temporary.asStateFlow()

    fun observe(worker: Worker) {
        worker.addEventListener("message", { event ->
            if (usesTemporaryStorage(event)) temporary.value = true
        })
    }
}

private fun usesTemporaryStorage(event: Event): Boolean = js("event.data?.data?.storageMode === 'temporary'")
