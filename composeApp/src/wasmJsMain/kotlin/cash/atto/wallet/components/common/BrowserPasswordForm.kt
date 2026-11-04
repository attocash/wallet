@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.components.common

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.events.Event

internal class BrowserPasswordForm(
    id: String,
    var onSubmit: (PasswordFormValues) -> Unit,
) {
    val element =
        (document.createElement("form") as HTMLFormElement).apply {
            this.id = id
            autocomplete = "on"
            noValidate = true
        }

    private val inputs = mutableListOf<BrowserPasswordInput>()
    private var disposed = false
    private var observerFrame: Int? = null
    private val submitListener: (Event) -> Unit = { event ->
        // Cancellation must happen before application validation or a callback can fail.
        event.preventDefault()
        if (!disposed) {
            onSubmit(
                PasswordFormValues(
                    password =
                        inputs
                            .firstOrNull { it.element.name == "password" }
                            ?.element
                            ?.value
                            .orEmpty(),
                    confirmation = inputs.firstOrNull { it.element.name == "confirm-password" }?.element?.value,
                ),
            )
        }
    }

    init {
        element.addEventListener("submit", submitListener)
        observeValues()
    }

    private fun observeValues() {
        observerFrame =
            window.requestAnimationFrame {
                observerFrame = null
                if (!disposed) {
                    // Password managers can change value without dispatching input or change.
                    inputs.toList().forEach { it.syncValue() }
                    if (!disposed) observeValues()
                }
            }
    }

    fun requestSubmit() {
        if (!disposed && element.isConnected) {
            requestNativeSubmit(element)
        }
    }

    internal fun register(input: BrowserPasswordInput) {
        check(!disposed) { "Cannot register an input with a disposed password form" }
        inputs.add(input)
    }

    internal fun unregister(input: BrowserPasswordInput) {
        inputs.remove(input)
    }

    internal fun focusNext(input: BrowserPasswordInput) {
        if (disposed) return
        val index = inputs.indexOf(input)
        if (index >= 0) {
            inputs
                .drop(index + 1)
                .firstOrNull { !it.element.disabled && it.element.isConnected }
                ?.element
                ?.focus()
        }
    }

    fun dispose() {
        if (disposed) return
        disposed = true
        observerFrame?.let { window.cancelAnimationFrame(it) }
        observerFrame = null
        inputs.toList().forEach { it.dispose() }
        element.removeEventListener("submit", submitListener)
        element.remove()
        onSubmit = {}
    }
}

private fun requestNativeSubmit(form: HTMLFormElement): Unit = js("form.requestSubmit()")
