package cash.atto.wallet.components.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary

@Composable
fun AttoScreenTitle(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        text = text,
        modifier = modifier,
        color = dark_text_primary,
        textAlign = textAlign,
        style =
            MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.W600,
                fontSize = 36.sp,
                lineHeight = 39.6.sp,
                letterSpacing = (-0.72).sp,
            ),
    )
}

@Composable
fun AttoScreenSubtitle(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        text = text,
        modifier = modifier,
        color = dark_text_secondary,
        textAlign = textAlign,
        style =
            MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            ),
    )
}
