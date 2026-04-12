package cash.atto.wallet.ui

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


fun Dp.isCompactWidth(): Boolean = this < 900.dp

fun BoxWithConstraintsScope.isCompactWidth(
): Boolean = maxWidth.isCompactWidth()
