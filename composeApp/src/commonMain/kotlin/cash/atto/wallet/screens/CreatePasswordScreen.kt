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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.password_confirm_hint
import attowallet.composeapp.generated.resources.password_create_back
import attowallet.composeapp.generated.resources.password_create_hint
import attowallet.composeapp.generated.resources.password_create_next
import attowallet.composeapp.generated.resources.password_create_text
import attowallet.composeapp.generated.resources.password_create_title
import attowallet.composeapp.generated.resources.password_no_match
import attowallet.composeapp.generated.resources.password_weak
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.secret.CreatePasswordUIState
import cash.atto.wallet.viewmodel.CreatePasswordViewModel
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreatePassword(
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

                TextField(
                    value = uiState.password.orEmpty(),
                    onValueChange = { onPasswordChanged.invoke(it) },
                    modifier = Modifier.onPreviewKeyEvent {
                        if (
                            it.key.nativeKeyCode == Key.Enter.nativeKeyCode ||
                            it.key.nativeKeyCode == Key.Tab.nativeKeyCode
                        ){
                            focusRequester.requestFocus()

                            return@onPreviewKeyEvent true
                        }

                        return@onPreviewKeyEvent false
                    },
                    placeholder = {
                        Text(text = stringResource(Res.string.password_create_hint))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusRequester.requestFocus() }
                    )
                )

                TextField(
                    value = uiState.passwordConfirm.orEmpty(),
                    onValueChange = { onPasswordConfirmChanged.invoke(it) },
                    modifier = Modifier.focusRequester(focusRequester)
                        .onPreviewKeyEvent {
                            if (it.key.nativeKeyCode == Key.Enter.nativeKeyCode){
                                onConfirmClick.invoke()

                                return@onPreviewKeyEvent true
                            }

                            return@onPreviewKeyEvent false
                        },
                    placeholder = {
                        Text(text = stringResource(Res.string.password_confirm_hint))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onConfirmClick.invoke() }
                    )
                )

                if (uiState.showError) {
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
                }

                Spacer(Modifier.weight(1f))

                Button(
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

@Composable
fun CreatePasswordPreview() {
    AttoWalletTheme {
        CreatePassword(
            uiState = CreatePasswordUIState.DEFAULT,
            onBackNavigation = {},
            onConfirmClick = {},
            onPasswordChanged = {},
            onPasswordConfirmChanged = {}
        )
    }
}