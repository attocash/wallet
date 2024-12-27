package cash.atto.wallet.components.secret

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SecretWordCompact(
    ordinal: Int,
    word: String,
    hidden: Boolean
) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(10.dp))
            .background(
                color = MaterialTheme.colorScheme
                    .onSurface
                    .copy(alpha = 0.05f)
            )
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${ordinal})",
            style = MaterialTheme.typography.labelSmall
        )

        Text(
            text = if (hidden) "******" else word,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun SecretWordExpanded(
    ordinal: Int,
    word: String,
    hidden: Boolean
) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(10.dp))
            .background(
                color = MaterialTheme.colorScheme
                    .onSurface
                    .copy(alpha = 0.05f)
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${ordinal})"
        )
        Text(
            text = if (hidden) "******" else word,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Preview
@Composable
fun SecretWordCompactPreview() {
    AttoWalletTheme {
        SecretWordCompact(
            ordinal = 1,
            word = "Word",
            hidden = false
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
            hidden = false
        )
    }
}