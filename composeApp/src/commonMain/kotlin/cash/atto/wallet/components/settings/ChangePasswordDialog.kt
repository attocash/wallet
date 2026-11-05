package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoPasswordField
import cash.atto.wallet.components.common.AttoPasswordForm
import cash.atto.wallet.components.common.AttoPasswordStrengthMeter
import cash.atto.wallet.components.common.AttoPasswordSubmitButton
import cash.atto.wallet.components.common.PasswordFormValues
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.viewmodel.ChangePasswordViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit) {
    val viewModel = koinViewModel<ChangePasswordViewModel>()
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var revealCurrent by remember { mutableStateOf(false) }
    var revealNew by remember { mutableStateOf(false) }
    var revealConfirmation by remember { mutableStateOf(false) }
    val checkPassword = remember { CheckPasswordInteractor() }
    val assessment = remember(newPassword) { checkPassword.assess(newPassword) }
    val mismatch = confirmation.isNotEmpty() && newPassword != confirmation
    val unchanged = newPassword.isNotEmpty() && newPassword == currentPassword
    val canSubmit = currentPassword.isNotEmpty() && assessment.isAcceptable &&
        newPassword == confirmation && !unchanged && !state.saving
    val dismiss = {
        if (!state.saving) {
            viewModel.reset()
            onDismiss()
        }
    }

    LaunchedEffect(Unit) { viewModel.reset() }
    LaunchedEffect(state.changed) {
        if (state.changed) {
            currentPassword = ""
            newPassword = ""
            confirmation = ""
        }
    }

    AttoModal(title = "Change Password", onDismiss = dismiss) {
        if (state.changed) {
            Text(
                text = "Your password has been changed.",
                color = dark_success,
                style = MaterialTheme.typography.bodyMedium,
            )
            AttoButton(text = "Close", onClick = dismiss, modifier = Modifier.fillMaxWidth())
        } else {
            Text(
                text = "Change the password used to unlock this wallet on this device.",
                color = dark_text_secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            AttoPasswordForm(
                id = "wallet-change-password",
                values = PasswordFormValues(newPassword, confirmation, currentPassword),
                onSubmit = { values ->
                    scope.launch {
                        viewModel.changePassword(values.currentPassword.orEmpty(), values.password, values.confirmation.orEmpty())
                    }
                },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AttoPasswordField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            viewModel.reset()
                        },
                        label = "Current password",
                        name = "current-password",
                        contentType = ContentType.Password,
                        revealed = revealCurrent,
                        onRevealToggle = { revealCurrent = !revealCurrent },
                        isError = state.incorrectCurrentPassword,
                        imeAction = ImeAction.Next,
                    )
                    AttoPasswordField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            viewModel.reset()
                        },
                        label = "New password",
                        placeholder = "Enter your new password",
                        contentType = ContentType.NewPassword,
                        revealed = revealNew,
                        onRevealToggle = { revealNew = !revealNew },
                        isError = unchanged,
                        imeAction = ImeAction.Next,
                    )
                    AttoPasswordStrengthMeter(assessment = assessment, isEmpty = newPassword.isEmpty())
                    AttoPasswordField(
                        value = confirmation,
                        onValueChange = {
                            confirmation = it
                            viewModel.reset()
                        },
                        label = "Confirm new password",
                        placeholder = "Re-enter your new password",
                        name = "confirm-password",
                        contentType = ContentType.NewPassword,
                        revealed = revealConfirmation,
                        onRevealToggle = { revealConfirmation = !revealConfirmation },
                        isError = mismatch,
                        imeAction = ImeAction.Done,
                        onDone = {
                            if (canSubmit) {
                                scope.launch { viewModel.changePassword(currentPassword, newPassword, confirmation) }
                            }
                        },
                    )
                    val message = when {
                        mismatch -> "The new passwords do not match."
                        unchanged -> "Choose a password different from your current password."
                        else -> state.error
                    }
                    if (message != null) {
                        Text(text = message, color = dark_danger, style = MaterialTheme.typography.bodySmall)
                    }
                    AttoPasswordSubmitButton(
                        text = if (state.saving) "Changing password…" else "Change password",
                        enabled = canSubmit,
                    )
                    AttoButton(
                        text = "Cancel",
                        onClick = dismiss,
                        modifier = Modifier.fillMaxWidth(),
                        variant = AttoButtonVariant.Outlined,
                        enabled = !state.saving,
                    )
                }
            }
        }
    }
}
