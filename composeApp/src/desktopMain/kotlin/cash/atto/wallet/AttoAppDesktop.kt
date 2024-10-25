package cash.atto.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.screens.MainScreenDesktop
import cash.atto.wallet.screens.SecretPhraseScreen
import cash.atto.wallet.screens.WelcomeScreen
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.AppUiState
import cash.atto.wallet.viewmodel.AppViewModel
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popToFirst
import com.arkivanov.decompose.router.stack.popWhile
import com.arkivanov.decompose.router.stack.push
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
                component = component
            )
        }
    }
}

@Composable
fun AttoNavHost(
    uiState: AppUiState,
    component: NavigationComponent,
    modifier: Modifier = Modifier
) {
    if (uiState.shownScreen == AppUiState.ShownScreen.LOADER) {
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
    } else {
        if (
            uiState.shownScreen == AppUiState.ShownScreen.OVERVIEW
            && component.childStack.active.instance == AttoDestination.Welcome
        ) {
            component.navigation.push(AttoDestination.DesktopMain)
        }

        Children(
            stack = component.childStack,
        ) { screen ->
            when (screen.instance) {
                is AttoDestination.Welcome -> WelcomeScreen {
                    component.navigation.push(AttoDestination.SecretPhrase)
                }

                is AttoDestination.SecretPhrase -> SecretPhraseScreen(
                    onBackNavigation = { component.navigation.pop() },
                    onBackupConfirmClicked = {
                        component.navigation.push(AttoDestination.DesktopMain)
                    }
                )

                is AttoDestination.DesktopMain -> MainScreenDesktop(
                    onLogoutNavigation = {
                        component.navigation.popToFirst()
                    }
                )

                else -> {}
            }
        }
    }
}