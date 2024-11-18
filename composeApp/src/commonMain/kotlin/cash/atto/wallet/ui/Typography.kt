package cash.atto.wallet.ui

import androidx.compose.material.Typography
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
import attowallet.composeapp.generated.resources.raleway_light
import attowallet.composeapp.generated.resources.raleway_medium
import attowallet.composeapp.generated.resources.raleway_regular
import attowallet.composeapp.generated.resources.raleway_semibold
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
private fun attoFontFamily() = FontFamily(fonts = listOf(
    light(),
    regular(),
    medium(),
    semibold()
))

@Composable
fun attoWalletTypography() = Typography(
    h1 = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 80.sp
    ),
    h2 = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 56.sp
    ),
    h3 = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W300,
        fontSize = 42.sp
    ),
    h4 = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 34.sp
    ),
    h5 = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 24.sp
    ),
    h6 = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 20.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W500,
        fontSize = 16.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        letterSpacing = 4.sp
    ),
    body1 = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 14.sp
    ),
    button = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W600,
        fontSize = 16.sp
    ),
    caption = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 14.sp
    ),
    overline = TextStyle(
        fontFamily = attoFontFamily(),
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )
)