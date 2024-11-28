package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AttoOnboardingContainer(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable (ColumnScope) -> Unit
) = Column(
    modifier = modifier.clip(MaterialTheme.shapes.medium)
        .background(color = MaterialTheme.colors.surface)
        .border(
            width = 1.dp,
            color = MaterialTheme.colors
                .secondaryVariant
                .copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.medium
        )
        .padding(40.dp),
    verticalArrangement = verticalArrangement,
    horizontalAlignment = horizontalAlignment,
    content = content
)

@Preview
@Composable
fun AttoOnboardingContainerPreview() {
    AttoWalletTheme {
        AttoOnboardingContainer {
            Text("content")
        }
    }
}