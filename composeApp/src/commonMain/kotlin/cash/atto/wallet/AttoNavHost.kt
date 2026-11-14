package cash.atto.wallet

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cash.atto.wallet.components.common.AttoLoader
import cash.atto.wallet.screens.CreatePasswordScreen
import cash.atto.wallet.screens.ImportPhraseScreen
import cash.atto.wallet.screens.LoginScreen
import cash.atto.wallet.screens.MainScreen
import cash.atto.wallet.screens.RecoveryPhraseScreen
import cash.atto.wallet.screens.WelcomeScreen
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.uistate.AppUiState
import cash.atto.wallet.viewmodel.AppViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AttoApp(
    initialDestination: AttoDestination = AttoDestination.Welcome,
    debugScreen: String? = null,
    debugPassword: String? = null,
    initialNavOverride: MainScreenNavDestination? = null,
    initialSendPaymentRequest: String? = null,
    initialOpenSendConfirm: Boolean = false,
    onAuthenticated: (() -> Unit)? = null,
    onAuthenticationNavigation: () -> Unit = {},
    onNavHostReady: suspend (NavHostController) -> Unit = {},
    qrScannerContent: (
        @Composable (
            onResult: (String) -> Unit,
            onError: (String) -> Unit,
            onDismiss: () -> Unit,
        ) -> Unit
    )? = null,
) {
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val fontFamily = attoFontFamily()

    LaunchedEffect(fontFamilyResolver, fontFamily) {
        fontFamilyResolver.preload(fontFamily)
    }

    AttoWalletTheme {
        val navController = rememberNavController()
        val viewModel = koinViewModel<AppViewModel>()
        val uiState = viewModel.state.collectAsState()

        AttoNavHost(
            uiState = uiState.value,
            navController = navController,
            initialDestination = initialDestination,
            debugScreen = debugScreen,
            debugPassword = debugPassword,
            initialNavOverride = initialNavOverride,
            initialSendPaymentRequest = initialSendPaymentRequest,
            initialOpenSendConfirm = initialOpenSendConfirm,
            onAuthenticated = onAuthenticated,
            onAuthenticationNavigation = onAuthenticationNavigation,
            onNavHostReady = onNavHostReady,
            qrScannerContent = qrScannerContent,
            submitPassword = {
                viewModel.enterPassword(it)
            },
            onTermsAndConditionsAcceptedChange = {
                viewModel.setTermsAndConditionsAccepted(it)
            },
            onLogout = {
                viewModel.logout()
            },
        )
    }
}

