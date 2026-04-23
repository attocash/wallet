package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppBar(onBackNavigation: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top =
                        WindowInsets.systemBars
                            .asPaddingValues()
                            .calculateTopPadding(),
                ).padding(start = 16.dp, top = 30.dp),
    ) {
        AttoBackButton(onClick = onBackNavigation)
    }
}

@Preview
@Composable
fun AppBarPreview() {
    AttoWalletTheme {
        AppBar {}
    }
}
