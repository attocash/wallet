@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package cash.atto.wallet.components.login

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.HtmlElementView
import cash.atto.wallet.components.common.LocalBrowserPasswordControlsVisible
import cash.atto.wallet.components.common.LocalBrowserPasswordForm
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.events.Event

@Composable
internal actual fun PlatformTermsAcceptance(
    accepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit,
    onShowTerms: () -> Unit,
    modifier: Modifier,
) {
    val form = LocalBrowserPasswordForm.current
    val visible = LocalBrowserPasswordControlsVisible.current
    val currentAcceptedChange = rememberUpdatedState(onAcceptedChange)
    val currentShowTerms = rememberUpdatedState(onShowTerms)
    val changeListener: (Event) -> Unit =
        remember {
            { event ->
                val checkbox = event.currentTarget as HTMLInputElement
                currentAcceptedChange.value(checkbox.checked)
            }
        }
    val showTermsListener: (Event) -> Unit = remember { { currentShowTerms.value() } }

    val termsModifier = modifier.fillMaxWidth().height(20.dp)
    if (!visible) {
        Spacer(termsModifier)
        return
    }

    HtmlElementView(
        factory = {
            (document.createElement("div") as HTMLDivElement).apply {
                className = "atto-password-terms"
                appendChild(
                    (document.createElement("input") as HTMLInputElement).apply {
                        id = "${form.element.id}-terms-accepted"
                        name = "terms-accepted"
                        type = "checkbox"
                        className = "atto-password-terms-checkbox"
                        setAttribute("form", form.element.id)
                        setAttribute("aria-label", "I accept the Terms and Conditions")
                        addEventListener("change", changeListener)
                    },
                )
                appendChild(
                    (document.createElement("div") as HTMLDivElement).apply {
                        appendChild(
                            (document.createElement("label") as HTMLLabelElement).apply {
                                htmlFor = "${form.element.id}-terms-accepted"
                                textContent = "I accept the "
                            },
                        )
                        appendChild(
                            (document.createElement("button") as HTMLButtonElement).apply {
                                type = "button"
                                className = "atto-password-terms-link"
                                textContent = "Terms and Conditions"
                                addEventListener("click", showTermsListener)
                            },
                        )
                    },
                )
            }
        },
        modifier = termsModifier,
        update = { container ->
            (container.querySelector("input") as HTMLInputElement).checked = accepted
        },
        onRelease = { container ->
            (container.querySelector("input") as HTMLInputElement).removeEventListener("change", changeListener)
            (container.querySelector("button") as HTMLButtonElement).removeEventListener("click", showTermsListener)
        },
    )
}
