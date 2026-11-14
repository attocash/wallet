package cash.atto.wallet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.bindToBrowserNavigation
import cash.atto.wallet.components.common.QrScannerView
import cash.atto.wallet.components.common.isQrScannerSupported
import cash.atto.wallet.di.viewModelModule
import cash.atto.wallet.screens.OgImageScreen
import cash.atto.wallet.ui.AttoFontLoader
import cash.atto.wallet.ui.AttoWalletTheme
import kotlinx.browser.window
import kotlinx.coroutines.flow.first
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    if (isOgImagePreview()) {
        ComposeViewport(viewportContainerId = "AttoWallet") {
            AttoFontLoader {
                AttoWalletTheme {
                    OgImageScreen()
                }
            }
        }
        return
    }

    val launchRequest = initialWebSendRequest()
    val initialDestination = initialWebDestination(launchRequest)

    startKoin { modules(viewModelModule) }

    val qrScannerAvailable = isQrScannerSupported()

    ComposeViewport(viewportContainerId = "AttoWallet") {
        AttoFontLoader {
            AttoApp(
                initialDestination = initialDestination,
//            debugScreen = webDebugScreen(),
//            debugPassword = webDebugPassword(),
//            initialNavOverride = webDebugMainScreen(),
                initialNavOverride = launchRequest?.let { MainScreenNavDestination.SEND },
                initialSendPaymentRequest = launchRequest?.paymentRequest,
                initialOpenSendConfirm = launchRequest?.openConfirm == true,
                onAuthenticated = {
                    if (launchRequest != null) {
                        clearWebQueryParams()
                    }
                },
                onAuthenticationNavigation = { replaceWebNavigationHistoryEntry() },
                onNavHostReady = { navController ->
                    if (launchRequest != null) {
                        // Browser binding removes query parameters; keep payment links reloadable until unlocked.
                        navController.currentBackStackEntryFlow.first { it.destination.hasRoute<AttoDestination.DesktopMain>() }
                    }
                    navController.bindToBrowserNavigation()
                },
                qrScannerContent =
                    if (qrScannerAvailable) {
                        { onResult, onError, _ ->
                            QrScannerView(
                                modifier =
                                    Modifier
                                        .fillMaxWidth(),
                                onQrCodeScanned = onResult,
                                onScanError = onError,
                            )
                        }
                    } else {
                        null
                    },
            )
        }
    }
}

private fun initialWebDestination(launchRequest: WebSendLaunchRequest?): AttoDestination =
    when {
        launchRequest != null -> {
            AttoDestination.DesktopMain
        }

        else -> {
            when (webQueryParam("screen")) {
                "welcome" -> AttoDestination.Welcome

                "overview",
                "send",
                "receive",
                "transactions",
                "settings",
                "staking",
                -> AttoDestination.DesktopMain

                "secretPhrase", "recovery-phrase" -> AttoDestination.RecoveryPhrase

                "importSecret", "import-phrase" -> AttoDestination.ImportPhrase

                "createPassword", "create-password" -> AttoDestination.CreatePassword

                else -> AttoDestination.Welcome
            }
        }
    }

internal fun webDebugScreen(): String? = webQueryParam("screen")

internal fun webDebugPassword(): String? = webQueryParam("password")

internal fun webDebugMainScreen(): MainScreenNavDestination? =
    when (webDebugScreen()) {
        "overview" -> MainScreenNavDestination.OVERVIEW
        "send" -> MainScreenNavDestination.SEND
        "receive" -> MainScreenNavDestination.RECEIVE
        "transactions" -> MainScreenNavDestination.TRANSACTIONS
        "settings" -> MainScreenNavDestination.SETTINGS
        "staking" -> MainScreenNavDestination.STAKING
        else -> null
    }

private fun webQueryParam(key: String): String? = webQueryParams()[key]

private fun isOgImagePreview(): Boolean =
    webQueryParam("screen") == "og-image" ||
        window.location.pathname
            .trim('/')
            .equals("og-image", ignoreCase = true)

private data class WebSendLaunchRequest(
    val paymentRequest: String,
    val openConfirm: Boolean,
)

private fun initialWebSendRequest(): WebSendLaunchRequest? {
    val receiverAddress = webQueryParam("receiverAddress")
    val normalizedAddress = receiverAddress ?: return null
    val amount = webQueryParam("amount")
    val sanitizedAmount = amount?.filter(Char::isDigit)?.takeIf { it.isNotBlank() }

    return WebSendLaunchRequest(
        paymentRequest =
            if (sanitizedAmount == null) {
                normalizedAddress
            } else {
                "$normalizedAddress?amount=$sanitizedAmount"
            },
        openConfirm = sanitizedAmount != null,
    )
}

private fun webQueryParams(): Map<String, String> =
    window.location.search
        .removePrefix("?")
        .split("&")
        .mapNotNull { part ->
            if (part.isBlank()) return@mapNotNull null

            val pieces = part.split("=", limit = 2)
            val rawKey = pieces.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val rawValue = pieces.getOrElse(1) { "" }

            decodeQueryComponent(rawKey) to decodeQueryComponent(rawValue)
        }.toMap()

@OptIn(ExperimentalWasmJsInterop::class)
private fun decodeQueryComponent(value: String): String = js("decodeURIComponent(value.replace(/\\+/g, '%20'))")

@OptIn(ExperimentalWasmJsInterop::class)
private fun clearWebQueryParams() {
    js(
        """
        const url = new URL(window.location.href);
        window.history.replaceState(window.history.state, document.title, url.pathname + url.hash);
        """,
    )
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun replaceWebNavigationHistoryEntry() {
    // Make authentication redirects replace browser history so Back can continue past old protected routes.
    js("window.history.replaceState(null, document.title, window.location.href);")
}
