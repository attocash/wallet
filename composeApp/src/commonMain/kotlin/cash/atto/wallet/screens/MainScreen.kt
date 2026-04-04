package cash.atto.wallet.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import cash.atto.wallet.MainScreenNavDestination
import cash.atto.wallet.components.common.AttoWallet
import cash.atto.wallet.components.settings.BackupSecretDialog
import cash.atto.wallet.components.settings.LogoutDialog
import cash.atto.wallet.repository.PersistentWorkCache
import cash.atto.wallet.uistate.desktop.MainScreenUiState
import cash.atto.wallet.viewmodel.MainScreenViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    onLogoutNavigation: () -> Unit,
    initialNavOverride: MainScreenNavDestination? = null,
    qrScannerContent: (@Composable (
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onDismiss: () -> Unit
    ) -> Unit)? = null
) {
    val viewModel = koinViewModel<MainScreenViewModel>()
    val workCache = koinInject<PersistentWorkCache>()
    val uiState = viewModel.state.collectAsState()
    val hasCachedWork = workCache.hasCachedWork.collectAsState()

    LaunchedEffect(workCache) {
        workCache.get()
    }

    val navState = rememberSaveable {
        mutableStateOf(initialNavOverride ?: MainScreenNavDestination.OVERVIEW)
    }

    val showBackupDialog = rememberSaveable { mutableStateOf(false) }

    MainScreenContent(
        uiState = uiState.value,
        navState = navState.value,
        onNavStateChanged = { navState.value = it },
        onBackupClick = { showBackupDialog.value = true },
        hasCachedWork = hasCachedWork.value,
        onLock = { viewModel.lock() },
        onShowLogout = { viewModel.showLogoutDialog() },
        onDismissLogout = { viewModel.hideLogoutDialog() },
        onConfirmLogout = {
            viewModel.logout()
            viewModel.hideLogoutDialog()
            onLogoutNavigation()
        },
        qrScannerContent = qrScannerContent
    )

    if (showBackupDialog.value) {
        BackupSecretDialog(
            onDismiss = { showBackupDialog.value = false }
        )
    }
}

@Composable
fun MainScreenContent(
    uiState: MainScreenUiState,
    navState: MainScreenNavDestination,
    onNavStateChanged: (MainScreenNavDestination) -> Unit,
    onBackupClick: () -> Unit,
    hasCachedWork: Boolean,
    onLock: () -> Unit,
    onShowLogout: () -> Unit,
    onDismissLogout: () -> Unit,
    onConfirmLogout: () -> Unit,
    qrScannerContent: (@Composable (
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onDismiss: () -> Unit
    ) -> Unit)? = null
) {
    AttoWallet(
        navState = navState,
        onNavStateChanged = onNavStateChanged,
        balanceUiState = uiState.balanceChipUiState,
        hasCachedWork = hasCachedWork,
        onLock = onLock
    ) {
        val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides viewModelStoreOwner
        ) {
            when (navState) {
                MainScreenNavDestination.OVERVIEW -> OverviewScreen(
                    onSendClick = { onNavStateChanged(MainScreenNavDestination.SEND) },
                    onReceiveClick = { onNavStateChanged(MainScreenNavDestination.RECEIVE) },
                    onTransactionsClick = { onNavStateChanged(MainScreenNavDestination.TRANSACTIONS) },
                    onStakingClick = { onNavStateChanged(MainScreenNavDestination.STAKING) }
                )
                MainScreenNavDestination.SEND -> SendScreen(
                    onBackClick = { onNavStateChanged(MainScreenNavDestination.OVERVIEW) },
                    qrScannerContent = qrScannerContent
                )
                MainScreenNavDestination.RECEIVE -> ReceiveScreen(
                    onBackClick = { onNavStateChanged(MainScreenNavDestination.OVERVIEW) }
                )
                MainScreenNavDestination.TRANSACTIONS -> TransactionsScreen(
                    onBackClick = { onNavStateChanged(MainScreenNavDestination.OVERVIEW) }
                )
                MainScreenNavDestination.STAKING -> StakingScreen(
                    onBackClick = { onNavStateChanged(MainScreenNavDestination.OVERVIEW) }
                )
                MainScreenNavDestination.SETTINGS -> SettingsScreen(
                    uiState = uiState.settingsUiState,
                    onBackClick = { onNavStateChanged(MainScreenNavDestination.OVERVIEW) },
                    onBackupClick = onBackupClick,
                    onLockClick = onLock,
                    onLogoutClick = onShowLogout
                )
            }
        }
    }

    if (uiState.showLogoutDialog) {
        LogoutDialog(
            onDismiss = onDismissLogout,
            onConfirm = onConfirmLogout
        )
    }
}
