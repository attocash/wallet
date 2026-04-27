package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.dark_text_tertiary

private val FieldBackground = Color(0xFF0F0F11)

@Composable
fun AttoCopyField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
    displayValue: String = value,
    middleEllipsize: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AttoCapsLabel(label)
        AttoFieldSurface {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayValue,
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    maxLines = maxLines,
                    overflow = if (middleEllipsize) TextOverflow.MiddleEllipsis else TextOverflow.Ellipsis,
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
                color = Color.White,
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
internal fun AttoFieldSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(FieldBackground)
                .padding(12.dp),
        content = content,
    )
}
