package cash.atto.wallet.components.common

import androidx.compose.ui.text.input.ImeAction
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

internal class BrowserPasswordInput(
    private val form: BrowserPasswordForm,
    name: String,
    initialValue: String,
    var onValueChange: (String) -> Unit,
    private val imeAction: ImeAction,
) {
    val element =
        (document.createElement("input") as HTMLInputElement).apply {
            id = "${this@BrowserPasswordInput.form.element.id}-$name"
            this.name = name
            type = "password"
            value = initialValue
            setAttribute("form", this@BrowserPasswordInput.form.element.id)
            setAttribute("enterkeyhint", if (imeAction == ImeAction.Next) "next" else "done")
            setAttribute("autocapitalize", "none")
            spellcheck = false
        }

    private var previousValue = initialValue
    private var lastNotifiedValue = initialValue
    private var disposed = false
    private val valueListener: (Event) -> Unit = { syncValue() }
    private val keyListener: (Event) -> Unit = { event ->
        val keyboardEvent = event as? KeyboardEvent
        if (!disposed && keyboardEvent?.key == "Enter" && !keyboardEvent.isComposing) {
            event.preventDefault()
            if (!keyboardEvent.repeat) {
                if (imeAction == ImeAction.Next) {
                    form.focusNext(this)
                } else {
                    form.requestSubmit()
                }
            }
        }
    }

    init {
        form.register(this)
        element.addEventListener("input", valueListener)
        element.addEventListener("change", valueListener)
        element.addEventListener("keydown", keyListener)
    }

    fun updateValue(value: String) {
        if (disposed || value == previousValue) return
        previousValue = value
        lastNotifiedValue = value
        // Unrelated recomposition must preserve autofill that emitted no input/change event.
        if (element.value != value) {
            element.value = value
        }
    }

    internal fun syncValue() {
        if (disposed) return
        val value = element.value
        if (value != lastNotifiedValue) {
            lastNotifiedValue = value
            onValueChange(value)
        }
    }

    fun setRevealed(revealed: Boolean) {
        if (disposed) return
        val type = if (revealed) "text" else "password"
        if (element.type == type) return
        val focused = document.activeElement == element
        val selectionStart = element.selectionStart
        val selectionEnd = element.selectionEnd
        val selectionDirection = element.selectionDirection
        element.type = type
        if (selectionStart != null && selectionEnd != null) {
            element.setSelectionRange(selectionStart, selectionEnd, selectionDirection ?: "none")
        }
        if (focused && document.activeElement != element) {
            element.focus()
        }
    }

    fun dispose() {
        if (disposed) return
        disposed = true
        element.removeEventListener("input", valueListener)
        element.removeEventListener("change", valueListener)
        element.removeEventListener("keydown", keyListener)
        element.value = ""
        previousValue = ""
        lastNotifiedValue = ""
        onValueChange = {}
        form.unregister(this)
        element.remove()
    }
}
