package cash.atto.wallet.components.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.logout_cancel
import attowallet.composeapp.generated.resources.logout_confirm
import attowallet.composeapp.generated.resources.logout_text
import attowallet.composeapp.generated.resources.logout_title
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(Res.string.logout_confirm),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.logout_cancel),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        title = {
            Text(
                text = stringResource(Res.string.logout_title),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Text(text = stringResource(Res.string.logout_text))
        }
    )
}

@Preview
@Composable
fun LogoutDialogPreview() {
    AttoWalletTheme {
        LogoutDialog({}, {})
    }
}