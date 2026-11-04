@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.HtmlElementView
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement

internal val LocalBrowserPasswordControlsVisible = staticCompositionLocalOf { true }

internal val LocalBrowserPasswordForm = staticCompositionLocalOf<BrowserPasswordForm> { error("Password form is missing") }

@Composable
internal actual fun AttoPasswordForm(
    id: String,
    values: PasswordFormValues,
    onSubmit: (PasswordFormValues) -> Unit,
    obscured: Boolean,
    content: @Composable () -> Unit,
) {
    val form = remember(id) { BrowserPasswordForm(id, onSubmit) }
    SideEffect { form.onSubmit = onSubmit }
    DisposableEffect(form) {
        onDispose { form.dispose() }
    }
    LaunchedEffect(obscured) {
        if (obscured) {
            // Compose receives keys on its shadow-root canvas, outside the native controls.
            val canvas =
                document
                    .getElementById("AttoWallet")
                    ?.firstElementChild
                    ?.firstElementChild
                    ?.shadowRoot
                    ?.querySelector("canvas") as? HTMLElement
            canvas?.focus()
        }
    }

    // Inputs and submit buttons use form=id to retain one owner across Compose layout slots.
    HtmlElementView(
        factory = {
            form.element.apply { appendChild(createBrowserPasswordStyles()) }
        },
        modifier = Modifier.size(0.dp),
        onRelease = { form.dispose() },
    )
    CompositionLocalProvider(
        LocalBrowserPasswordForm provides form,
        LocalBrowserPasswordControlsVisible provides !obscured,
    ) {
        content()
    }
}

@Composable
internal actual fun AttoPasswordSubmitButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier,
) {
    val form = LocalBrowserPasswordForm.current
    val visible = LocalBrowserPasswordControlsVisible.current
    val buttonModifier = modifier.fillMaxWidth().height(56.dp)
    if (!visible) {
        Spacer(buttonModifier)
        return
    }
    HtmlElementView(
        factory = {
            (document.createElement("button") as HTMLButtonElement).apply {
                type = "submit"
                className = "atto-password-submit"
                setAttribute("form", form.element.id)
            }
        },
        modifier = buttonModifier,
        update = { button ->
            button.textContent = text
            button.disabled = !enabled
        },
    )
}
