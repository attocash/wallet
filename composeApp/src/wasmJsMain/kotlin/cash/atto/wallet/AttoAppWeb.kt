package cash.atto.wallet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.copyright_link
import attowallet.composeapp.generated.resources.copyright_title
import cash.atto.wallet.components.common.AttoLoader
import cash.atto.wallet.screens.BackupSecretPhraseScreen
import cash.atto.wallet.screens.CreatePasswordScreen
import cash.atto.wallet.screens.EnterPassword
import cash.atto.wallet.screens.ImportSecretScreen
import cash.atto.wallet.screens.MainScreenWeb
import cash.atto.wallet.screens.RepresentativeScreen
import cash.atto.wallet.screens.SecretPhraseScreen
import cash.atto.wallet.screens.WelcomeScreen
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.AppUiState
import cash.atto.wallet.viewmodel.AppViewModel
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.backStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popToFirst
import com.arkivanov.decompose.router.stack.push
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AttoAppWeb(
    component: DWNavigationComponent
) {
    AttoWalletTheme {
        KoinContext {
            val viewModel = koinViewModel<AppViewModel>()
            val uiState = viewModel.state.collectAsState()
            val uriHandler = LocalUriHandler.current
            val windowSizeClass = calculateWindowSizeClass()

            Box(Modifier.fillMaxSize()) {
                AttoNavHost(
                    uiState = uiState.value,
                    component = component,
                    submitPassword = {
                        viewModel.enterPassword(it)
                    }
                )

                if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) {
                    Text(
                        modifier = Modifier.align(Alignment.BottomStart)
                            .padding(16.dp),
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(
                                textDecoration = TextDecoration.Underline
                            )) {
                                val text = stringResource(Res.string.copyright_title)
                                val link = stringResource(Res.string.copyright_link)
                                append(text)
                                addLink(
                                    clickable = LinkAnnotation.Clickable(
                                        tag = "URL",
                                        linkInteractionListener = {
                                            uriHandler.openUri(link)
                                        }
                                    ),
                                    start = 0,
                                    end = text.length
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AttoNavHost(
    uiState: AppUiState,
    component: DWNavigationComponent,
    modifier: Modifier = Modifier,
    submitPassword: suspend (String?) -> Boolean
) {
    when (uiState.shownScreen) {
        AppUiState.ShownScreen.LOADER -> AttoLoader(modifier)

        AppUiState.ShownScreen.PASSWORD_ENTER -> {
            val passwordValid = remember {
                mutableStateOf(true)
            }

            val coroutineScope = rememberCoroutineScope()

            EnterPassword(
                onSubmitPassword = {
                    coroutineScope.launch {
                        passwordValid.value = (submitPassword.invoke(it))
                    }
                },
                passwordValid = passwordValid.value
            )
        }

        AppUiState.ShownScreen.PASSWORD_CREATE -> {
            CreatePasswordScreen(
                onBackNavigation = { component.navigation.pop() },
                onConfirmClick = {}
            )
        }

        else -> {
            if (
                uiState.shownScreen == AppUiState.ShownScreen.OVERVIEW
                && !component.childStack
                    .backStack
                    .any { it.instance == AttoDestination.DesktopMain }
                && component.childStack.active.instance != AttoDestination.DesktopMain
            ) {
                component.navigation.push(AttoDestination.DesktopMain)
            }

            Children(
                stack = component.childStack,
            ) { screen ->
                when (screen.instance) {
                    is AttoDestination.BackupSecret -> BackupSecretPhraseScreen(
                        onBackNavigation = { component.navigation.pop() }
                    )

                    is AttoDestination.CreatePassword -> CreatePasswordScreen(
                        onBackNavigation = { component.navigation.pop() },
                        onConfirmClick = {
                            if (
                                !component.childStack
                                    .backStack
                                    .any { it.instance == AttoDestination.DesktopMain }
                                && component.childStack
                                    .active
                                    .instance != AttoDestination.DesktopMain
                            ) {
                                component.navigation.push(AttoDestination.DesktopMain)
                            }
                        }
                    )

                    is AttoDestination.DesktopMain -> MainScreenWeb(
                        onBackupSecretNavigation = {
                            component.navigation.push(AttoDestination.BackupSecret)
                        },
                        onRepresentativeNavigation = {
                            component.navigation.push(AttoDestination.Representative)
                        },
                        onLogoutNavigation = {
                            component.navigation.popToFirst()
                        }
                    )

                    is AttoDestination.ImportSecret -> ImportSecretScreen(
                        onBackNavigation = { component.navigation.pop() },
                        onImportAccount = {
                            component.navigation.push(AttoDestination.CreatePassword)
                        }
                    )

                    is AttoDestination.Representative -> RepresentativeScreen(
                        onBackNavigation = { component.navigation.pop() }
                    )

                    is AttoDestination.SecretPhrase -> SecretPhraseScreen(
                        onBackNavigation = { component.navigation.pop() },
                        onBackupConfirmClicked = {
                            component.navigation.push(AttoDestination.CreatePassword)
                        }
                    )

                    is AttoDestination.Welcome -> WelcomeScreen(
                        onCreateSecretClicked = {
                            component.navigation.push(AttoDestination.SecretPhrase)
                        },
                        onImportSecretClicked = {
                            component.navigation.push(AttoDestination.ImportSecret)
                        }
                    )

                    else -> {}
                }
            }
        }
    }
}