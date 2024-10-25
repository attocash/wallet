package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.main_nav_overview
import attowallet.composeapp.generated.resources.main_nav_receive
import attowallet.composeapp.generated.resources.main_nav_send
import attowallet.composeapp.generated.resources.main_nav_settings
import cash.atto.wallet.MainScreenNavDestination
import cash.atto.wallet.components.ExpandableDrawerItem
import cash.atto.wallet.components.NavigationDrawerItem
import cash.atto.wallet.components.PermanentNavigationDrawer
import cash.atto.wallet.components.settings.LogoutDialog
import cash.atto.wallet.components.settings.Profile
import cash.atto.wallet.components.settings.SettingsList
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.settings.SettingsUiState
import cash.atto.wallet.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreenDesktop(
    onLogoutNavigation: () -> Unit
) {
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val settingsUiState = settingsViewModel.state.collectAsState()


    val navState = remember {
        mutableStateOf(MainScreenNavDestination.OVERVIEW)
    }

    MainScreenContent(
        settingsUiState = settingsUiState.value,
        navState = navState.value,
        onNavStateChanged = { navState.value = it },
        onDismissLogout = { settingsViewModel.hideLogoutDialog() },
        onConfirmLogout = {
            settingsViewModel.logout()
            settingsViewModel.hideLogoutDialog()
            onLogoutNavigation()
        }
    )
}

@Composable
fun MainScreenContent(
    settingsUiState: SettingsUiState,
    navState: MainScreenNavDestination,
    onNavStateChanged: (MainScreenNavDestination) -> Unit,
    onDismissLogout: () -> Unit,
    onConfirmLogout: () -> Unit
) {
    PermanentNavigationDrawer(
        drawerContent = {
            Column(Modifier.fillMaxWidth()) {
                Profile(
                    modifier = Modifier.fillMaxWidth(),
                    uiState = settingsUiState.profileUiState
                )

                NavigationDrawerItem(
                    label = {
                        Text(text = stringResource(Res.string.main_nav_overview))
                    },
                    selected = (navState == MainScreenNavDestination.OVERVIEW),
                    onClick = { onNavStateChanged.invoke(MainScreenNavDestination.OVERVIEW) }
                )

                NavigationDrawerItem(
                    label = {
                        Text(text = stringResource(Res.string.main_nav_send))
                    },
                    selected = (navState == MainScreenNavDestination.SEND),
                    onClick = { onNavStateChanged.invoke(MainScreenNavDestination.SEND) }
                )

                NavigationDrawerItem(
                    label = {
                        Text(text = stringResource(Res.string.main_nav_receive))
                    },
                    selected = (navState == MainScreenNavDestination.RECEIVE),
                    onClick = { onNavStateChanged.invoke(MainScreenNavDestination.RECEIVE) }
                )

                ExpandableDrawerItem(
                    label = {
                        Text(text = stringResource(Res.string.main_nav_settings))
                    }
                ) {
                    SettingsList(settingsUiState.settingsListUiState)
                }
            }
        }
    ) {
        when (navState) {
            MainScreenNavDestination.OVERVIEW -> OverviewScreenDesktop()
            MainScreenNavDestination.SEND -> SendScreenDesktop()
            MainScreenNavDestination.RECEIVE -> ReceiveScreenDesktop()
        }
    }

    if (settingsUiState.showLogoutDialog)
        LogoutDialog(
            onDismiss = onDismissLogout,
            onConfirm = onConfirmLogout
        )
}

@Composable
fun MainScreenContentPreview() {
    AttoWalletTheme {
        MainScreenContent(
            settingsUiState = SettingsUiState.PREVIEW,
            navState = MainScreenNavDestination.OVERVIEW,
            onNavStateChanged = {},
            onDismissLogout = {},
            onConfirmLogout = {}
        )
    }
}