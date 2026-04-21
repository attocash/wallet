package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * Shared amount input field with ATTO/USD currency toggle chip.
 *
 * Shows an outlined text field with a trailing chip that toggles between
 * ATTO and USD input modes. When an amount is entered, the equivalent
 * value in the other currency is displayed below the field.
 */
@Composable
fun AttoAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    isUsdMode: Boolean,
    onToggleCurrency: () -> Unit,
    priceUsd: BigDecimal?,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "0.00",
    isError: Boolean = false,
    errorText: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    showEquivalent: Boolean = true,
    largeFontSize: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (label != null) {
            Text(
                text = label,
                color = dark_text_secondary,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600),
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(sanitizeAmountInput(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            isError = isError,
            trailingIcon = {
                Box(
                    modifier =
                        Modifier
                            .padding(end = 10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(dark_accent.copy(alpha = 0.12f))
                            .border(1.dp, dark_accent.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .clickable { onToggleCurrency() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = if (isUsdMode) "USD" else "ATTO",
                        color = dark_accent,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W700),
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(8.dp),
            textStyle =
                if (largeFontSize) {
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 32.sp,
                    )
                } else {
                    MaterialTheme.typography.bodyLarge
                },
            colors = attoAmountFieldColors(isError),
            supportingText =
                if (isError && errorText != null) {
                    { Text(text = errorText, color = dark_danger) }
                } else {
                    null
                },
        )

        if (showEquivalent && value.isNotBlank() && priceUsd != null && !isError) {
            val price = priceUsd.doubleValue(false)
            val amount = value.toDoubleOrNull()
            if (amount != null && price > 0) {
                val equivalent =
                    if (isUsdMode) {
                        "~ ${AttoFormatter.format(BigDecimal.fromDouble(amount / price))} ATTO"
                    } else {
                        "~ \$${AttoFormatter.format(BigDecimal.fromDouble(amount * price))} USD"
                    }
                Text(
                    text = equivalent,
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else if (showEquivalent) {
            Text(
                text = "",
                color = dark_text_secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun sanitizeAmountInput(value: String): String {
    val builder = StringBuilder()
    var hasDecimalSeparator = false

    value.forEach { character ->
        when {
            character.isDigit() -> {
                builder.append(character)
            }

            character == '.' && !hasDecimalSeparator -> {
                hasDecimalSeparator = true
                builder.append(character)
            }
        }
    }

    return builder.toString()
}

@Composable
fun attoAmountFieldColors(isError: Boolean) =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = dark_bg,
        unfocusedContainerColor = dark_bg,
        errorContainerColor = dark_bg,
        focusedBorderColor = if (isError) dark_danger else dark_accent,
        unfocusedBorderColor = if (isError) dark_danger else dark_border,
        errorBorderColor = dark_danger,
        focusedTextColor = dark_text_primary,
        unfocusedTextColor = dark_text_primary,
        errorTextColor = dark_text_primary,
        focusedPlaceholderColor = dark_text_secondary,
        unfocusedPlaceholderColor = dark_text_secondary,
        errorPlaceholderColor = dark_text_secondary,
        cursorColor = dark_accent,
    )
