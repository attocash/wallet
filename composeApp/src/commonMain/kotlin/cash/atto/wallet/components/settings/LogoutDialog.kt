package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.logout_cancel
import attowallet.composeapp.generated.resources.logout_confirm
import attowallet.composeapp.generated.resources.logout_input_hint
import attowallet.composeapp.generated.resources.logout_input_label
import attowallet.composeapp.generated.resources.logout_text
import attowallet.composeapp.generated.resources.logout_text_recovery
import attowallet.composeapp.generated.resources.logout_title
import cash.atto.wallet.components.common.AttoCapsLabel
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoTextField
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var confirmationText by remember { mutableStateOf("") }
    val canConfirm = confirmationText == "WIPE"

    AttoModal(
        title = stringResource(Res.string.logout_title),
        onDismiss = onDismiss,
        desktopWidth = 420.dp,
        showDivider = false,
        contentPadding = PaddingValues(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.logout_text),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(Res.string.logout_text_recovery),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AttoCapsLabel(stringResource(Res.string.logout_input_label))
        AttoTextField(
            value = confirmationText,
            onValueChange = { confirmationText = it },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            placeholder = {
                Text(
                    text = stringResource(Res.string.logout_input_hint),
                    color = Color.White.copy(alpha = 0.4f),
                )
            },
        )

        AttoButton(
            text = stringResource(Res.string.logout_confirm),
            variant = AttoButtonVariant.Danger,
            onClick = onConfirm,
            enabled = canConfirm,
            modifier = Modifier.fillMaxWidth(),
        )

        AttoButton(
            text = stringResource(Res.string.logout_cancel),
            variant = AttoButtonVariant.Outlined,
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
fun LogoutDialogPreview() {
    AttoWalletTheme {
        LogoutDialog({}, {})
    }
}
