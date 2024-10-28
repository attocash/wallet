package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.settings.LogoutDialog
import cash.atto.wallet.components.settings.Profile
import cash.atto.wallet.components.settings.SettingsList
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.settings.SettingsUiState
import cash.atto.wallet.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreenAndroid(
    onBackNavigation: () -> Unit,
    onRepresentativeNavigation: () -> Unit,
    onLogoutNavigation: () -> Unit
) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(uiState.value.navigateToRepresentative) {
        if (uiState.value.navigateToRepresentative) {
            viewModel.handleRepresentativeNavigation()
            onRepresentativeNavigation.invoke()
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
        backgroundColor = MaterialTheme.colors.surface,
        content = { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Profile(
                    modifier = Modifier.fillMaxWidth(),
                    uiState = uiState.profileUiState
                )

                SettingsList(
                    modifier = Modifier.fillMaxWidth(),
                    uiState = uiState.settingsListUiState
                )
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