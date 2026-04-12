package cash.atto.wallet.components.secret

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cash.atto.wallet.components.common.AttoWordChip
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SecretWordCompact(
    ordinal: Int,
    word: String,
    hidden: Boolean,
    modifier: Modifier = Modifier,
) {
    AttoWordChip(
        ordinal = ordinal,
        word = word,
        hidden = hidden,
        modifier = modifier,
    )
}

@Composable
fun SecretWordExpanded(
    ordinal: Int,
    word: String,
    hidden: Boolean,
    modifier: Modifier = Modifier,
) {
    AttoWordChip(
        ordinal = ordinal,
        word = word,
        hidden = hidden,
        modifier = modifier,
    )
}

@Preview
@Composable
fun SecretWordCompactPreview() {
    AttoWalletTheme {
        SecretWordCompact(
            ordinal = 1,
            word = "Word",
            hidden = false,
        )
    }
}

@Preview
@Composable
fun SecretWordExpandedPreview() {
    AttoWalletTheme {
        SecretWordExpanded(
            ordinal = 1,
            word = "Word",
            hidden = false,
        )
    }
}
