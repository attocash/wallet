package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.logout_cancel
import attowallet.composeapp.generated.resources.logout_confirm
import attowallet.composeapp.generated.resources.logout_text
import attowallet.composeapp.generated.resources.logout_title
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AttoModal(
        title = stringResource(Res.string.logout_title),
        onDismiss = onDismiss,
        desktopWidth = 420.dp,
        showDivider = false,
        contentPadding = PaddingValues(24.dp),
    ) {
        Text(
            text = stringResource(Res.string.logout_text),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
        )

        AttoButton(
            text = stringResource(Res.string.logout_confirm),
            variant = AttoButtonVariant.Danger,
            onClick = onConfirm,
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
