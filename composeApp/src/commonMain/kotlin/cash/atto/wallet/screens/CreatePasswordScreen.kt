package cash.atto.wallet.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoPasswordField
import cash.atto.wallet.components.common.AttoScreenSubtitle
import cash.atto.wallet.components.common.AttoScreenTitle
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.dark_bg
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_text_muted
import cash.atto.wallet.ui.dark_text_primary
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
    val coroutineScope = rememberCoroutineScope()

    CreatePassword(
        uiState = uiState.value,
        onBackNavigation = onBackNavigation,
        onConfirmClick = {
            coroutineScope.launch {
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
    onConfirmClick: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmChanged: (String) -> Unit,
) {
    var showPassword by remember { mutableStateOf(false) }
    var showPasswordConfirm by remember { mutableStateOf(false) }
    val password = uiState.password.orEmpty()
    val passwordConfirm = uiState.passwordConfirm.orEmpty()

    val hasMinLength = password.length >= 8
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val allRequirementsMet = hasMinLength && hasUpperCase && hasLowerCase && hasNumber && hasSpecial
    val passwordsMatch = password.isNotEmpty() && passwordConfirm.isNotEmpty() && password == passwordConfirm
    val showMismatch = passwordConfirm.isNotEmpty() && !passwordsMatch
    val canProceed = allRequirementsMet && passwordsMatch
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

                        Column {
                            PasswordField(
                                label = stringResource(Res.string.password_confirm_label),
                                value = passwordConfirm,
                                placeholder = stringResource(Res.string.password_confirm_hint),
                                revealed = showPasswordConfirm,
                                onRevealToggle = { showPasswordConfirm = !showPasswordConfirm },
                                onValueChange = onPasswordConfirmChanged,
                                contentType = ContentType.NewPassword,
                                imeAction = ImeAction.Done,
                                onDone = {
                                    if (canProceed) {
                                        onConfirmClick()
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

                        PasswordRequirementsCard(
                            hasMinLength = hasMinLength,
                            hasUpperCase = hasUpperCase,
                            hasLowerCase = hasLowerCase,
                            hasNumber = hasNumber,
                            hasSpecial = hasSpecial,
                        )
                    }

                    AttoButton(
                        text = stringResource(Res.string.password_create_next),
                        onClick = onConfirmClick,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                        enabled = canProceed,
                    )
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

@Composable
private fun PasswordRequirementsCard(
    hasMinLength: Boolean,
    hasUpperCase: Boolean,
    hasLowerCase: Boolean,
    hasNumber: Boolean,
    hasSpecial: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(dark_surface, RoundedCornerShape(8.dp))
                .border(1.dp, dark_border, RoundedCornerShape(8.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.password_requirements_title),
            color = dark_text_secondary,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 13.sp,
                ),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PasswordRequirement(hasMinLength, stringResource(Res.string.password_requirement_length))
            PasswordRequirement(hasUpperCase, stringResource(Res.string.password_requirement_upper))
            PasswordRequirement(hasLowerCase, stringResource(Res.string.password_requirement_lower))
            PasswordRequirement(hasNumber, stringResource(Res.string.password_requirement_number))
            PasswordRequirement(hasSpecial, stringResource(Res.string.password_requirement_special))
        }
    }
}

@Composable
private fun PasswordRequirement(
    satisfied: Boolean,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(16.dp)
                    .background(
                        color = if (satisfied) dark_success else dark_border,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (satisfied) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = dark_text_primary,
                )
            }
        }

        Text(
            text = text,
            color = if (satisfied) dark_success else dark_text_muted,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.W400,
                    fontSize = 13.sp,
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
            onConfirmClick = {},
            onPasswordChanged = {},
            onPasswordConfirmChanged = {},
        )
    }
}
