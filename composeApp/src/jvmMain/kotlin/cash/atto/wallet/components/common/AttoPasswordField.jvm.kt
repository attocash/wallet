package cash.atto.wallet.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.input.ImeAction

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
    ComposePasswordField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        revealed = revealed,
        onRevealToggle = onRevealToggle,
        isError = isError,
        contentType = contentType,
        imeAction = imeAction,
        onDone = onDone,
    )
}
