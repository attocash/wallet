package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
        textColor = Color(0xFF111827),
        fontWeight = FontWeight.W700,
        borderColor = null,
    ),
    Secondary(
        backgroundColor = dark_accent_soft,
        textColor = dark_text_primary,
        fontWeight = FontWeight.W600,
        borderColor = Color(0x33F7B500),
    ),
    Outlined(
        backgroundColor = dark_surface,
        textColor = Color.White,
        fontWeight = FontWeight.W600,
        borderColor = dark_border,
    ),
    Danger(
        backgroundColor = Color(0xFFDA0004),
        textColor = Color.White,
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
    Row(
        modifier =
            modifier
                .height(56.dp)
                .clip(shape)
                .background(
                    if (enabled) {
                        variant.backgroundColor
                    } else {
                        variant.backgroundColor.copy(alpha = 0.4f)
                    },
                ).then(
                    if (variant.borderColor != null) {
                        Modifier.border(1.dp, variant.borderColor, shape)
                    } else {
                        Modifier
                    },
                ).clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
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
