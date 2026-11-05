package cash.atto.wallet.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.*
import cash.atto.wallet.components.common.AttoBackButton
import cash.atto.wallet.components.common.AttoPasswordStrengthMeter
import cash.atto.wallet.components.common.AttoPasswordField
import cash.atto.wallet.components.common.AttoPasswordForm
import cash.atto.wallet.components.common.AttoPasswordSubmitButton
import cash.atto.wallet.components.common.AttoScreenSubtitle
import cash.atto.wallet.components.common.AttoScreenTitle
import cash.atto.wallet.components.common.PasswordFormValues
import cash.atto.wallet.components.login.AttoTermsAcceptance
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.dark_bg
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.uistate.secret.CreatePasswordUIState
import cash.atto.wallet.viewmodel.CreatePasswordViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreatePasswordScreen(
    onBackNavigation: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    val viewModel = koinViewModel<CreatePasswordViewModel>()
    val uiState = viewModel.state.collectAsState()
    val termsAccepted = viewModel.termsAndConditionsAccepted.collectAsState(false)
    val coroutineScope = rememberCoroutineScope()

    CreatePassword(
        uiState = uiState.value,
        termsAndConditionsAccepted = termsAccepted.value,
        onTermsAndConditionsAcceptedChange = { accepted ->
            coroutineScope.launch { viewModel.setTermsAndConditionsAccepted(accepted) }
        },
        onBackNavigation = onBackNavigation,
        onConfirmClick = { password, confirmation ->
            coroutineScope.launch {
                viewModel.setPassword(password)
                viewModel.setPasswordConfirm(confirmation)
                if (viewModel.savePassword()) {
                    viewModel.clearPassword()
                    onConfirmClick()
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
        },
    )
}

@Composable
fun CreatePassword(
    uiState: CreatePasswordUIState,
    onBackNavigation: () -> Unit,
    onConfirmClick: (String, String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmChanged: (String) -> Unit,
    termsAndConditionsAccepted: Boolean = false,
    onTermsAndConditionsAcceptedChange: (Boolean) -> Unit = {},
) {
    var showPassword by remember { mutableStateOf(false) }
    var showPasswordConfirm by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    val password = uiState.password.orEmpty()
    val passwordConfirm = uiState.passwordConfirm.orEmpty()

    val checkPassword = remember { CheckPasswordInteractor() }
    val assessment = remember(password) { checkPassword.assess(password) }
    val passwordsMatch = password.isNotEmpty() && passwordConfirm.isNotEmpty() && password == passwordConfirm
    val showMismatch = passwordConfirm.isNotEmpty() && !passwordsMatch
    val canProceed = assessment.isAcceptable && passwordsMatch && termsAndConditionsAccepted
    val scrollState = rememberScrollState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(dark_bg),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 40.dp,
                        bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 40.dp,
                        start = 24.dp,
                        end = 24.dp,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                val shellWidth = if (maxWidth > 576.dp) 576.dp else maxWidth

                AttoPasswordForm(
                    id = "wallet-create-password",
                    values = PasswordFormValues(password, passwordConfirm),
                    obscured = showTermsDialog,
                    onSubmit = { submitted ->
                        val confirmation = submitted.confirmation.orEmpty()
                        if (
                            termsAndConditionsAccepted &&
                            !showTermsDialog &&
                            submitted.password == confirmation &&
                            checkPassword.invoke(submitted.password) == CreatePasswordUIState.PasswordCheckState.VALID
                        ) {
                            onConfirmClick(submitted.password, confirmation)
                        }
                    },
                ) {
                    Column(
                        modifier = Modifier.width(shellWidth),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 32.dp),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            AttoBackButton(onClick = onBackNavigation)
                        }

                        AttoScreenTitle(
                            text = stringResource(Res.string.password_create_title),
                        )

                        AttoScreenSubtitle(
                            text = stringResource(Res.string.password_create_text),
                            modifier =
                                Modifier
                                    .padding(top = 12.dp)
                                    .padding(horizontal = 16.dp),
                        )

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                PasswordField(
                                    label = stringResource(Res.string.password_field_label),
                                    value = password,
                                    placeholder = stringResource(Res.string.password_create_hint),
                                    revealed = showPassword,
                                    onRevealToggle = { showPassword = !showPassword },
                                    onValueChange = onPasswordChanged,
                                    contentType = ContentType.NewPassword,
                                    imeAction = ImeAction.Next,
                                )
                                AttoPasswordStrengthMeter(
                                    assessment = assessment,
                                    isEmpty = password.isEmpty(),
                                )
                            }

                            Column {
                                PasswordField(
                                    label = stringResource(Res.string.password_confirm_label),
                                    value = passwordConfirm,
                                    placeholder = stringResource(Res.string.password_confirm_hint),
                                    revealed = showPasswordConfirm,
                                    onRevealToggle = { showPasswordConfirm = !showPasswordConfirm },
                                    onValueChange = onPasswordConfirmChanged,
                                    contentType = ContentType.NewPassword,
                                    name = "confirm-password",
                                    imeAction = ImeAction.Done,
                                    onDone = {
                                        if (canProceed) {
                                            onConfirmClick(password, passwordConfirm)
                                        }
                                    },
                                )

                                if (showMismatch || passwordsMatch) {
                                    PasswordMatchState(
                                        matches = passwordsMatch,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                            }
                        }

                        AttoTermsAcceptance(
                            accepted = termsAndConditionsAccepted,
                            onAcceptedChange = onTermsAndConditionsAcceptedChange,
                            onDialogVisibilityChanged = { showTermsDialog = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        )

                        AttoPasswordSubmitButton(
                            text = stringResource(Res.string.password_create_next),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                            enabled = canProceed,
                        )
                        uiState.storageError?.let { message ->
                            Text(
                                text = message,
                                color = dark_danger,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    placeholder: String,
    revealed: Boolean,
    onRevealToggle: () -> Unit,
    onValueChange: (String) -> Unit,
    contentType: ContentType,
    name: String = "password",
    imeAction: ImeAction,
    onDone: () -> Unit = {},
) {
    AttoPasswordField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        revealed = revealed,
        onRevealToggle = onRevealToggle,
        contentType = contentType,
        name = name,
        imeAction = imeAction,
        onDone = onDone,
    )
}

@Composable
private fun PasswordMatchState(
    matches: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (matches) Icons.Outlined.Check else Icons.Outlined.Close,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (matches) dark_success else dark_danger,
        )

        Text(
            text =
                if (matches) {
                    stringResource(Res.string.password_match)
                } else {
                    stringResource(Res.string.password_no_match)
                },
            color = if (matches) dark_success else dark_danger,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.W400,
                    fontSize = 12.sp,
                ),
        )
    }
}


@Preview
@Composable
fun CreatePasswordPreview() {
    AttoWalletTheme {
        CreatePassword(
            uiState = CreatePasswordUIState.DEFAULT,
            onBackNavigation = {},
            onConfirmClick = { _, _ -> },
            onPasswordChanged = {},
            onPasswordConfirmChanged = {},
        )
    }
}
