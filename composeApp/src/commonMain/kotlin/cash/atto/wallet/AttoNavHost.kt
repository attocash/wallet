package cash.atto.wallet

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cash.atto.wallet.components.common.AttoLoader
import cash.atto.wallet.screens.*
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.AppUiState
import cash.atto.wallet.viewmodel.AppViewModel
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.router.stack.*
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AttoApp(
    component: DWNavigationComponent,
    debugScreen: String? = null,
    debugPassword: String? = null,
    initialNavOverride: MainScreenNavDestination? = null,
    qrScannerContent: (
        @Composable (
            onResult: (String) -> Unit,
            onError: (String) -> Unit,
            onDismiss: () -> Unit,
        ) -> Unit
    )? = null,
) {
    AttoWalletTheme {
        val viewModel = koinViewModel<AppViewModel>()
        val uiState = viewModel.state.collectAsState()

        AttoNavHost(
            uiState = uiState.value,
            component = component,
            debugScreen = debugScreen,
            debugPassword = debugPassword,
            initialNavOverride = initialNavOverride,
            qrScannerContent = qrScannerContent,
            submitPassword = {
                viewModel.enterPassword(it)
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
    component: DWNavigationComponent,
    modifier: Modifier = Modifier,
    debugScreen: String? = null,
    debugPassword: String? = null,
    initialNavOverride: MainScreenNavDestination? = null,
    qrScannerContent: (
        @Composable (
            onResult: (String) -> Unit,
            onError: (String) -> Unit,
            onDismiss: () -> Unit,
        ) -> Unit
    )? = null,
    submitPassword: suspend (String?) -> Boolean,
    onLogout: () -> Unit,
) {
    if (debugScreen == "login") {
        val passwordValid =
            remember {
                mutableStateOf(true)
            }

        val coroutineScope = rememberCoroutineScope()

        LoginScreen(
            onSubmitPassword = {
                coroutineScope.launch {
                    passwordValid.value = (submitPassword.invoke(it))
                }
            },
            passwordValid = passwordValid.value,
            onLogout = {
                onLogout()
                component.navigation.popToFirst()
            },
        )
        return
    }

    when (uiState.shownScreen) {
        AppUiState.ShownScreen.LOADER -> AttoLoader(modifier)

        AppUiState.ShownScreen.PASSWORD_ENTER -> {
            val passwordValid =
                remember {
                    mutableStateOf(true)
                }
            val attemptedDebugPassword =
                remember {
                    mutableStateOf<String?>(null)
                }

            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(debugPassword) {
                if (
                    !debugPassword.isNullOrBlank() &&
                    attemptedDebugPassword.value != debugPassword
                ) {
                    attemptedDebugPassword.value = debugPassword
                    passwordValid.value = submitPassword.invoke(debugPassword)
                }
            }

            LoginScreen(
                onSubmitPassword = {
                    coroutineScope.launch {
                        passwordValid.value = (submitPassword.invoke(it))
                    }
                },
                passwordValid = passwordValid.value,
                onLogout = {
                    onLogout()
                    component.navigation.popToFirst()
                },
            )
        }

        AppUiState.ShownScreen.PASSWORD_CREATE -> {
            CreatePasswordScreen(
                onBackNavigation = { component.navigation.pop() },
                onConfirmClick = {},
            )
        }

        else -> {
            if (
                uiState.shownScreen == AppUiState.ShownScreen.OVERVIEW &&
                !component.childStack
                    .backStack
                    .any { it.instance == AttoDestination.DesktopMain } &&
                component.childStack.active.instance != AttoDestination.DesktopMain
            ) {
                component.navigation.push(AttoDestination.DesktopMain)
            }

            Children(
                stack = component.childStack,
            ) { screen ->
                when (screen.instance) {
                    is AttoDestination.CreatePassword ->
                        CreatePasswordScreen(
                            onBackNavigation = { component.navigation.pop() },
                            onConfirmClick = {
                                if (
                                    !component.childStack
                                        .backStack
                                        .any { it.instance == AttoDestination.DesktopMain } &&
                                    component.childStack
                                        .active
                                        .instance != AttoDestination.DesktopMain
                                ) {
                                    component.navigation.push(AttoDestination.DesktopMain)
                                }
                            },
                        )

                    is AttoDestination.DesktopMain ->
                        MainScreen(
                            onLogoutNavigation = {
                                component.navigation.popToFirst()
                            },
                            initialNavOverride = initialNavOverride,
                            qrScannerContent = qrScannerContent,
                        )

                    is AttoDestination.ImportPhrase ->
                        ImportPhraseScreen(
                            onBackNavigation = { component.navigation.pop() },
                            onImportAccount = {
                                component.navigation.push(AttoDestination.CreatePassword)
                            },
                        )

                    is AttoDestination.Voter ->
                        VoterScreen(
                            onBackNavigation = { component.navigation.pop() },
                        )

                    is AttoDestination.RecoveryPhrase ->
                        RecoveryPhraseScreen(
                            onBackNavigation = { component.navigation.pop() },
                            onBackupConfirmClicked = {
                                component.navigation.push(AttoDestination.CreatePassword)
                            },
                        )

                    is AttoDestination.Welcome ->
                        WelcomeScreen(
                            onCreateSecretClicked = {
                                component.navigation.push(AttoDestination.RecoveryPhrase)
                            },
                            onImportSecretClicked = {
                                component.navigation.push(AttoDestination.ImportPhrase)
                            },
                        )

                    else -> {}
                }
            }
        }
    }
}
