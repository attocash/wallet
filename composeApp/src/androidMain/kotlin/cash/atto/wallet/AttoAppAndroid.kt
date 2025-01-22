package cash.atto.wallet

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cash.atto.wallet.components.common.AttoLoader
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
import cash.atto.wallet.screens.WelcomeScreenAndroid
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

        else -> {
            NavHost(
                navController = navController,
                startDestination = when (uiState.shownScreen) {
                    AppUiState.ShownScreen.OVERVIEW -> AttoDestination.Overview.route
                    AppUiState.ShownScreen.PASSWORD_CREATE -> AttoDestination.CreatePassword.route
                    else -> AttoDestination.Welcome.route
                },
                modifier = modifier,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(SLIDE_DURATION)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(SLIDE_DURATION)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(SLIDE_DURATION)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(SLIDE_DURATION)
                    )
                }
            ) {
                composable(
                    route = AttoDestination.BackupSecret.route,
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            tween(SLIDE_DURATION)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            tween(SLIDE_DURATION)
                        )
                    }
                ) {
                    BackupSecretPhraseScreen(
                        onBackNavigation = { navController.navigateUp() }
                    )
                }

                composable(
                    route = AttoDestination.CreatePassword.route,
                    exitTransition = { fadeOut(tween(FADE_DURATION)) }
                ) {
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

                composable(
                    route = AttoDestination.ImportSecret.route,
                    enterTransition = { fadeIn(tween(FADE_DURATION)) },
                    exitTransition = { fadeOut(tween(FADE_DURATION)) }
                ) {
                    ImportSecretScreen(
                        onBackNavigation = { navController.navigateUp() },
                        onImportAccount = {
                            navController.navigate(AttoDestination.CreatePassword.route)
                        }
                    )
                }

                composable(
                    route = AttoDestination.Overview.route,
                    enterTransition = { fadeIn(tween(FADE_DURATION)) },
                    exitTransition = { fadeOut(tween(FADE_DURATION)) }
                ) {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides viewModelStoreOwner
                    ) {
                        OverviewScreenAndroid(
                            onSettingsClicked = {
                                navController.navigate(AttoDestination.Settings.route)
                            },
                            onSendClicked = {
                                navController.navigate(AttoDestination.SendFrom.route)
                            }
                        )
                    }
                }

                composable(
                    route = AttoDestination.Representative.route,
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            tween(SLIDE_DURATION)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            tween(SLIDE_DURATION)
                        )
                    }
                ) {
                    RepresentativeScreen(
                        onBackNavigation = { navController.navigateUp() }
                    )
                }

                composable(
                    route = AttoDestination.SafetyWarning.route,
                    enterTransition = { fadeIn(tween(FADE_DURATION)) },
                    exitTransition = { fadeOut(tween(FADE_DURATION)) },
                    popEnterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(700)
                        )
                    },
                ) {
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

                composable(
                    route = AttoDestination.SendConfirm.route,
                    enterTransition = { fadeIn(tween(FADE_DURATION)) },
                    exitTransition = { fadeOut(tween(FADE_DURATION)) }
                ) {
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

                composable(
                    route = AttoDestination.SendFrom.route,
                    enterTransition = { fadeIn(tween(FADE_DURATION)) },
                    exitTransition = { fadeOut(tween(FADE_DURATION)) }
                ) {
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

                composable(
                    route = AttoDestination.SendResult.route,
                    enterTransition = { fadeIn(tween(FADE_DURATION)) },
                    exitTransition = { fadeOut(tween(FADE_DURATION)) }
                ) {
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

                composable(
                    route = AttoDestination.Settings.route,
                    enterTransition = { fadeIn(tween(FADE_DURATION)) },
                    exitTransition = { fadeOut(tween(FADE_DURATION)) }
                ) {
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

                composable(
                    route = AttoDestination.Welcome.route,
                    enterTransition = { fadeIn(tween(FADE_DURATION)) },
                    exitTransition = { fadeOut(tween(FADE_DURATION)) }
                ) {
                    WelcomeScreenAndroid(
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

private const val SLIDE_DURATION = 700
private const val FADE_DURATION = 700