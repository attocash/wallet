package cash.atto.wallet.components.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AttoOutlinedTextCard(
    text: String,
    color: Color
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .border(
            width = 1.dp,
            color = color,
            shape = MaterialTheme.shapes.medium
        )
        .padding(19.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            color = color
        )
    }
}

@Preview
@Composable
fun AttoOutlinedTextCardPreview() {
    AttoWalletTheme {
        AttoOutlinedTextCard(
            text = "Text",
            color = MaterialTheme.colors.onSurface
        )
    }
}