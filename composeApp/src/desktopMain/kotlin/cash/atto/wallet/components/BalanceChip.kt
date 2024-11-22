package cash.atto.wallet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
    Box(modifier.clip(MaterialTheme.shapes.medium)
        .background(color = MaterialTheme.colors.surface)
        .padding(32.dp)
    ) {
        Text(
            text = AttoFormatter.format(uiState.attoCoins),
            modifier = Modifier.align(Alignment.CenterStart),
            fontSize = 28.sp,
            fontWeight = FontWeight.W300,
            fontFamily = attoFontFamily()
        )
    }
}

@Preview
@Composable
fun BalanceChipPreview() {
    AttoWalletTheme {
        BalanceChip(uiState = BalanceChipUiState.DEFAULT)
    }
}