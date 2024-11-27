package cash.atto.wallet.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_background_desktop
import attowallet.composeapp.generated.resources.secret_copy
import attowallet.composeapp.generated.resources.secret_title
import attowallet.composeapp.generated.resources.settings_backup_hide
import attowallet.composeapp.generated.resources.settings_backup_show
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.secret.SecretPhraseGridCompact
import cash.atto.wallet.components.secret.SecretPhraseGridExpanded
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.secret.SecretPhraseUiState
import cash.atto.wallet.viewmodel.BackupSecretViewModel
import org.jetbrains.compose.resources.painterResource
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun BackupSecretPhrase(
    uiState: SecretPhraseUiState,
    onBackNavigation: () -> Unit,
    onVisibilityToggled: () -> Unit,
    onCopyClick: () -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
        BackupSecretPhraseExtended(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onVisibilityToggled = onVisibilityToggled,
            onCopyClick = onCopyClick
        )
    } else {
        BackupSecretPhraseCompact(
            uiState = uiState,
            onBackNavigation = onBackNavigation,
            onVisibilityToggled = onVisibilityToggled,
            onCopyClick = onCopyClick
        )
    }
}

@Composable
fun BackupSecretPhraseCompact(
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

                SecretPhraseGridCompact(
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

@Composable
fun BackupSecretPhraseExtended(
    uiState: SecretPhraseUiState,
    onBackNavigation: () -> Unit,
    onVisibilityToggled: () -> Unit,
    onCopyClick: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        modifier = Modifier.paint(
            painter = painterResource(Res.drawable.atto_background_desktop),
            contentScale = ContentScale.FillBounds
        ),
        backgroundColor = Color.Transparent,
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(50.dp))
                        .background(color = MaterialTheme.colors.surface)
                        .padding(
                            start = 108.dp,
                            top = 84.dp,
                            end = 108.dp,
                            bottom = 56.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.secret_title),
                        style = MaterialTheme.typography.h4
                    )

                    Spacer(Modifier.height(24.dp))

                    SecretPhraseGridExpanded(
                        words = uiState.words,
                        hidden = uiState.hidden
                    )

                    Spacer(Modifier.height(20.dp))

                    AttoOutlinedButton(
                        onClick = onVisibilityToggled,
                        modifier = Modifier.width(300.dp)
                    ) {
                        Text(text = stringResource(
                            if (uiState.hidden) Res.string.settings_backup_show
                            else Res.string.settings_backup_hide
                        ))
                    }

                    AttoButton(
                        onClick = onCopyClick,
                        modifier = Modifier.width(300.dp)
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
fun BackupSecretPhraseCompactPreview() {
    AttoWalletTheme {
        BackupSecretPhraseCompact(
            uiState = SecretPhraseUiState.DEFAULT,
            onBackNavigation = {},
            onVisibilityToggled = {},
            onCopyClick = {}
        )
    }
}

@Preview
@Composable
fun BackupSecretPhraseExtendedPreview() {
    AttoWalletTheme {
        BackupSecretPhraseExtended(
            uiState = SecretPhraseUiState.DEFAULT,
            onBackNavigation = {},
            onVisibilityToggled = {},
            onCopyClick = {}
        )
    }
}