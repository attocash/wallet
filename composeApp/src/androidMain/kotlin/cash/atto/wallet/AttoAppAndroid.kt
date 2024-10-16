package cash.atto.wallet

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cash.atto.wallet.screens.OverviewScreen
import cash.atto.wallet.screens.SecretPhraseScreen
import cash.atto.wallet.screens.SettingsScreen
import cash.atto.wallet.screens.WelcomeScreen
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.AppUiState
import cash.atto.wallet.viewmodel.AppViewModel
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AttoAppAndroid() {
    AttoWalletTheme {
        KoinContext {
            val viewModel = koinViewModel<AppViewModel>()
            val uiState = viewModel.getAppState()
            val navController = rememberNavController()

            AttoNavHost(
                uiState = uiState,
                navController = navController
            )
        }
    }
}

@Composable
fun AttoNavHost(
    uiState: AppUiState,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = if (uiState.skipWelcome)
            AttoDestination.Overview.route
        else AttoDestination.Welcome.route,
        modifier = modifier,
        enterTransition = { slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Start,
            tween(700)
        ) },
        exitTransition = { slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Start,
            tween(700)
        ) },
        popEnterTransition = { slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.End,
            tween(700)
        ) },
        popExitTransition = { slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.End,
            tween(700)
        ) }
    ) {
        composable(route = AttoDestination.Overview.route) {
            OverviewScreen(
                onSettingsClicked = {
                    navController.navigate(AttoDestination.Settings.route)
                }
            )
        }

        composable(route = AttoDestination.SecretPhrase.route) {
            SecretPhraseScreen(
                onBackNavigation = { navController.navigateUp() },
                onBackupConfirmClicked = {
                    navController.navigate(AttoDestination.Overview.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                            saveState = true
                        }
                    }
                }
            )
        }

        composable(route = AttoDestination.Settings.route) {
            SettingsScreen(
                onBackNavigation = { navController.navigateUp() },
                onLogoutNavigation = {
                    navController.navigate(AttoDestination.Welcome.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = false
                        }
                    }
                }
            )
        }

        composable(route = AttoDestination.Welcome.route) {
            WelcomeScreen(
                onCreateSecretClicked = {
                    navController.navigate(AttoDestination.SecretPhrase.route)
                }
            )
        }
    }
}