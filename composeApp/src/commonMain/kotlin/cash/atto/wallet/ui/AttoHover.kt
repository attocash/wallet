package cash.atto.wallet.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

fun attoHoverTint(
    baseTint: Color,
    hovered: Boolean,
    highlight: Float = 0.4f,
): Color = if (hovered) lerp(baseTint, Color.White, highlight) else baseTint
