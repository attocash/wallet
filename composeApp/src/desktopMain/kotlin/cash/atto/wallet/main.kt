package cash.atto.wallet

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cash.atto.wallet.di.initKoin
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import runOnUiThread
import java.awt.Dimension

fun main() {
    val lifecycle = LifecycleRegistry()
    val navComponent = runOnUiThread {
        NavigationComponent(DefaultComponentContext(lifecycle))
    }

    application {
        initKoin()

        val windowState = rememberWindowState()
        Window(
            onCloseRequest = ::exitApplication,
            title = "Atto Wallet",
        ) {
            window.minimumSize = Dimension(
                1344,
                800
            )

            LifecycleController(
                lifecycleRegistry = lifecycle,
                windowState = windowState,
                windowInfo = LocalWindowInfo.current,
            )

            AttoAppDesktop(navComponent)
        }
    }
}