package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.secret_copy
import attowallet.composeapp.generated.resources.secret_title
import attowallet.composeapp.generated.resources.settings_backup_hide
import attowallet.composeapp.generated.resources.settings_backup_show
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.secret.SecretPhraseGrid
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.secret.SecretPhraseUiState
import cash.atto.wallet.viewmodel.BackupSecretViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BackupSecretPhraseScreen(
    onBackNavigation: () -> Unit
) {
    val viewModel = koinViewModel<BackupSecretViewModel>()
    val uiState = viewModel.state.collectAsState()

    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    BackupSecretPhrase(
        uiState = uiState.value,
        onBackNavigation = onBackNavigation,
        onVisibilityToggled = {
            if (uiState.value.hidden)
                viewModel.showSecretPhrase()
            else viewModel.hideSecretPhrase()
        },
        onCopyClick = {
            clipboardManager.setText(
                AnnotatedString(
                    uiState.value
                        .words
                        .joinToString(" ")
                )
            )
        }
    )
}

@Composable
fun BackupSecretPhrase(
    uiState: SecretPhraseUiState,
    onBackNavigation: () -> Unit,
    onVisibilityToggled: () -> Unit,
    onCopyClick: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        backgroundColor = MaterialTheme.colors.surface,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(
                        bottom = WindowInsets.systemBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                                + 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(Res.string.secret_title))

                SecretPhraseGrid(
                    columns = 3,
                    words = uiState.words,
                    hidden = uiState.hidden
                )

                AttoOutlinedButton(onClick = onVisibilityToggled) {
                    Text(text = stringResource(
                        if (uiState.hidden) Res.string.settings_backup_show
                        else Res.string.settings_backup_hide
                    ))
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = onCopyClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.secret_copy))
                }
            }
        }
    )
}

@Preview
@Composable
fun BackupSecretPhrasePreview() {
    AttoWalletTheme {
        BackupSecretPhrase(
            uiState = SecretPhraseUiState.DEFAULT,
            onBackNavigation = {},
            onVisibilityToggled = {},
            onCopyClick = {}
        )
    }
}