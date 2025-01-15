package cash.atto.wallet

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import cash.atto.wallet.di.viewModelModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
//    val lifecycle = LifecycleRegistry()
//    val navComponent = DWNavigationComponent(DefaultComponentContext(lifecycle))

    startKoin { modules(viewModelModule) }

    CanvasBasedWindow(canvasElementId = "AttoWallet") {
        AttoAppWeb()
    }
}