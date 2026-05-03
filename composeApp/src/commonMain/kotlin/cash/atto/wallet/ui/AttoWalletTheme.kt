package cash.atto.wallet.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val attoColors =
    darkColorScheme(
        primary = dark_accent,
        onPrimary = dark_accent_on,
        primaryContainer = dark_accent_soft,
        onPrimaryContainer = dark_text_primary,
        secondary = dark_accent,
        onSecondary = dark_accent_on,
        secondaryContainer = dark_surface_alt,
        onSecondaryContainer = dark_text_primary,
        tertiary = dark_violet,
        onTertiary = dark_text_primary,
        background = dark_bg,
        onBackground = dark_text_primary,
        surface = dark_surface,
        onSurface = dark_text_primary,
        surfaceVariant = dark_surface_alt,
        onSurfaceVariant = dark_text_secondary,
        error = dark_danger,
        onError = dark_bg,
        outline = dark_border,
        outlineVariant = dark_border_muted,
    )

val attoShapes =
    Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
    )

@Composable
fun AttoWalletTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = attoColors,
        typography = attoWalletTypography(),
        shapes = attoShapes,
    ) {
        content()
    }
}
