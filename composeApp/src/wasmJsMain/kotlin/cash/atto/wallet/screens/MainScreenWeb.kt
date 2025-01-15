package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_background_desktop
import attowallet.composeapp.generated.resources.ic_nav_overview
import attowallet.composeapp.generated.resources.ic_nav_receive
import attowallet.composeapp.generated.resources.ic_nav_send
import attowallet.composeapp.generated.resources.main_nav_overview
import attowallet.composeapp.generated.resources.main_nav_receive
import attowallet.composeapp.generated.resources.main_nav_send
import attowallet.composeapp.generated.resources.main_nav_settings
import cash.atto.wallet.MainScreenNavDestination
import cash.atto.wallet.components.mainscreen.BalanceChip
import cash.atto.wallet.components.mainscreen.ExpandableDrawerItem
import cash.atto.wallet.components.mainscreen.NavigationDrawerItem
import cash.atto.wallet.components.mainscreen.PermanentNavigationDrawer
import cash.atto.wallet.components.settings.LogoutDialog
import cash.atto.wallet.components.settings.ProfileExtended
import cash.atto.wallet.components.settings.SettingsList
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.desktop.MainScreenUiState
import cash.atto.wallet.viewmodel.MainScreenViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreenWeb(
    onBackupSecretNavigation: () -> Unit,
    onRepresentativeNavigation: () -> Unit,
    onLogoutNavigation: () -> Unit
) {
    val viewModel = koinViewModel<MainScreenViewModel>()
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(uiState.value.navigateToBackup) {
        if (uiState.value.navigateToBackup) {
            viewModel.handleBackupNavigation()
            onBackupSecretNavigation.invoke()
        }
    }

    LaunchedEffect(uiState.value.navigateToRepresentative) {
        if (uiState.value.navigateToRepresentative) {
            viewModel.handleRepresentativeNavigation()
            onRepresentativeNavigation.invoke()
        }
    }

    val navState = remember {
        mutableStateOf(MainScreenNavDestination.OVERVIEW)
    }

    MainScreenContent(
        uiState = uiState.value,
        navState = navState.value,
        onNavStateChanged = { navState.value = it },
        onDismissLogout = { viewModel.hideLogoutDialog() },
        onConfirmLogout = {
            viewModel.logout()
            viewModel.hideLogoutDialog()
            onLogoutNavigation()
        }
    )
}

@Composable
fun MainScreenContent(
    uiState: MainScreenUiState,
    navState: MainScreenNavDestination,
    onNavStateChanged: (MainScreenNavDestination) -> Unit,
    onDismissLogout: () -> Unit,
    onConfirmLogout: () -> Unit
) {
    val settingsUiState = uiState.settingsUiState

    Column(
        modifier = Modifier.fillMaxSize()
            .paint(
                painter = painterResource(Res.drawable.atto_background_desktop),
                contentScale = ContentScale.FillBounds
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileExtended(
            modifier = Modifier.fillMaxWidth(),
            uiState = settingsUiState.profileUiState
        )

        PermanentNavigationDrawer(
            modifier = Modifier,
            drawerContent = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BalanceChip(
                        modifier = Modifier.fillMaxWidth(),
                        uiState = uiState.balanceChipUiState
                    )

                    NavigationDrawerItem(
                        label = stringResource(Res.string.main_nav_overview),
                        icon = vectorResource(Res.drawable.ic_nav_overview),
                        selected = (navState == MainScreenNavDestination.OVERVIEW),
                        onClick = { onNavStateChanged.invoke(MainScreenNavDestination.OVERVIEW) }
                    )

                    NavigationDrawerItem(
                        label = stringResource(Res.string.main_nav_send),
                        icon = vectorResource(Res.drawable.ic_nav_send),
                        selected = (navState == MainScreenNavDestination.SEND),
                        onClick = { onNavStateChanged.invoke(MainScreenNavDestination.SEND) }
                    )

                    NavigationDrawerItem(
                        label = stringResource(Res.string.main_nav_receive),
                        icon = vectorResource(Res.drawable.ic_nav_receive),
                        selected = (navState == MainScreenNavDestination.RECEIVE),
                        onClick = { onNavStateChanged.invoke(MainScreenNavDestination.RECEIVE) }
                    )

                    ExpandableDrawerItem(
                        label = stringResource(Res.string.main_nav_settings)
                    ) {
                        SettingsList(settingsUiState.settingsListUiState)
                    }
                }
            }
        ) {
            val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            }

            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                when (navState) {
                    MainScreenNavDestination.OVERVIEW -> OverviewScreenWeb()
                    MainScreenNavDestination.SEND -> SendScreenWeb()
                    MainScreenNavDestination.RECEIVE -> ReceiveScreenWeb()
                }
            }
        }
    }

    if (uiState.showLogoutDialog)
        LogoutDialog(
            onDismiss = onDismissLogout,
            onConfirm = onConfirmLogout
        )
}

@Composable
fun MainScreenContentPreview() {
    AttoWalletTheme {
        MainScreenContent(
            uiState = MainScreenUiState.DEFAULT,
            navState = MainScreenNavDestination.OVERVIEW,
            onNavStateChanged = {},
            onDismissLogout = {},
            onConfirmLogout = {}
        )
    }
}