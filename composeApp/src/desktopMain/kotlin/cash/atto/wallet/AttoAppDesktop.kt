package cash.atto.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.screens.BackupSecretPhraseScreen
import cash.atto.wallet.screens.CreatePasswordScreen
import cash.atto.wallet.screens.EnterPassword
import cash.atto.wallet.screens.ImportSecretScreen
import cash.atto.wallet.screens.MainScreenDesktop
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
import com.arkivanov.decompose.router.stack.popWhile
import com.arkivanov.decompose.router.stack.push
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AttoAppDesktop(
    component: NavigationComponent
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
    component: NavigationComponent,
    modifier: Modifier = Modifier,
    submitPassword: suspend (String?) -> Boolean
) {
    when (uiState.shownScreen) {
        AppUiState.ShownScreen.LOADER -> {
            Box(
                modifier = modifier.fillMaxSize()
                    .background(color = MaterialTheme.colors.surface)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                        .width(64.dp),
                    color = MaterialTheme.colors.primary
                )
            }
        }

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
                            component.navigation.push(AttoDestination.DesktopMain)
                        }
                    )

                    is AttoDestination.DesktopMain -> MainScreenDesktop(
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