package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    content: @Composable() (ColumnScope.() -> Unit)
) = Column(
        modifier = modifier.fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
) {
    BottomSheetStud()
    content()
}

@Preview
@Composable
fun BottomSheetPreview() {
    AttoWalletTheme {
        BottomSheet {
            Text("Preview")
        }
    }
}