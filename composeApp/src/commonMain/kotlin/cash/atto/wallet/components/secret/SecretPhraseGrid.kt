package cash.atto.wallet.components.secret

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.divider
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SecretPhraseGrid(
    columns: Int,
    words: List<String>,
    hidden: Boolean
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns)
    ) {
        itemsIndexed(
            words.chunked(columns)
                .map { it + null }
                .flatten(),
            span = { index, item ->
                GridItemSpan(item?.let { 1 } ?: 3)
            }
        ) { index, item ->
            item?.let {
                SecretWord(
                    ordinal = index + 1,
                    word = it,
                    hidden = hidden
                )
            } ?: Divider(color = MaterialTheme.colors.divider)
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
                .toList(),
            hidden = false
        )
    }
}