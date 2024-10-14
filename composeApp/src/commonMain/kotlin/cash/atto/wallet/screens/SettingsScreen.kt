package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.settings.Profile
import cash.atto.wallet.components.settings.SettingsList
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.settings.SettingsUiState
import cash.atto.wallet.viewmodel.SettingsViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(onBackNavigation: () -> Unit) {
    KoinContext {
        val viewModel = koinViewModel<SettingsViewModel>()
        val uiState = viewModel.state.collectAsState()

        Settings(
            uiState = SettingsUiState(
                profileUiState = uiState.value.profileUiState,
                settingsListUiState = uiState.value.settingsListUiState
            ),
            onBackNavigation = onBackNavigation
        )
    }
}

@Composable
fun Settings(
    uiState: SettingsUiState,
    onBackNavigation: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        backgroundColor = MaterialTheme.colors.surface,
        content = {
            Column(Modifier.fillMaxSize()) {
                Profile(
                    modifier = Modifier.fillMaxWidth(),
                    uiState = uiState.profileUiState
                )

                SettingsList(
                    modifier = Modifier.fillMaxWidth(),
                    uiState = uiState.settingsListUiState
                )
            }
        }
    )
}

@Preview
@Composable
fun SettingsPreview() {
    AttoWalletTheme {
        Settings(SettingsUiState.PREVIEW) {}
    }
}