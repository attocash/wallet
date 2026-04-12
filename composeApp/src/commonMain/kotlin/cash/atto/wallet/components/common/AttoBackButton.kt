package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_surface

@Composable
fun AttoRoundButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .size(40.dp)
                .background(dark_surface, CircleShape)
                .border(1.dp, dark_border, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun AttoBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AttoRoundButton(onClick = onClick, modifier = modifier) {
        Text(
            text = "‹",
            color = Color.White,
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                ),
        )
    }
}
