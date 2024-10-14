package cash.atto.wallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cash.atto.wallet.screens.OverviewScreen
import cash.atto.wallet.screens.SecretPhraseScreen
import cash.atto.wallet.screens.WelcomeScreen
import cash.atto.wallet.ui.AttoWalletTheme
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push

@Composable
fun AttoAppDesktop(
    component: NavigationComponent
) {
    AttoWalletTheme {
        Children(
            stack = component.childStack,
//            animation = stackAnimation(fade() + scale()),
        ) { screen ->
            when (screen.instance) {
                is AttoDestination.Welcome -> WelcomeScreen {
                    component.navigation.push(AttoDestination.SecretPhrase)
                }

                is AttoDestination.SecretPhrase -> SecretPhraseScreen(
                    onBackNavigation = { component.navigation.pop() },
                    onBackupConfirmClicked = {
                        component.navigation.push(AttoDestination.Overview)
                    }
                )

                is AttoDestination.Overview -> OverviewScreen {  }
                else -> {}
            }
        }
    }
}