@Composable
fun AttoNavHost(
    uiState: AppUiState,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    initialDestination: AttoDestination = AttoDestination.Welcome,
    debugScreen: String? = null,
    debugPassword: String? = null,
    initialNavOverride: MainScreenNavDestination? = null,
    initialSendPaymentRequest: String? = null,
    initialOpenSendConfirm: Boolean = false,
    onAuthenticated: (() -> Unit)? = null,
    onAuthenticationNavigation: () -> Unit = {},
    onNavHostReady: suspend (NavHostController) -> Unit = {},
    qrScannerContent: (
        @Composable (
            onResult: (String) -> Unit,
            onError: (String) -> Unit,
            onDismiss: () -> Unit,
        ) -> Unit
    )? = null,
    submitPassword: suspend (String?) -> Boolean,
    onTermsAndConditionsAcceptedChange: suspend (Boolean) -> Unit,
    onLogout: () -> Unit,
) {
    val authenticationCallbackConsumed = remember { mutableStateOf(false) }
    val currentEntry by navController.currentBackStackEntryAsState()
    val shownScreen = if (debugScreen == "login") AppUiState.ShownScreen.PASSWORD_ENTER else uiState.shownScreen

    LaunchedEffect(uiState.shownScreen, onAuthenticated) {
        if (
            !authenticationCallbackConsumed.value &&
            onAuthenticated != null &&
            uiState.shownScreen == AppUiState.ShownScreen.OVERVIEW
        ) {
            authenticationCallbackConsumed.value = true
            onAuthenticated()
        }
    }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.first { !it.destination.hasRoute<AttoDestination.Loading>() }
        onNavHostReady(navController)
    }

    LaunchedEffect(shownScreen, currentEntry) {
        val destination = currentEntry?.destination ?: return@LaunchedEffect
        when (shownScreen) {
            AppUiState.ShownScreen.LOADER -> {
                Unit
            }

            AppUiState.ShownScreen.PASSWORD_ENTER -> {
                if (!destination.hasRoute<AttoDestination.Login>()) {
                    onAuthenticationNavigation()
                    navController.navigate(AttoDestination.Login) { launchSingleTop = true }
                }
            }

            AppUiState.ShownScreen.PASSWORD_CREATE -> {
                if (!destination.hasRoute<AttoDestination.CreatePassword>()) {
                    onAuthenticationNavigation()
                    navController.resetTo(AttoDestination.CreatePassword)
                }
            }

            AppUiState.ShownScreen.OVERVIEW -> {
                if (!destination.hasRoute<AttoDestination.DesktopMain>()) {
                    onAuthenticationNavigation()
                    // Locking leaves the wallet below login so its ViewModels and saved tab survive.
                    if (!navController.popBackStack<AttoDestination.DesktopMain>(inclusive = false)) {
                        navController.resetTo(AttoDestination.DesktopMain)
                    }
                }
            }

            AppUiState.ShownScreen.WELCOME -> {
                when {
                    destination.hasRoute<AttoDestination.Loading>() -> {
                        val onboardingDestination =
                            when (initialDestination) {
                                AttoDestination.Loading, AttoDestination.Login, AttoDestination.DesktopMain -> AttoDestination.Welcome
                                else -> initialDestination
                            }
                        onAuthenticationNavigation()
                        navController.resetTo(onboardingDestination)
                    }

                    destination.hasRoute<AttoDestination.Login>() || destination.hasRoute<AttoDestination.DesktopMain>() -> {
                        onAuthenticationNavigation()
                        navController.resetTo(AttoDestination.Welcome)
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AttoDestination.Loading,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<AttoDestination.Loading> { AttoLoader() }
        composable<AttoDestination.Login> {
            if (shownScreen == AppUiState.ShownScreen.PASSWORD_ENTER) {
                val passwordValid = remember { mutableStateOf(true) }
                val attemptedDebugPassword = remember { mutableStateOf<String?>(null) }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(debugPassword, uiState.termsAndConditionsAccepted) {
                    if (
                        !debugPassword.isNullOrBlank() &&
                        uiState.termsAndConditionsAccepted &&
                        attemptedDebugPassword.value != debugPassword
                    ) {
                        attemptedDebugPassword.value = debugPassword
                        passwordValid.value = submitPassword(debugPassword)
                    }
                }

                LoginScreen(
                    onSubmitPassword = {
                        coroutineScope.launch {
                            if (uiState.termsAndConditionsAccepted) {
                                passwordValid.value = submitPassword(it)
                            }
                        }
                    },
                    passwordValid = passwordValid.value,
                    errorMessage = uiState.authenticationError,
                    termsAndConditionsAccepted = uiState.termsAndConditionsAccepted,
                    termsAndConditionsDate = uiState.termsAndConditionsDate,
                    onTermsAndConditionsAcceptedChange = { accepted ->
                        coroutineScope.launch { onTermsAndConditionsAcceptedChange(accepted) }
                    },
                    onLogout = onLogout,
                )
            } else {
                AttoLoader()
            }
        }
        composable<AttoDestination.CreatePassword> {
            if (shownScreen == AppUiState.ShownScreen.WELCOME || shownScreen == AppUiState.ShownScreen.PASSWORD_CREATE) {
                CreatePasswordScreen(
                    onBackNavigation = { navController.navigateUp() },
                    // Authentication state owns the transition to the wallet after persistence succeeds.
                    onConfirmClick = {},
                )
            } else {
                AttoLoader()
            }
        }
        composable<AttoDestination.DesktopMain> {
            val walletStateHolder = rememberSaveableStateHolder()
            if (shownScreen == AppUiState.ShownScreen.OVERVIEW) {
                walletStateHolder.SaveableStateProvider("wallet") {
                    MainScreen(
                        onLogout = onLogout,
                        initialNavOverride = initialNavOverride,
                        initialSendPaymentRequest = initialSendPaymentRequest,
                        initialOpenSendConfirm = initialOpenSendConfirm,
                        qrScannerContent = qrScannerContent,
                    )
                }
            } else {
                AttoLoader()
            }
        }
        composable<AttoDestination.ImportPhrase> {
            if (shownScreen == AppUiState.ShownScreen.WELCOME) {
                ImportPhraseScreen(
                    onBackNavigation = { navController.navigateUp() },
                    onImportAccount = { navController.navigate(AttoDestination.CreatePassword) },
                )
            } else {
                AttoLoader()
            }
        }
        composable<AttoDestination.RecoveryPhrase> {
            if (shownScreen == AppUiState.ShownScreen.WELCOME) {
                RecoveryPhraseScreen(
                    onBackNavigation = { navController.navigateUp() },
                    onBackupConfirmClicked = { navController.navigate(AttoDestination.CreatePassword) },
                )
            } else {
                AttoLoader()
            }
        }
        composable<AttoDestination.Welcome> {
            if (shownScreen == AppUiState.ShownScreen.WELCOME) {
                WelcomeScreen(
                    onCreateSecretClicked = { navController.navigate(AttoDestination.RecoveryPhrase) },
                    onImportSecretClicked = { navController.navigate(AttoDestination.ImportPhrase) },
                )
            } else {
                AttoLoader()
            }
        }
    }
}

private fun NavHostController.resetTo(destination: AttoDestination) {
    navigate(destination) {
        popUpTo(graph.id)
        launchSingleTop = true
    }
}
