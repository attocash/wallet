package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_welcome_background
import attowallet.composeapp.generated.resources.password_create_next
import attowallet.composeapp.generated.resources.secret_import_hint
import attowallet.composeapp.generated.resources.secret_import_title
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoOnboardingContainer
import cash.atto.wallet.components.common.AttoTextField
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.uistate.secret.ImportSecretUiState
import cash.atto.wallet.viewmodel.ImportSecretViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ImportSecret(
    uiState: ImportSecretUiState,
    onBackNavigation: () -> Unit,
    onInputChanged: (String) -> Unit,
    onDoneClicked: () -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
        ImportSecretExpanded(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onInputChanged = onInputChanged,
            onDoneClicked = onDoneClicked
        )
    } else {
        ImportSecretCompact(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onInputChanged = onInputChanged,
            onDoneClicked = onDoneClicked
        )
    }
}

@Composable
fun ImportSecretCompact(
    uiState: ImportSecretUiState,
    onBackNavigation: () -> Unit,
    onInputChanged: (String) -> Unit,
    onDoneClicked: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        containerColor = MaterialTheme.colorScheme.surface,
        content = {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(
                        bottom = WindowInsets.systemBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                                + 16.dp
                    )
                    .padding(
                        start = 16.dp,
                        top = 20.dp,
                        end = 16.dp
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.secret_import_title),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.W300,
                        fontFamily = attoFontFamily()
                    )

                    Text(
                        text = stringResource(Res.string.secret_import_hint),
                        lineHeight = 22.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    AttoTextField(
                        value = uiState.input.orEmpty(),
                        onValueChange = {
                            onInputChanged.invoke(it)
                        },
                        modifier = Modifier.fillMaxWidth()
                            .padding(top = 8.dp),
                        onDone = { onDoneClicked.invoke() },
                        isError = !uiState.inputValid,
                        errorLabel = {
                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }

                Spacer(Modifier.weight(1f))

                AttoButton(
                    onClick = onDoneClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.password_create_next))
                }
            }
        }
    )
}

@Composable
fun ImportSecretExpanded(
    uiState: ImportSecretUiState,
    onBackNavigation: () -> Unit,
    onInputChanged: (String) -> Unit,
    onDoneClicked: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        modifier = Modifier.paint(
            painter = painterResource(Res.drawable.atto_welcome_background),
            contentScale = ContentScale.FillBounds
        ),
        containerColor = Color.Transparent,
        content = {
            Box(Modifier.fillMaxSize()) {
                AttoOnboardingContainer(
                    modifier = Modifier.align(Alignment.Center)
                        .width(560.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = stringResource(Res.string.secret_import_title),
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Text(
                        text = stringResource(Res.string.secret_import_hint),
                        modifier = Modifier.padding(top = 25.dp)
                    )

                    AttoTextField(
                        value = uiState.input.orEmpty(),
                        onValueChange = {
                            onInputChanged.invoke(it)
                        },
                        modifier = Modifier.fillMaxWidth()
                            .padding(top = 12.dp),
                        onDone = { onDoneClicked.invoke() },
                        isError = !uiState.inputValid,
                        errorLabel = {
                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )

                    AttoButton(
                        onClick = onDoneClicked,
                        modifier = Modifier.fillMaxWidth()
                            .padding(top = 40.dp)
                    ) {
                        Text(stringResource(Res.string.password_create_next))
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun ImportSecretCompactPreview() {
    AttoWalletTheme {
        ImportSecretCompact(
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

@Preview
@Composable
fun ImportSecretExpandedPreview() {
    AttoWalletTheme {
        ImportSecretExpanded(
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