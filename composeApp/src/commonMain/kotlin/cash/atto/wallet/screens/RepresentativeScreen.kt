package cash.atto.wallet.screens

import androidx.compose.runtime.Composable
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RepresentativeScreen() {
    RepresentativeScreenContent()
}

@Composable
fun RepresentativeScreenContent() {}

@Preview
@Composable
fun RepresentativeScreenContentPreview() {
    AttoWalletTheme {
        RepresentativeScreenContent()
    }
}