package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.settings.LogoutDialog
import cash.atto.wallet.components.settings.ProfileSmall
import cash.atto.wallet.components.settings.SettingsList
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.BottomSheetShape
import cash.atto.wallet.ui.primaryGradient
import cash.atto.wallet.uistate.settings.SettingsUiState
import cash.atto.wallet.viewmodel.SettingsViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreenAndroid(
    onBackNavigation: () -> Unit,
    onBackupSecretNavigation: () -> Unit,
    onLogoutNavigation: () -> Unit
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(uiState.value.navigateToBackup) {
        if (uiState.value.navigateToBackup) {
            viewModel.handleBackupNavigation()
            onBackupSecretNavigation.invoke()
        }
    }

    SettingsAndroid(
        uiState = uiState.value,
        onBackNavigation = onBackNavigation,
        onDismissLogout = { viewModel.hideLogoutDialog() },
        onConfirmLogout = {
            viewModel.logout()
            viewModel.hideLogoutDialog()
            onLogoutNavigation()
        }
    )
}

@Composable
fun SettingsAndroid(
    uiState: SettingsUiState,
    onBackNavigation: () -> Unit,
    onDismissLogout: () -> Unit,
    onConfirmLogout: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                colors = MaterialTheme.colorScheme.primaryGradient
            )
        ),
        containerColor = Color.Transparent,
        content = { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ProfileSmall(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    uiState = uiState.profileUiState
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 6.dp)
                        .clip(BottomSheetShape)
                        .background(color = MaterialTheme.colorScheme.secondary)
                ) {
                    SettingsList(
                        modifier = Modifier.fillMaxWidth(),
                        uiState = uiState.settingsListUiState,
                        contentPadding = PaddingValues(vertical = 16.dp)
                    )
                }
            }

            if (uiState.showLogoutDialog)
                LogoutDialog(
                    onDismiss = onDismissLogout,
                    onConfirm = onConfirmLogout
                )
        }
    )
}

@Preview
@Composable
fun SettingsAndroidPreview() {
    AttoWalletTheme {
        SettingsAndroid(
            uiState = SettingsUiState.PREVIEW,
            onBackNavigation = {},
            onDismissLogout = {},
            onConfirmLogout = {}
        )
    }
}