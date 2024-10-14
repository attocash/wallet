package cash.atto.wallet

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cash.atto.wallet.screens.OverviewScreen
import cash.atto.wallet.screens.SecretPhraseScreen
import cash.atto.wallet.screens.SettingsScreen
import cash.atto.wallet.screens.WelcomeScreen
import cash.atto.wallet.ui.AttoWalletTheme

@Composable
fun AttoAppAndroid() {
    AttoWalletTheme {
        val navController = rememberNavController()
        
        AttoNavHost(navController = navController)
    }
}

@Composable
fun AttoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Welcome.route,
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
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) }
    ) {
        composable(route = Overview.route) {
            OverviewScreen(
                onSettingsClicked = {
                    navController.navigate(Settings.route)
                }
            )
        }

        composable(route = SecretPhrase.route) {
            SecretPhraseScreen(
                onBackNavigation = { navController.navigateUp() },
                onBackupConfirmClicked = {
                    navController.navigate(Overview.route)
                }
            )
        }

        composable(route = Settings.route) {
            SettingsScreen(
                onBackNavigation = { navController.navigateUp() }
            )
        }

        composable(route = Welcome.route) {
            WelcomeScreen(
                onCreateSecretClicked = {
                    navController.navigate(SecretPhrase.route)
                }
            )
        }
    }
}