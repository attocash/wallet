package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_border_muted
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_surface_alt

/**
 * Standard dark-theme card used throughout the redesigned screens.
 *
 * Provides a [dark_surface] background with a 1 dp [dark_border] outline and
 * rounded corners. On hover the background lightens to [dark_surface_alt] and
 * the border darkens slightly to keep the edge readable without using accent color.
 */
@Composable
fun AttoCard(
    modifier: Modifier = Modifier,
    background: Color = dark_surface,
    hoverBackground: Color = dark_surface_alt,
    border: Color = dark_border,
    hoverBorder: Color = dark_border_muted,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val cornerRadius = 12.dp
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val hovered by resolvedInteractionSource.collectIsHoveredAsState()

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = if (hovered) hoverBackground else background,
                    shape = RoundedCornerShape(cornerRadius),
                ).border(
                    width = 1.dp,
                    color = if (hovered) hoverBorder else border,
                    shape = RoundedCornerShape(cornerRadius),
                ).let {
                    if (onClick == null) {
                        it
                    } else {
                        it.pointerHoverIcon(PointerIcon.Hand).clickable(
                            interactionSource = resolvedInteractionSource,
                            indication = null,
                            onClick = onClick,
                        )
                    }
                }.padding(contentPadding),
        content = content,
    )
}
