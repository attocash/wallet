package cash.atto.wallet.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val atto_caption = Color.DarkGray
val atto_divider_color = Color.LightGray
private val atto_red = Color(0xFFE30425)
private val atto_white = Color.White
private val atto_purple_700 = Color(0xFF720D5D)
private val atto_purple_800 = Color(0xFF5D1049)
private val atto_purple_900 = Color(0xFF4E0D3A)

val attoColors = lightColors(
    primary = atto_purple_800,
    secondary = atto_red,
    surface = atto_purple_900,
    onSurface = atto_white,
    primaryVariant = atto_purple_700
)

val BottomSheetShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

@Composable
fun AttoWalletTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = attoColors, typography = attoWalletTypography()) {
        content()
    }
}