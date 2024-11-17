package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

// Doesn't support interactionSource since this is actually a row
@Composable
fun AttoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    contentPadding: PaddingValues = PaddingValues(19.dp),
    content: @Composable() (RowScope.() -> Unit)
) {
    Surface(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true),
                enabled = enabled,
                onClick = onClick
            ),
        shape = shape,
    ) {
        ProvideTextStyle(
            value = MaterialTheme.typography.button
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xffFFE249),
                                Color(0xffFFD11F),
                                Color(0xffFF9C05)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(contentPadding),
                    content = content
                )
            }
        }
    }
}

@Preview
@Composable
fun AttoButtonPreview() {
    AttoWalletTheme {
        AttoButton(onClick = {}) {
            Text("Button")
        }
    }
}