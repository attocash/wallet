package cash.atto.wallet.components.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_placeholder
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_text_primary
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AttoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder:
        @Composable()
        (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onDone: () -> Unit = {},
    isError: Boolean = false,
    singleLine: Boolean = false,
    submitOnEnterOrTab: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions(onDone = { onDone.invoke() }),
    trailingIcon:
        @Composable()
        (() -> Unit)? = null,
    errorLabel:
        @Composable()
        (BoxScope.() -> Unit) = {},
    supportingLabel: (@Composable BoxScope.() -> Unit)? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(
        horizontalAlignment = horizontalAlignment,
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                modifier
                    .border(
                        width = 1.dp,
                        color = if (isError) dark_danger else dark_border,
                        shape = RoundedCornerShape(8.dp),
                    ).onPreviewKeyEvent {
                        if (!submitOnEnterOrTab) {
                            return@onPreviewKeyEvent false
                        }
                        if (
                            it.key.keyCode == Key.Enter.keyCode ||
                            it.key.keyCode == Key.Tab.keyCode
                        ) {
                            onDone.invoke()

                            return@onPreviewKeyEvent true
                        }

                        return@onPreviewKeyEvent false
                    },
            placeholder = placeholder,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(8.dp),
            textStyle =
                MaterialTheme.typography.bodyLarge.copy(
                    color = dark_text_primary,
                    fontWeight = FontWeight.W500,
                ),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = dark_surface,
                    unfocusedContainerColor = dark_surface,
                    disabledContainerColor = dark_surface,
                    errorContainerColor = dark_surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedTextColor = dark_text_primary,
                    unfocusedTextColor = dark_text_primary,
                    errorTextColor = dark_text_primary,
                    focusedPlaceholderColor = dark_placeholder,
                    unfocusedPlaceholderColor = dark_placeholder,
                    errorPlaceholderColor = dark_placeholder,
                    cursorColor = dark_accent,
                ),
        )

        if (isError || supportingLabel != null) {
            Spacer(Modifier.height(4.dp))

            Box {
                if (supportingLabel != null) {
                    supportingLabel()
                } else {
                    errorLabel()
                }
            }
        }
    }
}

@Preview
@Composable
fun AttoTextFieldPreview() {
    AttoWalletTheme {
        AttoTextField(
            value = "Text",
            onValueChange = {},
        )
    }
}
