package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.primaryGradient
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    content: @Composable() (ColumnScope.() -> Unit)
) = Column(
    modifier = modifier.fillMaxWidth()
        .background(
            Brush.horizontalGradient(
                MaterialTheme.colorScheme.primaryGradient
            )
        )
        .padding(
            start = 16.dp,
            top = 8.dp,
            end = 16.dp,
            bottom = 32.dp
        ),
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