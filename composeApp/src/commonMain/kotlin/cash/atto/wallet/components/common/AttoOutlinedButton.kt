package cash.atto.wallet.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AttoOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp,
        disabledElevation = 0.dp,
        hoveredElevation = 0.dp,
        focusedElevation = 0.dp
    ),
    shape: Shape = MaterialTheme.shapes.small,
    color: Color = MaterialTheme.colors.onSurface,
    contentPadding: PaddingValues = PaddingValues(19.dp),
    content: @Composable() (RowScope.() -> Unit)
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        shape = shape,
        border = BorderStroke(1.dp, color),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = color
        ),
        contentPadding = contentPadding,
        content = content
    )
}

@Preview
@Composable
fun AttoOutlinedButtonPreview() {
    AttoWalletTheme {
        AttoOutlinedButton(onClick = {}) {
            Text("Button")
        }
    }
}