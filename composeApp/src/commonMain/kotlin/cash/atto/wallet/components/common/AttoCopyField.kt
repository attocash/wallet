package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_field_deep
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_tertiary

@Composable
fun AttoCopyField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    displayValue: String = value,
    labelTrailingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (labelTrailingContent == null) {
            AttoCapsLabel(label)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AttoCapsLabel(label)
                labelTrailingContent()
            }
        }
        AttoFieldSurface {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AttoFieldValueText(
                    text = displayValue,
                    modifier = Modifier.weight(1f),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.W600,
                            fontSize = 13.sp,
                        ),
                )
                AttoCopyButton(
                    text = value,
                    tint = dark_text_tertiary,
                    contentDescription = "Copy $label",
                )
            }
        }
    }
}

@Composable
fun AttoDetailField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AttoCapsLabel(label)
        AttoFieldSurface {
            Text(
                text = value,
                color = dark_text_primary,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.W600,
                        fontSize = 13.sp,
                    ),
            )
        }
    }
}

@Composable
fun AttoCapsLabel(text: String) {
    Text(
        text = text,
        color = dark_text_tertiary,
        style =
            MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.W500,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
            ),
    )
}

@Composable
fun AttoAccentInlineLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.widthIn(max = 220.dp),
        color = dark_accent,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style =
            MaterialTheme.typography.bodySmall.copy(
                fontFamily = attoFontFamily(),
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
            ),
    )
}

@Composable
internal fun AttoFieldValueText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = dark_text_primary,
    middleEllipsize: Boolean = true,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        maxLines = 1,
        softWrap = true,
        overflow = if (middleEllipsize) TextOverflow.MiddleEllipsis else TextOverflow.Ellipsis,
        style = style,
    )
}

@Composable
internal fun AttoFieldSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(dark_field_deep)
                .padding(12.dp),
        content = content,
    )
}
