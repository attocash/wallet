package cash.atto.wallet

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cash.atto.wallet.screens.BackupSecretPhraseScreen
import cash.atto.wallet.screens.CreatePasswordScreen
import cash.atto.wallet.screens.EnterPassword
import cash.atto.wallet.screens.ImportSecretScreen
import cash.atto.wallet.screens.OverviewScreenAndroid
import cash.atto.wallet.screens.RepresentativeScreen
import cash.atto.wallet.screens.SafetyWarningScreen
import cash.atto.wallet.screens.SecretBackupConfirmScreen
import cash.atto.wallet.screens.SecretPhraseScreen
import cash.atto.wallet.screens.SendConfirmScreen
import cash.atto.wallet.screens.SendFromScreenAndroid
import cash.atto.wallet.screens.SendResultScreen
import cash.atto.wallet.screens.SettingsScreenAndroid
import cash.atto.wallet.screens.WelcomeScreen
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.AppUiState
import cash.atto.wallet.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AttoAppAndroid() {
    AttoWalletTheme {
        KoinContext {
            val viewModel = koinViewModel<AppViewModel>()
            val uiState = viewModel.state.collectAsState()
            val navController = rememberNavController()

            AttoNavHost(
                uiState = uiState.value,
                navController = navController,
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
    navController: NavHostController,
    modifier: Modifier = Modifier,
    submitPassword: suspend (String?) -> Boolean
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    when (uiState.shownScreen) {
        AppUiState.ShownScreen.LOADER -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colors.surface)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
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

        else -> {
            NavHost(
                navController = navController,
                startDestination = when (uiState.shownScreen) {
                    AppUiState.ShownScreen.OVERVIEW -> AttoDestination.Overview.route
                    AppUiState.ShownScreen.PASSWORD_CREATE -> AttoDestination.CreatePassword.route
                    else -> AttoDestination.Welcome.route
                },
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
                composable(route = AttoDestination.BackupSecret.route) {
                    BackupSecretPhraseScreen(
                        onBackNavigation = { navController.navigateUp() }
                    )
                }

                composable(route = AttoDestination.CreatePassword.route) {
                    CreatePasswordScreen(
                        onBackNavigation = { navController.navigateUp() },
                        onConfirmClick = {
                            navController.navigate(AttoDestination.Overview.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                    saveState = true
                                }
                            }
                        }
                    )
                }

                composable(route = AttoDestination.ImportSecret.route) {
                    ImportSecretScreen(
                        onBackNavigation = { navController.navigateUp() },
                        onImportAccount = {
                            navController.navigate(AttoDestination.Overview.route)
                        }
                    )
                }

                composable(route = AttoDestination.Overview.route) {
                    OverviewScreenAndroid(
                        onSettingsClicked = {
                            navController.navigate(AttoDestination.Settings.route)
                        },
                        onSendClicked = {
                            navController.navigate(AttoDestination.SendFrom.route)
                        }
                    )
                }

                composable(route = AttoDestination.Representative.route) {
                    RepresentativeScreen(
                        onBackNavigation = { navController.navigateUp() }
                    )
                }

                composable(route = AttoDestination.SafetyWarning.route) {
                    SafetyWarningScreen(
                        onBackNavigation = { navController.navigateUp() },
                        onConfirmClicked = {
                            navController.navigate(AttoDestination.SecretPhrase.route)
                        }
                    )
                }

                composable(route = AttoDestination.SecretBackupConfirmation.route) {
                    SecretBackupConfirmScreen(
                        onBackNavigation = { navController.navigateUp() },
                        onConfirmClicked = {
                            navController.navigate(AttoDestination.CreatePassword.route)
                        }
                    )
                }

                composable(route = AttoDestination.SecretPhrase.route) {
                    SecretPhraseScreen(
                        onBackNavigation = { navController.navigateUp() },
                        onBackupConfirmClicked = {
                            navController.navigate(AttoDestination.SecretBackupConfirmation.route)
                        }
                    )
                }

                composable(route = AttoDestination.SendConfirm.route) {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides viewModelStoreOwner
                    ) {
                        SendConfirmScreen(
                            onBackNavigation = { navController.navigateUp() },
                            onConfirm = {
                                navController.navigate(AttoDestination.SendResult.route)
                            },
                            onCancel = {
                                navController.popBackStack(
                                    route = AttoDestination.Overview.route,
                                    inclusive = false
                                )

                                navController.navigateUp()
                            }
                        )
                    }
                }

                composable(route = AttoDestination.SendFrom.route) {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides viewModelStoreOwner
                    ) {
                        SendFromScreenAndroid(
                            onBackNavigation = { navController.navigateUp() },
                            onSendClicked = {
                                navController.navigate(AttoDestination.SendConfirm.route)
                            }
                        )
                    }
                }

                composable(route = AttoDestination.SendResult.route) {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides viewModelStoreOwner
                    ) {
                        SendResultScreen(
                            onClose = {
                                navController.popBackStack(
                                    route = AttoDestination.Overview.route,
                                    inclusive = false
                                )

                                navController.navigateUp()
                            }
                        )
                    }
                }

                composable(route = AttoDestination.Settings.route) {
                    SettingsScreenAndroid(
                        onBackNavigation = { navController.navigateUp() },
                        onBackupSecretNavigation = {
                            navController.navigate(AttoDestination.BackupSecret.route)
                        },
                        onRepresentativeNavigation = {
                            navController.navigate(AttoDestination.Representative.route)
                        },
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
                            navController.navigate(AttoDestination.SafetyWarning.route)
                        },
                        onImportSecretClicked = {
                            navController.navigate(AttoDestination.ImportSecret.route)
                        }
                    )
                }
            }
        }
    }
}