package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.welcome_create_wallet
import attowallet.composeapp.generated.resources.welcome_import_wallet
import attowallet.composeapp.generated.resources.welcome_message
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.ButtonShape
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WelcomeScreen(
    onCreateSecretClicked: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier
                .align(Alignment.Center),
            text = stringResource(Res.string.welcome_message)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onCreateSecretClicked
            ) {
                Text(stringResource(Res.string.welcome_create_wallet))
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            ) {
                Text(stringResource(Res.string.welcome_import_wallet))
            }
        }
    }
}

@Composable
@Preview
fun WelcomeScreenPreview() {
    AttoWalletTheme {
        WelcomeScreen(onCreateSecretClicked = {})
    }
}