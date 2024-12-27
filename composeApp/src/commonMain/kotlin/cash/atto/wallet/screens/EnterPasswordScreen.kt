package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_welcome_background
import attowallet.composeapp.generated.resources.password_enter_hint
import attowallet.composeapp.generated.resources.password_enter_submit
import attowallet.composeapp.generated.resources.password_enter_title
import attowallet.composeapp.generated.resources.password_wrong
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoOnboardingContainer
import cash.atto.wallet.components.common.AttoTextField
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun EnterPassword(
    onSubmitPassword: (String?) -> Unit,
    passwordValid: Boolean = true
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
        EnterPasswordExpanded(
            onSubmitPassword = onSubmitPassword,
            passwordValid = passwordValid
        )
    } else {
        EnterPasswordCompact(
            onSubmitPassword = onSubmitPassword,
            passwordValid = passwordValid
        )
    }
}

@Composable
fun EnterPasswordCompact(
    onSubmitPassword: (String?) -> Unit,
    passwordValid: Boolean = true
) {
    val input = remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.password_enter_title),
                fontSize = 30.sp,
                fontWeight = FontWeight.W300,
                fontFamily = attoFontFamily(),
            )

            AttoTextField(
                value = input.value.orEmpty(),
                onValueChange = { input.value = it },
                placeholder = {
                    Text(text = stringResource(Res.string.password_enter_hint))
                },
                visualTransformation = PasswordVisualTransformation(),
                onDone = { onSubmitPassword.invoke(input.value) },
                isError = !passwordValid,
                errorLabel = {
                    Text(
                        text = stringResource(Res.string.password_wrong),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )

            Spacer(Modifier.weight(1f))
        }

        AttoButton(
            onClick = { onSubmitPassword.invoke(input.value) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.password_enter_submit))
        }
    }
}

@Composable
fun EnterPasswordExpanded(
    onSubmitPassword: (String?) -> Unit,
    passwordValid: Boolean = true
) {
    val input = remember {
        mutableStateOf<String?>(null)
    }

    Box(
        Modifier.fillMaxSize()
            .paint(
                painter = painterResource(Res.drawable.atto_welcome_background),
                contentScale = ContentScale.FillBounds
            )
    ) {
        AttoOnboardingContainer(
            modifier = Modifier.align(Alignment.Center)
                .width(560.dp)
        ) {
            Text(
                text = stringResource(Res.string.password_enter_title),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(Modifier.height(1.dp))

            AttoTextField(
                value = input.value.orEmpty(),
                onValueChange = { input.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = stringResource(Res.string.password_enter_hint))
                },
                visualTransformation = PasswordVisualTransformation(),
                onDone = { onSubmitPassword.invoke(input.value) },
                isError = !passwordValid,
                errorLabel = {
                    Text(
                        text = stringResource(Res.string.password_wrong),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )

            Spacer(Modifier.height(1.dp))

            AttoButton(
                onClick = { onSubmitPassword.invoke(input.value) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.password_enter_submit))
            }
        }
    }
}

@Preview
@Composable
fun EnterPasswordCompactPreview() {
    AttoWalletTheme {
        EnterPasswordCompact(
            onSubmitPassword = {}
        )
    }
}

@Preview
@Composable
fun EnterPasswordExpandedPreview() {
    AttoWalletTheme {
        EnterPasswordExpanded(
            onSubmitPassword = {}
        )
    }
}