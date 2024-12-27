package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.divider
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BottomSheetStud() = Box(
    modifier = Modifier.height(4.dp)
        .width(32.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(color = MaterialTheme.colorScheme.divider)
)

@Preview
@Composable
fun BottomSheetStudPreview() {
    AttoWalletTheme {
        BottomSheetStud()
    }
}