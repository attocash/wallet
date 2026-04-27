package cash.atto.wallet.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import cash.atto.wallet.MainScreenNavDestination
import cash.atto.wallet.components.common.AttoWallet
import cash.atto.wallet.components.settings.BackupSecretDialog
import cash.atto.wallet.components.settings.LogoutDialog
import cash.atto.wallet.repository.PersistentWorkCache
import cash.atto.wallet.ui.isCompactWidth
import cash.atto.wallet.uistate.desktop.MainScreenUiState
import cash.atto.wallet.viewmodel.MainScreenViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    onLogoutNavigation: () -> Unit,
    initialNavOverride: MainScreenNavDestination? = null,
    initialSendPaymentRequest: String? = null,
    initialOpenSendConfirm: Boolean = false,
    qrScannerContent: (
        @Composable (
            onResult: (String) -> Unit,
            onError: (String) -> Unit,
            onDismiss: () -> Unit,
        ) -> Unit
    )? = null,
) {
    val viewModel = koinViewModel<MainScreenViewModel>()
    val workCache = koinInject<PersistentWorkCache>()
    val uiState = viewModel.state.collectAsState()
    val hasCachedWork = workCache.hasCachedWork.collectAsState()

    val navState =
        rememberSaveable {
            mutableStateOf(initialNavOverride ?: MainScreenNavDestination.OVERVIEW)
        }

    val showBackupDialog = rememberSaveable { mutableStateOf(false) }

    MainScreenContent(
        uiState = uiState.value,
        navState = navState.value,
        onNavStateChanged = { navState.value = it },
        showBackupDialog = showBackupDialog.value,
        onBackupClick = { showBackupDialog.value = true },
        onDismissBackup = { showBackupDialog.value = false },
        hasCachedWork = hasCachedWork.value,
        onLock = { viewModel.lock() },
        onShowLogout = { viewModel.showLogoutDialog() },
        onDismissLogout = { viewModel.hideLogoutDialog() },
        onExportPreferences = { viewModel.exportPreferences() },
        onImportPreferences = { viewModel.importPreferences() },
        onDismissPreferencesMessage = { viewModel.clearPreferencesMessage() },
        onConfirmLogout = {
            viewModel.logout()
            viewModel.hideLogoutDialog()
            onLogoutNavigation()
        },
        initialSendPaymentRequest = initialSendPaymentRequest,
        initialOpenSendConfirm = initialOpenSendConfirm,
        qrScannerContent = qrScannerContent,
    )
}

@Composable
fun MainScreenContent(
    uiState: MainScreenUiState,
    navState: MainScreenNavDestination,
    onNavStateChanged: (MainScreenNavDestination) -> Unit,
    showBackupDialog: Boolean,
    onBackupClick: () -> Unit,
    onDismissBackup: () -> Unit,
    hasCachedWork: Boolean,
    onLock: () -> Unit,
    onShowLogout: () -> Unit,
    onDismissLogout: () -> Unit,
    onExportPreferences: () -> Unit,
    onImportPreferences: () -> Unit,
    onDismissPreferencesMessage: () -> Unit,
    onConfirmLogout: () -> Unit,
    initialSendPaymentRequest: String? = null,
    initialOpenSendConfirm: Boolean = false,
    qrScannerContent: (
        @Composable (
            onResult: (String) -> Unit,
            onError: (String) -> Unit,
            onDismiss: () -> Unit,
        ) -> Unit
    )? = null,
) {
    AttoWallet(
        navState = navState,
        onNavStateChanged = onNavStateChanged,
        balanceUiState = uiState.balanceChipUiState,
        isWalletInitialized = uiState.isWalletInitialized,
        hasCachedWork = hasCachedWork,
        onLock = onLock,
    ) {
        val viewModelStoreOwner =
            checkNotNull(LocalViewModelStoreOwner.current) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides viewModelStoreOwner,
        ) {
            if (showBackupDialog) {
                BackupSecretDialog(
                    onDismiss = onDismissBackup,
                    compact = isCompactWidth(),
                )
            }

            when (navState) {
                MainScreenNavDestination.OVERVIEW -> {
                    OverviewScreen(
                        isWalletInitialized = uiState.isWalletInitialized,
                        onSendClick = {
                            if (uiState.isWalletInitialized) {
                                onNavStateChanged(MainScreenNavDestination.SEND)
                            }
                        },
                        onReceiveClick = { onNavStateChanged(MainScreenNavDestination.RECEIVE) },
                        onTransactionsClick = { onNavStateChanged(MainScreenNavDestination.TRANSACTIONS) },
                        onStakingClick = {
                            if (uiState.isWalletInitialized) {
                                onNavStateChanged(MainScreenNavDestination.STAKING)
                            }
                        },
                    )
                }

                MainScreenNavDestination.SEND -> {
                    SendScreen(
                        onBackClick = { onNavStateChanged(MainScreenNavDestination.OVERVIEW) },
                        initialPaymentRequest = initialSendPaymentRequest,
                        openConfirmOnLaunch = initialOpenSendConfirm,
                        qrScannerContent = qrScannerContent,
                    )
                }

                MainScreenNavDestination.RECEIVE -> {
                    ReceiveScreen(
                        onBackClick = { onNavStateChanged(MainScreenNavDestination.OVERVIEW) },
                    )
                }

                MainScreenNavDestination.TRANSACTIONS -> {
                    TransactionsScreen(
                        onBackClick = { onNavStateChanged(MainScreenNavDestination.OVERVIEW) },
                    )
                }

                MainScreenNavDestination.STAKING -> {
                    StakingScreen(
                        onBackClick = { onNavStateChanged(MainScreenNavDestination.OVERVIEW) },
                    )
                }

                MainScreenNavDestination.SETTINGS -> {
                    SettingsScreen(
                        uiState = uiState.settingsUiState,
                        onBackClick = { onNavStateChanged(MainScreenNavDestination.OVERVIEW) },
                        onBackupClick = onBackupClick,
                        onExportClick = onExportPreferences,
                        onImportClick = onImportPreferences,
                        onDismissPreferencesMessage = onDismissPreferencesMessage,
                        onLogoutClick = onShowLogout,
                    )
                }
            }
        }
    }

    if (uiState.showLogoutDialog) {
        LogoutDialog(
            onDismiss = onDismissLogout,
            onConfirm = onConfirmLogout,
        )
    }
}
