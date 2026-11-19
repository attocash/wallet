package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
internal fun AttoValueText(
    text: AnnotatedString,
    color: Color,
    style: TextStyle,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 14.sp,
) {
    val textMeasurer = rememberTextMeasurer()
    // Keep the full-size line's height even when the value needs a smaller font to fit.
    val height =
        with(LocalDensity.current) {
            textMeasurer
                .measure("0", style = style)
                .size.height
                .toDp()
        }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.Center,
    ) {
        val availableWidth = constraints.maxWidth
        val fittedStyle =
            remember(text, style, minFontSize, availableWidth, textMeasurer) {
                var candidate = style
                while (candidate.fontSize > minFontSize) {
                    val width = textMeasurer.measure(text, style = candidate, softWrap = false, maxLines = 1).size.width
                    if (width <= availableWidth) break

                    val size = candidate.fontSize.value
                    val scaledSize = size * availableWidth / width
                    candidate = candidate.copy(fontSize = minOf(size - 1, scaledSize).coerceAtLeast(minFontSize.value).sp)
                }
                candidate
            }

        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            style = fittedStyle,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
