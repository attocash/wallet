package cash.atto.wallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
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
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.backStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popToFirst
import com.arkivanov.decompose.router.stack.push
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AttoApp(
    component: DWNavigationComponent,
    debugScreen: String? = null,
    debugPassword: String? = null,
    initialNavOverride: MainScreenNavDestination? = null,
    initialSendPaymentRequest: String? = null,
    initialOpenSendConfirm: Boolean = false,
    onAuthenticated: (() -> Unit)? = null,
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
        val viewModel = koinViewModel<AppViewModel>()
        val uiState = viewModel.state.collectAsState()

        AttoNavHost(
            uiState = uiState.value,
            component = component,
            debugScreen = debugScreen,
            debugPassword = debugPassword,
            initialNavOverride = initialNavOverride,
            initialSendPaymentRequest = initialSendPaymentRequest,
            initialOpenSendConfirm = initialOpenSendConfirm,
            onAuthenticated = onAuthenticated,
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
    initialSendPaymentRequest: String? = null,
    initialOpenSendConfirm: Boolean = false,
    onAuthenticated: (() -> Unit)? = null,
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
    val authenticationCallbackConsumed = remember { mutableStateOf(false) }

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
        AppUiState.ShownScreen.LOADER -> {
            AttoLoader(modifier)
        }

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
                    is AttoDestination.CreatePassword -> {
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
                    }

                    is AttoDestination.DesktopMain -> {
                        MainScreen(
                            onLogoutNavigation = {
                                component.navigation.popToFirst()
                            },
                            initialNavOverride = initialNavOverride,
                            initialSendPaymentRequest = initialSendPaymentRequest,
                            initialOpenSendConfirm = initialOpenSendConfirm,
                            qrScannerContent = qrScannerContent,
                        )
                    }

                    is AttoDestination.ImportPhrase -> {
                        ImportPhraseScreen(
                            onBackNavigation = { component.navigation.pop() },
                            onImportAccount = {
                                component.navigation.push(AttoDestination.CreatePassword)
                            },
                        )
                    }

                    is AttoDestination.RecoveryPhrase -> {
                        RecoveryPhraseScreen(
                            onBackNavigation = { component.navigation.pop() },
                            onBackupConfirmClicked = {
                                component.navigation.push(AttoDestination.CreatePassword)
                            },
                        )
                    }

                    is AttoDestination.Welcome -> {
                        WelcomeScreen(
                            onCreateSecretClicked = {
                                component.navigation.push(AttoDestination.RecoveryPhrase)
                            },
                            onImportSecretClicked = {
                                component.navigation.push(AttoDestination.ImportPhrase)
                            },
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}
