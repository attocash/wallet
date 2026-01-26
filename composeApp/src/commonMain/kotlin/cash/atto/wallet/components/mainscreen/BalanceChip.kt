package cash.atto.wallet.components.mainscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.uistate.desktop.BalanceChipUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BalanceChip(
    modifier: Modifier = Modifier,
    uiState: BalanceChipUiState
) {
    Box(
        modifier.clip(MaterialTheme.shapes.medium)
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = AttoFormatter.format(uiState.attoCoins),
                fontSize = 28.sp,
                fontWeight = FontWeight.W300,
                fontFamily = attoFontFamily()
            )
            val usdText = uiState.usdValue?.let { "≈ $${it.toPlainString()} USD" } ?: ""
            val apyText = uiState.apy?.let { " · ${it.toPlainString()}% APY" } ?: ""
            val apyColor = if (uiState.apy != null && uiState.apy > com.ionspin.kotlin.bignum.decimal.BigDecimal.ZERO) {
                androidx.compose.ui.graphics.Color(0xFF4CAF50)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            }
            if (usdText.isNotEmpty() || apyText.isNotEmpty()) {
                Text(
                    text = androidx.compose.ui.text.buildAnnotatedString {
                        append(usdText)
                        pushStyle(androidx.compose.ui.text.SpanStyle(color = apyColor))
                        append(apyText)
                        pop()
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W300,
                    fontFamily = attoFontFamily(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview
@Composable
fun BalanceChipPreview() {
    AttoWalletTheme {
        BalanceChip(uiState = BalanceChipUiState.DEFAULT)
    }
}