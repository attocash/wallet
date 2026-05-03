package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_placeholder
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_text_primary
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AttoWordChip(
    ordinal: Int,
    word: String,
    hidden: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color = dark_surface)
                .border(1.dp, dark_border, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = ordinal.toString().padStart(2, '0'),
            modifier =
                Modifier
                    .width(24.dp)
                    .alignByBaseline(),
            color = dark_placeholder,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.W600,
                    fontSize = 12.sp,
                ),
        )

        Text(
            text = if (hidden) "******" else word,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .alignByBaseline(),
            color = dark_text_primary,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 15.sp,
                ),
        )
    }
}

@Preview
@Composable
fun AttoWordChipPreview() {
    AttoWalletTheme {
        AttoWordChip(
            ordinal = 1,
            word = "Word",
            hidden = false,
        )
    }
}
