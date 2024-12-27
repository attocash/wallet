package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AttoLoader(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    darkMode: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(10f)
            .pointerInput(Unit) {
                detectTapGestures {}
            }
    ) {
        val backgroundColor = if (darkMode)
            MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.surface

        Box(
            Modifier.fillMaxSize()
                .background(color = backgroundColor.copy(alpha))
        )

        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .width(64.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
fun AttoLoaderPreview() {
    AttoWalletTheme {
        AttoLoader()
    }
}