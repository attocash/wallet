package cash.atto.wallet.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

//val attoColors = lightColors(
//    primary = brown_700,
//    primaryVariant = brown_600,
//    secondary = blue_900,
//    secondaryVariant = blue_800,
//    background = brown_100,
//    surface = brown_050,
//    error = red_900,
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onBackground = Color.Black,
//    onSurface = Color.Black,
//    onError = Color.White
//)

val attoColors = lightColors(
    primary = yellow_500,
//    primaryVariant = brown_600,
//    secondary = blue_900,
//    secondaryVariant = blue_800,
    background = gray_900,
    surface = Color.Black,
    error = red_900,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

val Colors.divider: Color
    @Composable
    get() = gray_800

val attoShapes = Shapes(
    small = RoundedCornerShape(24.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(8.dp)
)

val BottomSheetShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp
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