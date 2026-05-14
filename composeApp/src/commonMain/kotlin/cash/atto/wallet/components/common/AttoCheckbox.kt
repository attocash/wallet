package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_accent_on
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_border_muted
import cash.atto.wallet.ui.dark_surface_alt
import cash.atto.wallet.ui.dark_surface_raised
import cash.atto.wallet.ui.dark_text_dim

@Composable
fun AttoCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(5.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val borderColor =
        when {
            checked -> dark_accent
            hovered && enabled -> dark_border_muted
            enabled -> dark_border
            else -> dark_text_dim
        }
    val backgroundColor =
        when {
            checked -> dark_accent
            hovered && enabled -> dark_surface_alt
            else -> dark_surface_raised
        }

    Box(
        modifier =
            modifier
                .size(18.dp)
                .clip(shape)
                .background(
                    color = backgroundColor,
                    shape = shape,
                ).border(
                    width = 1.dp,
                    color = borderColor,
                    shape = shape,
                ).then(
                    if (enabled) {
                        Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onCheckedChange(!checked) },
                            )
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = dark_accent_on,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}
