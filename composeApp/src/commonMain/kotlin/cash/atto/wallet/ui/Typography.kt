package cash.atto.wallet.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.helvetica_neue_light
import attowallet.composeapp.generated.resources.helvetica_neue_medium
import attowallet.composeapp.generated.resources.pp_mori_regular
import attowallet.composeapp.generated.resources.pp_mori_semibold
import org.jetbrains.compose.resources.Font

@Composable
private fun light() = Font(Res.font.helvetica_neue_light, FontWeight.W300)

@Composable
private fun regular() = Font(Res.font.pp_mori_regular, FontWeight.W400)

@Composable
private fun medium() = Font(Res.font.helvetica_neue_medium, FontWeight.W500)

@Composable
private fun semibold() = Font(Res.font.pp_mori_semibold, FontWeight.W600)

@Composable
fun attoFontFamily() = FontFamily(
    fonts = listOf(
        light(),
        regular(),
        medium(),
        semibold()
    )
)


@Composable
fun attoWalletTypography() = Typography(
    displayLarge = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 80.sp
    ),
    displayMedium = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 56.sp
    ),
    displaySmall = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W300,
        fontSize = 42.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 24.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 18.sp
    ),
    titleMedium = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        letterSpacing = 4.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )
)