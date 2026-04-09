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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_text_muted
import cash.atto.wallet.ui.dark_text_secondary

private val FieldContainer = dark_surface
private val FieldBorder = dark_border
private val FieldTextPrimary = Color.White
private val FieldTextSecondary = dark_text_secondary
private val FieldTextMuted = dark_text_muted
private val FieldCursor = Color(0xFFFAB005)

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
    imeAction: ImeAction = ImeAction.Done,
    onDone: () -> Unit = {},
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                color = FieldTextSecondary,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 13.sp
                )
            )
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (label != null) 8.dp else 0.dp)
                .border(1.dp, FieldBorder, RoundedCornerShape(8.dp)),
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                color = FieldTextPrimary
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFF505050),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp
                    )
                )
            },
            visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onRevealToggle
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (revealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = FieldTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldContainer,
                unfocusedContainerColor = FieldContainer,
                disabledContainerColor = FieldContainer,
                errorContainerColor = FieldContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedTextColor = FieldTextPrimary,
                unfocusedTextColor = FieldTextPrimary,
                errorTextColor = FieldTextPrimary,
                cursorColor = FieldCursor
            )
        )
    }
}
