package cash.atto.wallet.components.settings

import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
                    style = MaterialTheme.typography.overline
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.logout_cancel),
                    style = MaterialTheme.typography.overline
                )
            }
        },
        title = {
            Text(
                text = stringResource(Res.string.logout_title),
                color = MaterialTheme.colors.primary
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