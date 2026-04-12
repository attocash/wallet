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
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_surface_alt

/**
 * Standard dark-theme card used throughout the redesigned screens.
 *
 * Provides a [dark_surface] background with a 1 dp [dark_border] outline and
 * rounded corners. On hover the background lightens to [dark_surface_alt] and
 * the border gains a subtle accent tint, matching the WelcomeScreen card style.
 */
@Composable
fun AttoCard(
    modifier: Modifier = Modifier,
    background: Color = dark_surface,
    hoverBackground: Color = dark_surface_alt,
    border: Color = dark_border,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val cornerRadius = 12.dp
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = if (hovered) hoverBackground else background,
                    shape = RoundedCornerShape(cornerRadius),
                ).border(
                    width = 1.dp,
                    color = if (hovered) dark_accent.copy(alpha = 0.32f) else border,
                    shape = RoundedCornerShape(cornerRadius),
                ).let {
                    if (onClick == null) {
                        it
                    } else {
                        it.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick,
                        )
                    }
                }.padding(contentPadding),
        content = content,
    )
}
