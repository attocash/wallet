package cash.atto.wallet.components.secret

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SecretPhraseGridCompact(
    words: List<String>,
    hidden: Boolean
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3)
    ) {
        itemsIndexed(
            words
        ) { index, item ->
            SecretWordCompact(
                ordinal = index + 1,
                word = item,
                hidden = hidden
            )
        }
    }
}

@Composable
fun SecretPhraseGridExpanded(
    words: List<String>,
    hidden: Boolean
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier.fillMaxWidth(0.7f),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        itemsIndexed(
            words
        ) { index, item ->
            SecretWordExpanded(
                ordinal = index + 1,
                word = item,
                hidden = hidden
            )
        }
    }
}

@Preview
@Composable
fun SecretPhraseGridCompactPreview() {
    AttoWalletTheme {
        SecretPhraseGridCompact(
            words = (1..24)
                .map { "Word$it" }
                .toList(),
            hidden = false
        )
    }
}

@Preview
@Composable
fun SecretPhraseGridExpandedPreview() {
    AttoWalletTheme {
        SecretPhraseGridExpanded(
            words = (1..24)
                .map { "Word$it" }
                .toList(),
            hidden = false
        )
    }
}