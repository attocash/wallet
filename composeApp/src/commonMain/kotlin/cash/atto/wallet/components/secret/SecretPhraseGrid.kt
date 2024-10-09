package cash.atto.wallet.components.secret

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SecretPhraseGrid(
    columns: Int,
    words: List<String>
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns)
    ) {
        itemsIndexed(words) { index, item ->
            SecretWord(index + 1, item)
        }
    }
}

@Preview
@Composable
fun SecretPhraseGridPreview() {
    AttoWalletTheme {
        SecretPhraseGrid(
            columns = 3,
            words = (1..24)
                .map { "Word$it" }
                .toList()
        )
    }
}