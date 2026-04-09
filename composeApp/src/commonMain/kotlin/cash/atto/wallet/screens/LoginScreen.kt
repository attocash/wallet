package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.password_wrong
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoPasswordField
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.ui.dark_bg
import cash.atto.wallet.ui.dark_border_subtle
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private val UnlockBackground = dark_bg
private val UnlockIconBackground = dark_surface
private val UnlockBorder = dark_border_subtle
private val UnlockTextPrimary = dark_text_primary
private val UnlockTextSecondary = dark_text_secondary
private val UnlockDanger = Color(0xFFE56A6A)

@Composable
fun LoginScreen(
    onSubmitPassword: (String?) -> Unit,
    passwordValid: Boolean = true
) {
    var input by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UnlockBackground),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            val shellWidth = if (maxWidth > 480.dp) 480.dp else maxWidth

            Column(
                modifier = Modifier.width(shellWidth),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(UnlockIconBackground, RoundedCornerShape(16.dp))
                        .border(1.dp, UnlockBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Color(0xFFF4B620),
                        modifier = Modifier.size(29.dp)
                    )
                }

                Text(
                    text = "Unlock Wallet",
                    modifier = Modifier.padding(top = 24.dp),
                    color = UnlockTextPrimary,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 35.sp,
                        lineHeight = 39.sp,
                        letterSpacing = (-0.3).sp
                    )
                )

                Text(
                    text = "Your wallet is locked. Enter your password to continue.",
                    modifier = Modifier.padding(top = 9.dp),
                    color = UnlockTextSecondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W400,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AttoPasswordField(
                        value = input,
                        onValueChange = { input = it },
                        label = "Password",
                        placeholder = "Enter your password",
                        revealed = showPassword,
                        onRevealToggle = { showPassword = !showPassword },
                        isError = !passwordValid,
                        imeAction = ImeAction.Done,
                        onDone = { onSubmitPassword(input) },
                    )

                    if (!passwordValid) {
                        Text(
                            text = stringResource(Res.string.password_wrong),
                            color = UnlockDanger,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                AttoButton(
                    text = "Unlock",
                    onClick = { onSubmitPassword(input) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    color = UnlockBorder
                )

                Text(
                    text = "Forgot password? Use recovery phrase",
                    modifier = Modifier.padding(top = 11.dp),
                    color = UnlockTextSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    AttoWalletTheme {
        LoginScreen(onSubmitPassword = {})
    }
}
