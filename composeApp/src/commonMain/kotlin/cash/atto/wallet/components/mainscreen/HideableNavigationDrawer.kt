package cash.atto.wallet.components.mainscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_atto_chevron_left
import attowallet.composeapp.generated.resources.ic_atto_chevron_right
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.primaryGradient
import cash.atto.wallet.ui.secondaryGradient
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HideableNavigationDrawer(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    drawerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        val drawerExpanded = remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(Modifier.size(48.dp, 48.dp)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme
                            .onSurface
                            .copy(alpha = 0.05f)
                    )
                    .clickable { drawerExpanded.value = true }
                ) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        imageVector = vectorResource(Res.drawable.ic_atto_chevron_right),
                        contentDescription = "backIcon"
                    )
                }

                header()
            }

            Box(Modifier.fillMaxSize()) {
                content()
            }
        }

        if (drawerExpanded.value) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)
                ))
            }
        }

        AnimatedVisibility(
            visible = drawerExpanded.value,
            enter = slideIn(
                animationSpec = tween(700),
                initialOffset = { size -> IntOffset(-size.width, 0) }
            ),
            exit = slideOut(
                animationSpec = tween(700),
                targetOffset = { size -> IntOffset(-size.width, 0) }
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.75f)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .background(
                        color = MaterialTheme.colorScheme.tertiary

                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.size(48.dp, 48.dp)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme
                            .onSurface
                            .copy(alpha = 0.05f)
                    )
                    .clickable { drawerExpanded.value = false }
                ) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        imageVector = vectorResource(Res.drawable.ic_atto_chevron_left),
                        contentDescription = "backIcon"
                    )
                }

                drawerContent()
            }
        }
    }
}

@Preview
@Composable
fun HideableNavigationDrawerPreview() {
    AttoWalletTheme {
        HideableNavigationDrawer(
            header = {
                Text("header")
            },
            drawerContent = {
                NavigationDrawerItemPreview()
            }
        ) {
            Text("Hello")
        }
    }
}