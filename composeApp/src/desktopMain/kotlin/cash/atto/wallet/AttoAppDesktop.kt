package cash.atto.wallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cash.atto.wallet.components.common.AttoLoader
import cash.atto.wallet.screens.BackupSecretPhraseScreen
import cash.atto.wallet.screens.CreatePasswordScreen
import cash.atto.wallet.screens.EnterPassword
import cash.atto.wallet.screens.ImportSecretScreen
import cash.atto.wallet.screens.MainScreenDesktop
import cash.atto.wallet.screens.VoterDetailScreen
import cash.atto.wallet.screens.VoterScreen
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
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AttoAppDesktop(
    component: DWNavigationComponent
) {
    AttoWalletTheme {
        KoinContext {
            val viewModel = koinViewModel<AppViewModel>()
            val uiState = viewModel.state.collectAsState()

            AttoNavHost(
                uiState = uiState.value,
                component = component,
                submitPassword = {
                    viewModel.enterPassword(it)
                }
            )
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

                    is AttoDestination.DesktopMain -> MainScreenDesktop(
                        onBackupSecretNavigation = {
                            component.navigation.push(AttoDestination.BackupSecret)
                        },
                        onLogoutNavigation = {
                            component.navigation.popToFirst()
                        },
                        onVoterDetailNavigation = { voterAddress ->
                            component.navigation.push(AttoDestination.VoterDetail(voterAddress))
                        }
                    )

                    is AttoDestination.VoterDetail -> VoterDetailScreen(
                        voterAddress = (screen.instance as AttoDestination.VoterDetail).voterAddress,
                        onBackNavigation = { component.navigation.pop() },
                        onConfirm = { component.navigation.pop() }
                    )

                    is AttoDestination.ImportSecret -> ImportSecretScreen(
                        onBackNavigation = { component.navigation.pop() },
                        onImportAccount = {
                            component.navigation.push(AttoDestination.CreatePassword)
                        }
                    )

                    is AttoDestination.Voter -> VoterScreen(
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