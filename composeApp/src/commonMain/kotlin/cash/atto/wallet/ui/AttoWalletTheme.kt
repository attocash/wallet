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
    secondary = gold_900,
    secondaryVariant = gold_100,
    background = Color.Black,
    surface = Color.White,
    error = red_900,
    onPrimary = gray_800,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = gray_800,
    onError = Color.White
)

val Colors.back: Color
    get() = gray_700

val Colors.divider: Color
    get() = gray_800

val Colors.setting: Color
    get() = ochre_600

val Colors.success: Color
    get() = green_700

val Colors.primaryGradient: List<Color>
    get() = listOf(
        Color(0xffFFE249),
        Color(0xffFFD11F),
        Color(0xffFF9C05)
    )

val Colors.secondaryGradient: List<Color>
    get() = listOf(
        Color(0xffFF9B36),
        Color(0xffEC6C00),
        Color(0xffFC8704)
    )

val Colors.errorGradient: List<Color>
    get() = listOf(
        Color(0xffDA0004),
        Color(0xffFF0004),
        Color(0xffDA0004)
    )

val attoShapes = Shapes(
    small = RoundedCornerShape(24.dp),
    medium = RoundedCornerShape(20.dp),
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