package cash.atto.wallet.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val attoColors = lightColors(
    primary = gold_600,
//    primaryVariant = brown_600,
    secondary = lime_500,
    secondaryVariant = green_400,
    background = gray_900,
    surface = Color.White,
    error = red_500,
    onPrimary = gray_800,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = gray_800,
    onError = Color.White
)

val Colors.back: Color
    get() = gray_700

val Colors.divider: Color
    @Composable
    get() = gray_800

val attoShapes = Shapes(
    small = RoundedCornerShape(24.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(8.dp)
)

val BottomSheetShape = RoundedCornerShape(
    topStart = 50.dp,
    topEnd = 50.dp
)

@Composable
fun AttoWalletTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = attoColors,
        typography = attoWalletTypography(),
        shapes = attoShapes
    ) {
        content()
    }
}