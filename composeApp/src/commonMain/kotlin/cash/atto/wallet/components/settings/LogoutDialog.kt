package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoCapsLabel
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoTextField
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var confirmationText by remember { mutableStateOf("") }
    val canConfirm = confirmationText == "WIPE"

    AttoModal(
        title = "Logout",
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Logging out clears the wallet keys stored on this device.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Make sure you have your recovery phrase saved before continuing.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AttoCapsLabel("Type WIPE to confirm")
        AttoTextField(
            value = confirmationText,
            onValueChange = { confirmationText = it },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            placeholder = {
                Text(
                    text = "WIPE",
                    color = Color.White.copy(alpha = 0.4f),
                )
            },
        )

        AttoButton(
            text = "Logout",
            variant = AttoButtonVariant.Danger,
            onClick = onConfirm,
            enabled = canConfirm,
            modifier = Modifier.fillMaxWidth(),
        )

        AttoButton(
            text = "Cancel",
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
