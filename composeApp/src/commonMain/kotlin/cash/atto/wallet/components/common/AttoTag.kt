package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.dark_accent

@Composable
fun AttoTag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = dark_accent,
    onClick: (() -> Unit)? = null,
) {
    val interactiveModifier =
        if (onClick != null) {
            Modifier
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(onClick = onClick)
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.12f))
                .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .then(interactiveModifier)
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W700),
        )
    }
}
