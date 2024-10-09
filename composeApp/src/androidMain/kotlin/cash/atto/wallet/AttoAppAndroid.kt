package cash.atto.wallet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import cash.atto.wallet.ui.AttoWalletTheme

@Composable
fun AttoAppAndroid() {
    AttoWalletTheme {
        val navController = rememberNavController()
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
        modifier = modifier
    ) {}
}