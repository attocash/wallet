package cash.atto.wallet.components.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

private val LocalPasswordSubmit = staticCompositionLocalOf<() -> Unit> { error("Password form is missing") }

@Composable
internal actual fun AttoPasswordForm(
    id: String,
    values: PasswordFormValues,
    onSubmit: (PasswordFormValues) -> Unit,
    obscured: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalPasswordSubmit provides { onSubmit(values) }) {
        content()
    }
}

@Composable
internal actual fun AttoPasswordSubmitButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier,
) {
    AttoButton(text = text, enabled = enabled, modifier = modifier, onClick = LocalPasswordSubmit.current)
}
