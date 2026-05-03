package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.*
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class AttoButtonVariant(
    val backgroundColor: Color,
    val textColor: Color,
    val fontWeight: FontWeight,
    val borderColor: Color?,
) {
    Accent(
        backgroundColor = dark_accent,
        textColor = dark_accent_on,
        fontWeight = FontWeight.W700,
        borderColor = null,
    ),
    Secondary(
        backgroundColor = dark_accent_soft,
        textColor = dark_text_primary,
        fontWeight = FontWeight.W600,
        borderColor = dark_accent.copy(alpha = 0.2f),
    ),
    Outlined(
        backgroundColor = dark_surface,
        textColor = dark_text_primary,
        fontWeight = FontWeight.W600,
        borderColor = dark_border,
    ),
    Danger(
        backgroundColor = dark_danger,
        textColor = dark_bg,
        fontWeight = FontWeight.W700,
        borderColor = null,
    ),
}

@Composable
fun AttoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: AttoButtonVariant = AttoButtonVariant.Accent,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val backgroundColor =
        when {
            !enabled -> variant.backgroundColor.copy(alpha = 0.4f)
            hovered -> variant.hoverBackgroundColor()
            else -> variant.backgroundColor
        }
    val borderColor =
        when {
            !enabled -> variant.borderColor
            hovered -> variant.hoverBorderColor()
            else -> variant.borderColor
        }

    Row(
        modifier =
            modifier
                .height(56.dp)
                .clip(shape)
                .background(backgroundColor)
                .then(
                    if (borderColor != null) {
                        Modifier.border(1.dp, borderColor, shape)
                    } else {
                        Modifier
                    },
                ).then(
                    if (enabled) {
                        Modifier.pointerHoverIcon(PointerIcon.Hand)
                    } else {
                        Modifier
                    },
                ).clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun AttoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: AttoButtonVariant = AttoButtonVariant.Accent,
    icon: ImageVector? = null,
) {
    AttoButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = variant.textColor,
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = variant.textColor,
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = variant.fontWeight,
                    fontSize = 15.sp,
                ),
        )
    }
}

private fun AttoButtonVariant.hoverBackgroundColor(): Color =
    when (this) {
        AttoButtonVariant.Accent -> lerp(backgroundColor, dark_text_primary, 0.16f)
        AttoButtonVariant.Secondary -> dark_accent_soft_hover
        AttoButtonVariant.Outlined -> dark_surface_alt
        AttoButtonVariant.Danger -> lerp(backgroundColor, dark_text_primary, 0.08f)
    }

private fun AttoButtonVariant.hoverBorderColor(): Color? =
    when (this) {
        AttoButtonVariant.Secondary -> dark_border_subtle
        AttoButtonVariant.Outlined -> dark_border_muted
        else -> borderColor
    }

@Preview
@Composable
fun AttoButtonPreview() {
    AttoWalletTheme {
        AttoButton(text = "Button", onClick = {})
    }
}

@Preview
@Composable
fun AttoButtonAccentPreview() {
    AttoWalletTheme {
        AttoButton(
            text = "Accent Button",
            onClick = {},
            variant = AttoButtonVariant.Accent,
        )
    }
}
