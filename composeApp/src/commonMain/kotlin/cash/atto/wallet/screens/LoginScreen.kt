package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.password_wrong
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoCheckbox
import cash.atto.wallet.components.common.AttoPasswordField
import cash.atto.wallet.components.common.AttoRoundButton
import cash.atto.wallet.components.login.TermsAndConditionsDialog
import cash.atto.wallet.components.settings.LogoutDialog
import cash.atto.wallet.model.TermsAndConditions
import cash.atto.wallet.ui.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoginScreen(
    onSubmitPassword: (String?) -> Unit,
    passwordValid: Boolean = true,
    termsAndConditionsAccepted: Boolean = false,
    termsAndConditionsDate: String = TermsAndConditions.EFFECTIVE_DATE,
    onTermsAndConditionsAcceptedChange: (Boolean) -> Unit = {},
    onLogout: () -> Unit = {},
) {
    var input by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showTermsAndConditionsDialog by remember { mutableStateOf(false) }
    val canSubmit = termsAndConditionsAccepted

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(dark_bg),
    ) {
        AttoRoundButton(
            onClick = { showLogoutDialog = true },
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 12.dp,
                        end = 12.dp,
                    ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = "Logout",
                tint = dark_text_primary,
                modifier = Modifier.size(18.dp),
            )
        }

        BoxWithConstraints(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            val shellWidth = if (maxWidth > 480.dp) 480.dp else maxWidth

            Column(
                modifier = Modifier.width(shellWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(68.dp)
                            .background(dark_surface, RoundedCornerShape(16.dp))
                            .border(1.dp, dark_border_subtle, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = dark_accent,
                        modifier = Modifier.size(29.dp),
                    )
                }

                Text(
                    text = "Unlock Wallet",
                    modifier = Modifier.padding(top = 24.dp),
                    color = dark_text_primary,
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.W600,
                            fontSize = 35.sp,
                            lineHeight = 39.sp,
                            letterSpacing = (-0.3).sp,
                        ),
                )

                Text(
                    text = "Your wallet is locked. Enter your password to continue.",
                    modifier = Modifier.padding(top = 9.dp),
                    color = dark_text_secondary,
                    textAlign = TextAlign.Center,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.W400,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        ),
                )

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                        onDone = {
                            if (canSubmit) {
                                onSubmitPassword(input)
                            }
                        },
                    )

                    if (!passwordValid) {
                        Text(
                            text = stringResource(Res.string.password_wrong),
                            color = dark_danger,
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                ),
                        )
                    }

                    TermsAndConditionsAcceptanceRow(
                        accepted = termsAndConditionsAccepted,
                        onAcceptedChange = onTermsAndConditionsAcceptedChange,
                        onOpenTerms = { showTermsAndConditionsDialog = true },
                    )
                }

                AttoButton(
                    text = "Unlock",
                    onClick = { onSubmitPassword(input) },
                    enabled = canSubmit,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                )

                HorizontalDivider(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                    color = dark_border_subtle,
                )

                Text(
                    text = "Forgot your password? Log out and re-import your recovery phrase",
                    modifier = Modifier.padding(top = 11.dp),
                    color = dark_text_secondary,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                        ),
                )
            }
        }

        if (showTermsAndConditionsDialog) {
            TermsAndConditionsDialog(
                effectiveDate = termsAndConditionsDate,
                accepted = termsAndConditionsAccepted,
                onAccept = {
                    onTermsAndConditionsAcceptedChange(true)
                    showTermsAndConditionsDialog = false
                },
                onDismiss = { showTermsAndConditionsDialog = false },
            )
        }

        if (showLogoutDialog) {
            LogoutDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    onLogout()
                },
            )
        }
    }
}

@Composable
private fun TermsAndConditionsAcceptanceRow(
    accepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit,
) {
    val nextAccepted = !accepted

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AttoCheckbox(
            checked = accepted,
            onCheckedChange = onAcceptedChange,
            modifier = Modifier.padding(top = 1.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "I accept the ",
                    color = dark_text_secondary,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onAcceptedChange(nextAccepted) },
                            ),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        ),
                )

                Text(
                    text = "Terms and Conditions",
                    color = dark_accent,
                    modifier =
                        Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenTerms,
                            ),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.W600,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        ),
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
