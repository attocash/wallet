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
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_welcome_background
import attowallet.composeapp.generated.resources.secret_backup
import attowallet.composeapp.generated.resources.secret_copy
import attowallet.composeapp.generated.resources.secret_title
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoOnboardingContainer
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.secret.SecretPhraseGridCompact
import cash.atto.wallet.components.secret.SecretPhraseGridExpanded
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.uistate.secret.SecretPhraseUiState
import cash.atto.wallet.viewmodel.SecretPhraseViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SecretPhraseScreen(
    onBackNavigation: () -> Unit,
    onBackupConfirmClicked: () -> Unit
) {
    val viewModel = koinViewModel<SecretPhraseViewModel>()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val uiState = viewModel.state.collectAsState()

    SecretPhrase(
        uiState = uiState.value,
        onBackNavigation = onBackNavigation,
        onBackupConfirmClicked = onBackupConfirmClicked,
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SecretPhrase(
    uiState: SecretPhraseUiState,
    onBackNavigation: () -> Unit,
    onBackupConfirmClicked: () -> Unit,
    onCopyClick: () -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
        SecretPhraseExpanded(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onBackupConfirmClicked = onBackupConfirmClicked,
            onCopyClick = onCopyClick
        )
    } else {
        SecretPhraseCompact(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onBackupConfirmClicked = onBackupConfirmClicked,
            onCopyClick = onCopyClick
        )
    }
}

@Composable
fun SecretPhraseCompact(
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
                    .padding(bottom = WindowInsets.systemBars
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
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.secret_title),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.W300,
                        fontFamily = attoFontFamily()
                    )

                    SecretPhraseGridCompact(
                        words = uiState.words,
                        hidden = uiState.hidden
                    )

                    AttoOutlinedButton(onClick = onCopyClick) {
                        Text(text = stringResource(Res.string.secret_copy))
                    }
                }

                AttoButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBackupConfirmClicked
                ) {
                    Text(text = stringResource(Res.string.secret_backup))
                }
            }
        }
    )
}

@Composable
fun SecretPhraseExpanded(
    uiState: SecretPhraseUiState,
    onBackNavigation: () -> Unit,
    onBackupConfirmClicked: () -> Unit,
    onCopyClick: () -> Unit
) {
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
                        .fillMaxWidth(0.7f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.secret_title),
                        style = MaterialTheme.typography.h4
                    )

                    Spacer(Modifier.height(8.dp))

                    SecretPhraseGridExpanded(
                        words = uiState.words,
                        hidden = uiState.hidden
                    )

                    Spacer(Modifier.height(12.dp))

                    AttoButton(
                        onClick = onBackupConfirmClicked,
                        modifier = Modifier.width(480.dp)
                    ) {
                        Text(text = stringResource(Res.string.secret_backup))
                    }

                    AttoOutlinedButton(
                        onClick = onCopyClick,
                        modifier = Modifier.width(480.dp)
                    ) {
                        Text(text = stringResource(Res.string.secret_copy))
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun SecretPhraseCompactPreview() {
    AttoWalletTheme {
        SecretPhraseCompact(
            uiState = SecretPhraseUiState.DEFAULT,
            onBackNavigation = {},
            onBackupConfirmClicked = {},
            onCopyClick = {}
        )
    }
}

@Preview
@Composable
fun SecretPhraseExpandedPreview() {
    AttoWalletTheme {
        SecretPhraseExpanded(
            uiState = SecretPhraseUiState.DEFAULT,
            onBackNavigation = {},
            onBackupConfirmClicked = {},
            onCopyClick = {}
        )
    }
}