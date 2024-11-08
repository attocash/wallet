package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.secret_import_hint
import attowallet.composeapp.generated.resources.secret_import_title
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.secret.ImportSecretUiState
import cash.atto.wallet.viewmodel.ImportSecretViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ImportSecretScreen(
    onBackNavigation: () -> Unit,
    onImportAccount: () -> Unit
) {
    val viewModel = koinViewModel<ImportSecretViewModel>()
    val uiState = viewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    ImportSecret(
        uiState = uiState.value,
        onBackNavigation = onBackNavigation,
        onInputChanged = {
            coroutineScope.launch {
                viewModel.updateInput(it)
            }
        },
        onDoneClicked = {
            coroutineScope.launch {
                if (viewModel.importWallet())
                    onImportAccount.invoke()
            }
        }
    )
}

@Composable
fun ImportSecret(
    uiState: ImportSecretUiState,
    onBackNavigation: () -> Unit,
    onInputChanged: (String) -> Unit,
    onDoneClicked: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        backgroundColor = MaterialTheme.colors.surface,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                            + 16.dp
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.secret_import_title),
                        color = MaterialTheme.colors.primary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h5
                    )

                    Text(text = stringResource(Res.string.secret_import_hint))

                    TextField(
                        value = uiState.input.orEmpty(),
                        onValueChange = {
                            onInputChanged.invoke(it)
                        },
                        modifier = Modifier.onPreviewKeyEvent {
                            if (it.key.nativeKeyCode == Key.Enter.nativeKeyCode){
                                onDoneClicked.invoke()

                                return@onPreviewKeyEvent true
                            }

                            return@onPreviewKeyEvent false
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { onDoneClicked.invoke() }
                        )
                    )

                    if (!uiState.inputValid) {
                        Text(text = uiState.errorMessage.orEmpty())
                    }
                }

                FloatingActionButton(
                    onClick = onDoneClicked,
                    modifier = Modifier.align(Alignment.BottomEnd),
                    backgroundColor = MaterialTheme.colors.primary
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Done"
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun ImportSecretPreview() {
    AttoWalletTheme {
        ImportSecret(
            uiState = ImportSecretUiState(
                input = "ring mask spirit scissors best differ mean pet print century loyal major brain path already version jaguar rescue elder slender anxiety behind leg pigeon",
                errorMessage = "Input not valid"
            ),
            onBackNavigation = {},
            onInputChanged = {},
            onDoneClicked = {}
        )
    }
}