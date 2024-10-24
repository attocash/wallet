package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.main_nav_overview
import attowallet.composeapp.generated.resources.main_nav_receive
import attowallet.composeapp.generated.resources.main_nav_send
import cash.atto.wallet.components.NavigationDrawerItem
import cash.atto.wallet.components.PermanentNavigationDrawer
import cash.atto.wallet.components.settings.Profile
import cash.atto.wallet.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreenDesktop() {
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val settingsUiState = settingsViewModel.state.collectAsState()

    PermanentNavigationDrawer(
        drawerContent = {
            Column(Modifier.fillMaxWidth()) {
                Profile(
                    modifier = Modifier.fillMaxWidth(),
                    uiState = settingsUiState.value.profileUiState
                )

                NavigationDrawerItem(
                    label = {
                        Text(text = stringResource(Res.string.main_nav_overview))
                    },
                    selected = true,
                    onClick = {}
                )

                NavigationDrawerItem(
                    label = {
                        Text(text = stringResource(Res.string.main_nav_send))
                    },
                    selected = false,
                    onClick = {}
                )

                NavigationDrawerItem(
                    label = {
                        Text(text = stringResource(Res.string.main_nav_receive))
                    },
                    selected = false,
                    onClick = {}
                )
            }
        }
    ) {
        OverviewScreenDesktop {  }
    }
}