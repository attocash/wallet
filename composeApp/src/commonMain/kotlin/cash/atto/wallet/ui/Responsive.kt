package cash.atto.wallet.ui

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Dp.isCompactWidth(): Boolean = this < 600.dp

@Composable
fun isCompactWidth(): Boolean =
    with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp().isCompactWidth()
    }

fun BoxWithConstraintsScope.isCompactWidth(
): Boolean = maxWidth.isCompactWidth()
