@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.HtmlElementView
import cash.atto.wallet.model.PasswordStrength
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.events.Event

@Composable
actual fun AttoPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    label: String?,
    placeholder: String,
    revealed: Boolean,
    onRevealToggle: () -> Unit,
    isError: Boolean,
    contentType: ContentType,
    name: String,
    imeAction: ImeAction,
    onDone: () -> Unit,
) {
    val form = LocalBrowserPasswordForm.current
    val visible = LocalBrowserPasswordControlsVisible.current
    val binding = remember(form, name, imeAction) { BrowserPasswordInput(form, name, value, onValueChange, imeAction) }
    val currentRevealToggle = rememberUpdatedState(onRevealToggle)
    val nativeLabel = remember(binding) { document.createElement("label") as HTMLLabelElement }
    val revealButton =
        remember(binding) {
            (document.createElement("button") as HTMLButtonElement).apply {
                type = "button"
                className = "atto-password-reveal"
            }
        }
    DisposableEffect(binding) {
        val keepInputFocus: (Event) -> Unit = { it.preventDefault() }
        val toggle: (Event) -> Unit = { currentRevealToggle.value() }
        revealButton.addEventListener("mousedown", keepInputFocus)
        revealButton.addEventListener("click", toggle)
        onDispose {
            revealButton.removeEventListener("mousedown", keepInputFocus)
            revealButton.removeEventListener("click", toggle)
            binding.dispose()
        }
    }

    val fieldModifier = modifier.fillMaxWidth().height(if (label == null) 56.dp else 80.dp)
    if (!visible) {
        // A hidden child leaves the interop wrapper intercepting clicks over Compose dialogs.
        Spacer(fieldModifier)
        return
    }

    HtmlElementView(
        factory = {
            (document.createElement("div") as HTMLDivElement).apply {
                className = "atto-password-field"
                nativeLabel.htmlFor = binding.element.id
                appendChild(nativeLabel)
                appendChild(
                    (document.createElement("div") as HTMLDivElement).apply {
                        className = "atto-password-control"
                        appendChild(binding.element)
                        appendChild(revealButton)
                    },
                )
            }
        },
        modifier = fieldModifier,
        update = { container ->
            binding.onValueChange = onValueChange
            binding.updateValue(value)
            binding.setRevealed(revealed)
            binding.element.autocomplete = if (contentType == ContentType.NewPassword) "new-password" else "current-password"
            if (contentType == ContentType.NewPassword) {
                binding.element.minLength = PasswordStrength.MINIMUM_LENGTH
                // Stricter generation hints do not impose composition rules on wallet acceptance.
                // Safari uses passwordrules; Chromium does not currently consume them.
                binding.element.setAttribute(
                    "passwordrules",
                    "minlength: 20; required: upper; required: lower; required: digit; required: special;",
                )
            } else {
                binding.element.removeAttribute("minlength")
                binding.element.removeAttribute("passwordrules")
            }
            binding.element.placeholder = placeholder
            binding.element.setAttribute("aria-label", label ?: placeholder)
            binding.element.setAttribute("aria-invalid", isError.toString())
            nativeLabel.textContent = label
            nativeLabel.hidden = label == null
            container.setAttribute("data-error", isError.toString())
            revealButton.setAttribute("aria-label", if (revealed) "Hide password" else "Show password")
            revealButton.setAttribute("aria-pressed", revealed.toString())
            revealButton.innerHTML = if (revealed) HIDE_PASSWORD_ICON else SHOW_PASSWORD_ICON
        },
    )
}

private const val SHOW_PASSWORD_ICON =
    """<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true">
    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5C21.27 7.61 17 4.5 12 4.5z
    M12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zM12 9c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
    </svg>"""

private const val HIDE_PASSWORD_ICON =
    """<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true">
    <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92A11.83 11.83 0 0 0 23 12
    c-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16A5.1 5.1 0 0 1 12 7zM2.71 3.16 1.29 4.57l2.5 2.5A11.82 11.82 0 0 0 1 12
    c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.83l3.05 3.04 1.41-1.41L2.71 3.16zM12 17a5 5 0 0 1-5-5c0-.77.18-1.5.49-2.15
    l1.57 1.57A3 3 0 0 0 12 15c.2 0 .39-.02.58-.06l1.57 1.57c-.65.31-1.38.49-2.15.49z"/>
    </svg>"""
