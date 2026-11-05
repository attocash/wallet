package cash.atto.wallet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.noto_sans_variable
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoLoader
import cash.atto.wallet.components.common.AttoPanelCard
import cash.atto.wallet.components.common.preloadBrowserPasswordFont
import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun AttoFontLoader(content: @Composable () -> Unit) {
    // Load every weight before checking readiness so none first appear as the fallback font.
    val fonts = attoFontWeights.map { preloadFont(Res.font.noto_sans_variable, it).value }
    val composeFontsLoaded by rememberUpdatedState(fonts.all { it != null })
    var fontsReady by remember { mutableStateOf(false) }
    var fontLoadFailed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            withTimeout(30.seconds) {
                preloadBrowserPasswordFont()
                snapshotFlow { composeFontsLoaded }.first { it }
            }
            fontsReady = true
        } catch (error: Throwable) {
            if (error is CancellationException && error !is TimeoutCancellationException) throw error
            fontLoadFailed = true
        }
    }

    if (fontsReady) {
        content()
    } else {
        AttoWalletTheme {
            if (fontLoadFailed) {
                Box(
                    modifier = Modifier.fillMaxSize().background(dark_bg).padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AttoPanelCard(modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth()) {
                        Text("Unable to load wallet", color = dark_text_primary, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Check your connection and reload to try again.",
                            color = dark_text_secondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        AttoButton(text = "Reload wallet", onClick = { window.location.reload() }, modifier = Modifier.fillMaxWidth())
                    }
                }
            } else {
                AttoLoader()
            }
        }
    }
}
