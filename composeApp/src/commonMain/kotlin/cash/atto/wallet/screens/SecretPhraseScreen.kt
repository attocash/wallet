package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.secret_backup
import attowallet.composeapp.generated.resources.secret_copy
import attowallet.composeapp.generated.resources.secret_title
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.secret.SecretPhraseGrid
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.secret.SecretPhraseUiState
import cash.atto.wallet.viewmodel.SecretPhraseViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SecretPhraseScreen(
    onBackNavigation: () -> Unit,
    onBackupConfirmClicked: () -> Unit
) {
    KoinContext {
        val viewModel = koinViewModel<SecretPhraseViewModel>()
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        val uiState = viewModel.state.collectAsState()

        SecretPhrase(
            uiState = uiState.value,
            onBackNavigation = onBackNavigation,
            onBackupConfirmClicked = onBackupConfirmClicked,
            onCopyClick = { clipboardManager.setText(
                AnnotatedString(
                    uiState.value
                        .words
                        .joinToString(" ")
                )
            )}
        )
    }
}

@Composable
fun SecretPhrase(
    uiState: SecretPhraseUiState,
    onBackNavigation: () -> Unit,
    onBackupConfirmClicked: () -> Unit,
    onCopyClick: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        backgroundColor = MaterialTheme.colors.surface,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                            + 16.dp
                    )
            ) {
                Column(Modifier.fillMaxWidth().weight(1f)) {
                    Text(text = stringResource(Res.string.secret_title))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SecretPhraseGrid(
                            columns = 3,
                            words = uiState.words
                        )

                        AttoOutlinedButton(onClick = onCopyClick) {
                            Text(text = stringResource(Res.string.secret_copy))
                        }
                    }
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBackupConfirmClicked
                ) {
                    Text(text = stringResource(Res.string.secret_backup))
                }
            }
        }
    )
}

@Preview
@Composable
fun SecretPhrasePreview() {
    AttoWalletTheme {
        SecretPhrase(
            uiState = SecretPhraseUiState.DEFAULT,
            onBackNavigation = {},
            onBackupConfirmClicked = {},
            onCopyClick = {}
        )
    }
}