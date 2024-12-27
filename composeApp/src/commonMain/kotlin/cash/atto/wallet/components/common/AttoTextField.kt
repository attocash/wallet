package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AttoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable() (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onDone: () -> Unit = {},
    isError: Boolean = false,
    errorLabel: @Composable() (BoxScope.() -> Unit) = {},
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(
        horizontalAlignment = horizontalAlignment
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.onPreviewKeyEvent {
                if (
                    it.key.nativeKeyCode == Key.Enter.nativeKeyCode ||
                    it.key.nativeKeyCode == Key.Tab.nativeKeyCode
                ) {
                    onDone.invoke()

                    return@onPreviewKeyEvent true
                }

                return@onPreviewKeyEvent false
            },
            placeholder = placeholder,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onDone.invoke() }
            )
        )

        if (isError) {
            Spacer(Modifier.height(4.dp))

            Box(content = errorLabel)
        }
    }
}

@Preview
@Composable
fun AttoTextFieldPreview() {
    AttoWalletTheme {
        AttoTextField(
            value = "Text",
            onValueChange = {}
        )
    }
}