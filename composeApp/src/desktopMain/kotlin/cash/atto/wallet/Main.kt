package cash.atto.wallet

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cash.atto.wallet.di.initKoin
import java.awt.Dimension

fun main() {
    application {
        initKoin()

        val windowState = rememberWindowState()
        Window(
            onCloseRequest = ::exitApplication,
            title = "Atto Wallet",
        ) {
            window.minimumSize =
                Dimension(
                    1396,
                    900,
                )

            AttoApp()
        }
    }
}
