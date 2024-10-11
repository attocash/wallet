package cash.atto.wallet.components.secret

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SecretWord(
    ordinal: Int,
    word: String
) {
    Row(Modifier.padding(vertical = 8.dp)) {
        Text("${ordinal})")
        Text(
            text = word,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Preview
@Composable
fun SecretWordPreview() {
    AttoWalletTheme {
        SecretWord(1, "Word")
    }
}