package cash.atto.wallet.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal data class PasswordFormValues(
    val password: String,
    val confirmation: String? = null,
)

// CMP-9861: Compose Web does not map password autofill semantics to native HTML inputs.
// Revisit after Compose Multiplatform 1.14 is released: verify browser autofill/save support,
// then simplify these platform boundaries back to shared Compose fields and buttons.
// https://youtrack.jetbrains.com/issue/CMP-9861
@Composable
internal expect fun AttoPasswordForm(
    id: String,
    values: PasswordFormValues,
    onSubmit: (PasswordFormValues) -> Unit,
    obscured: Boolean = false,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun AttoPasswordSubmitButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
)
