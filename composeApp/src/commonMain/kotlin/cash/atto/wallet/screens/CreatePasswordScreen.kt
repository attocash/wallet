package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_welcome_background
import attowallet.composeapp.generated.resources.password_confirm_hint
import attowallet.composeapp.generated.resources.password_create_back
import attowallet.composeapp.generated.resources.password_create_hint
import attowallet.composeapp.generated.resources.password_create_next
import attowallet.composeapp.generated.resources.password_create_text
import attowallet.composeapp.generated.resources.password_create_title
import attowallet.composeapp.generated.resources.password_no_match
import attowallet.composeapp.generated.resources.password_weak
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoOnboardingContainer
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.common.AttoTextField
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.secret.CreatePasswordUIState
import cash.atto.wallet.viewmodel.CreatePasswordViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreatePasswordScreen(
    onBackNavigation: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val viewModel = koinViewModel<CreatePasswordViewModel>()
    val uiState = viewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    CreatePassword(
        uiState = uiState.value,
        onBackNavigation = onBackNavigation,
        onConfirmClick = {
            coroutineScope.launch {
                if (viewModel.savePassword()) {
                    viewModel.clearPassword()
                    onConfirmClick.invoke()
                }
            }
        },
        onPasswordChanged = {
            coroutineScope.launch {
                viewModel.setPassword(it)
            }
        },
        onPasswordConfirmChanged = {
            coroutineScope.launch {
                viewModel.setPasswordConfirm(it)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun CreatePassword(
    uiState: CreatePasswordUIState,
    onBackNavigation: () -> Unit,
    onConfirmClick: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmChanged: (String) -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
        CreatePasswordExpanded(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onConfirmClick = onConfirmClick,
            onPasswordChanged = onPasswordChanged,
            onPasswordConfirmChanged = onPasswordConfirmChanged
        )
    } else {
        CreatePasswordCompact(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onConfirmClick = onConfirmClick,
            onPasswordChanged = onPasswordChanged,
            onPasswordConfirmChanged = onPasswordConfirmChanged
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreatePasswordCompact(
    uiState: CreatePasswordUIState,
    onBackNavigation: () -> Unit,
    onConfirmClick: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmChanged: (String) -> Unit
) {
    val (focusRequester) = FocusRequester.createRefs()

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
                Text(
                    text = stringResource(Res.string.password_create_title),
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.h5
                )

                Text(
                    text = stringResource(Res.string.password_create_text),
                    textAlign = TextAlign.Center
                )

                AttoTextField(
                    value = uiState.password.orEmpty(),
                    onValueChange = { onPasswordChanged.invoke(it) },
                    placeholder = {
                        Text(text = stringResource(Res.string.password_create_hint))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    onDone = { focusRequester.requestFocus() }
                )

                AttoTextField(
                    value = uiState.passwordConfirm.orEmpty(),
                    onValueChange = { onPasswordConfirmChanged.invoke(it) },
                    modifier = Modifier.focusRequester(focusRequester),
                    placeholder = {
                        Text(text = stringResource(Res.string.password_confirm_hint))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    onDone = { onConfirmClick.invoke() },
                    isError = uiState.showError,
                    errorLabel = {
                        Text(
                            text = stringResource(
                                if (uiState.checkState == CreatePasswordUIState.PasswordCheckState.INVALID)
                                    Res.string.password_weak
                                else Res.string.password_no_match
                            ),
                            color = MaterialTheme.colors.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.caption
                        )
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                )

                Spacer(Modifier.weight(1f))

                AttoButton(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.password_create_next))
                }

                AttoOutlinedButton(
                    onClick = onBackNavigation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.password_create_back))
                }
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreatePasswordExpanded(
    uiState: CreatePasswordUIState,
    onBackNavigation: () -> Unit,
    onConfirmClick: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmChanged: (String) -> Unit
) {
    val (focusRequester) = FocusRequester.createRefs()

    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        modifier = Modifier.paint(
            painter = painterResource(Res.drawable.atto_welcome_background),
            contentScale = ContentScale.FillBounds
        ),
        backgroundColor = Color.Transparent,
        content = {
            Box(Modifier.fillMaxSize()) {
                AttoOnboardingContainer(
                    modifier = Modifier.align(Alignment.Center)
                        .width(560.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.password_create_title),
                        style = MaterialTheme.typography.h4
                    )

                    Spacer(Modifier.height(1.dp))

                    Text(stringResource(Res.string.password_create_text))

                    Spacer(Modifier.height(12.dp))

                    AttoTextField(
                        value = uiState.password.orEmpty(),
                        onValueChange = { onPasswordChanged.invoke(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = stringResource(Res.string.password_create_hint))
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        onDone = { focusRequester.requestFocus() }
                    )

                    AttoTextField(
                        value = uiState.passwordConfirm.orEmpty(),
                        onValueChange = { onPasswordConfirmChanged.invoke(it) },
                        modifier = Modifier.fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(text = stringResource(Res.string.password_confirm_hint))
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        onDone = { onConfirmClick.invoke() },
                        isError = uiState.showError,
                        errorLabel = {
                            Text(
                                text = stringResource(
                                    if (uiState.checkState == CreatePasswordUIState.PasswordCheckState.INVALID)
                                        Res.string.password_weak
                                    else Res.string.password_no_match
                                ),
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    AttoButton(
                        onClick = onConfirmClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(Res.string.password_create_next))
                    }

                    AttoOutlinedButton(
                        onClick = onBackNavigation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(Res.string.password_create_back))
                    }
                }
            }
        }
    )
}

@Composable
fun CreatePasswordCompactPreview() {
    AttoWalletTheme {
        CreatePasswordCompact(
            uiState = CreatePasswordUIState.DEFAULT,
            onBackNavigation = {},
            onConfirmClick = {},
            onPasswordChanged = {},
            onPasswordConfirmChanged = {}
        )
    }
}

@Composable
fun CreatePasswordExpandedPreview() {
    AttoWalletTheme {
        CreatePasswordExpanded(
            uiState = CreatePasswordUIState.DEFAULT,
            onBackNavigation = {},
            onConfirmClick = {},
            onPasswordChanged = {},
            onPasswordConfirmChanged = {}
        )
    }
}