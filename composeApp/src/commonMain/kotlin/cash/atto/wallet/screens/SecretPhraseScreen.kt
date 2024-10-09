package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.secret_backup
import attowallet.composeapp.generated.resources.secret_copy
import attowallet.composeapp.generated.resources.secret_title
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.secret.SecretPhraseGrid
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SecretPhraseScreen(
    onBackupConfirmClicked: () -> Unit
) {
    Scaffold(
        topBar = { AppBar() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Column(Modifier.fillMaxWidth().weight(1f)) {
                    Text(text = stringResource(Res.string.secret_title))

                    SecretPhraseGrid(
                        columns = 3,
                        words = (1..24)
                            .map { "Word$it" }
                            .toList()
                    )

                    Button(onClick = {}) {
                        Text(text = stringResource(Res.string.secret_copy))
                    }
                }

                Button(onClick = onBackupConfirmClicked) {
                    Text(text = stringResource(Res.string.secret_backup))
                }
            }
        }
    )
}

@Preview
@Composable
fun SecretPhraseScreenPreview() {
    AttoWalletTheme {
        SecretPhraseScreen(onBackupConfirmClicked = {})
    }
}