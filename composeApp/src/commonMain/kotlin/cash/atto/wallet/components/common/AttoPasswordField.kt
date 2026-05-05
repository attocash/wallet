package cash.atto.wallet.components.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.autofill.contentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_placeholder
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_text_muted
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary

@Composable
fun AttoPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Enter your password",
    revealed: Boolean = false,
    onRevealToggle: () -> Unit = {},
    isError: Boolean = false,
    contentType: ContentType = ContentType.Password,
    imeAction: ImeAction = ImeAction.Done,
    onDone: () -> Unit = {},
) {
    val textFieldState = rememberTextFieldState(initialText = value)
    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    LaunchedEffect(value) {
        if (value != textFieldState.text.toString()) {
            textFieldState.setTextAndPlaceCursorAtEnd(value)
        }
    }

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { text ->
                if (text != currentValue) {
                    currentOnValueChange(text)
                }
            }
    }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                color = dark_text_secondary,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 13.sp,
                    ),
            )
        }
        SecureTextField(
            state = textFieldState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .contentType(contentType)
                    .padding(top = if (label != null) 8.dp else 0.dp)
                    .border(
                        width = 1.dp,
                        color = if (isError) dark_danger else dark_border,
                        shape = RoundedCornerShape(8.dp),
                    ),
            isError = isError,
            shape = RoundedCornerShape(8.dp),
            textStyle =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    color = dark_text_primary,
                ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = dark_placeholder,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.W400,
                            fontSize = 14.sp,
                        ),
                )
            },
            trailingIcon = {
                Box(
                    modifier =
                        Modifier
                            .size(24.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onRevealToggle,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (revealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = dark_text_muted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = imeAction,
                ),
            onKeyboardAction =
                if (imeAction == ImeAction.Done) {
                    KeyboardActionHandler { onDone() }
                } else {
                    null
                },
            textObfuscationMode =
                if (revealed) {
                    TextObfuscationMode.Visible
                } else {
                    TextObfuscationMode.Hidden
                },
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
                    cursorColor = dark_accent,
                ),
        )
    }
}
