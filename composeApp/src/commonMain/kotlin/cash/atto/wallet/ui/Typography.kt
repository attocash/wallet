package cash.atto.wallet.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.noto_sans_variable
import org.jetbrains.compose.resources.Font

@Composable
private fun notoSans(weight: FontWeight) = Font(Res.font.noto_sans_variable, weight)

@Composable
fun attoFontFamily() =
    FontFamily(
        fonts =
            listOf(
                notoSans(FontWeight.W400),
                notoSans(FontWeight.W500),
                notoSans(FontWeight.W600),
                notoSans(FontWeight.W700),
                notoSans(FontWeight.W800),
            ),
    )

@Composable
fun attoWalletTypography(): Typography {
    val fontFamily = attoFontFamily()

    return Typography(
        displayLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 80.sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 56.sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W300,
                fontSize = 42.sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 34.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 24.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 20.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 18.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 16.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 12.sp,
            ),
    )
}
