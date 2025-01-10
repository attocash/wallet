package cash.atto.wallet.components.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_copy
import attowallet.composeapp.generated.resources.ic_share
import attowallet.composeapp.generated.resources.overview_receive_address
import attowallet.composeapp.generated.resources.overview_receive_close
import attowallet.composeapp.generated.resources.overview_receive_copy
import attowallet.composeapp.generated.resources.overview_receive_share
import cash.atto.wallet.components.common.BottomSheet
import cash.atto.wallet.components.common.QRCodeImage
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ReceiveAttoBottomSheet(
    address: String?,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onClose: () -> Unit,
) = address?.let {
    BottomSheet {
        ReceiveAttoContent(
            address = address,
            onCopy = onCopy
        )

        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(19.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_share),
                    contentDescription = "Copy icon"
                )

                Text(text = stringResource(Res.string.overview_receive_share))
            }
        }

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(19.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(Res.string.overview_receive_close))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ReceiveAttoContent(
    address: String,
    onCopy: () -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact)
        ReceiveAttoContentCompact(address, onCopy)
    else ReceiveAttoContentExtended(address, onCopy)
}

@Composable
fun ReceiveAttoContentCompact(
    address: String,
    onCopy: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val displayAddress = address.substring(0, address.length / 2) +
                "\n" +
                address.substring(address.length / 2, address.length)

        Text(
            text = stringResource(Res.string.overview_receive_address),
            color = MaterialTheme.colorScheme
                .onSurface
                .copy(alpha = 0.55f),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = displayAddress,
            style = MaterialTheme.typography.titleLarge
        )

        Box(Modifier.height(200.dp)) {
            Text(
                text = displayAddress,
                modifier = Modifier
                    .wrapContentSize(
                        align = Alignment.Center,
                        unbounded = true
                    )
                    .width(800.dp)
                    .padding(top = 40.dp),
                color = MaterialTheme.colorScheme
                    .onSurface
                    .copy(alpha = 0.15f),
                textAlign = TextAlign.Center,
                softWrap = false,
                style = MaterialTheme.typography.displayMedium
            )

            QRCodeImage(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                url = address,
                contentDescription = "QR"
            )
        }


        Button(
            onClick = onCopy,
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(19.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_copy),
                    contentDescription = "Copy icon"
                )

                Text(text = stringResource(Res.string.overview_receive_copy))
            }
        }
    }
}

@Composable
fun ReceiveAttoContentExtended(
    address: String,
    onCopy: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val displayAddress = address.substring(0, address.length / 2) +
                "\n" +
                address.substring(address.length / 2, address.length)

        Text(
            text = stringResource(Res.string.overview_receive_address),
            color = MaterialTheme.colorScheme
                .onSurface
                .copy(alpha = 0.55f),
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = displayAddress,
            style = MaterialTheme.typography.headlineMedium
        )

        QRCodeImage(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            url = address,
            contentDescription = "QR"
        )

        Button(
            onClick = onCopy,
            modifier = Modifier.fillMaxWidth(0.7f),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(19.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_copy),
                    contentDescription = "Copy icon"
                )

                Text(text = stringResource(Res.string.overview_receive_copy))
            }
        }
    }
}

@Preview
@Composable
fun ReceiveAttoContentCompactPreview() {
    AttoWalletTheme {
        ReceiveAttoContentCompact(
            address = "address",
            onCopy = {}
        )
    }
}

@Preview
@Composable
fun ReceiveAttoContentExtendedPreview() {
    AttoWalletTheme {
        ReceiveAttoContentExtended(
            address = "address",
            onCopy = {}
        )
    }
}

@Preview
@Composable
fun ReceiveAttoBottomSheetPreview() {
    AttoWalletTheme {
        ReceiveAttoBottomSheet(
            address = "address",
            onCopy = {},
            onShare = {},
            onClose = {},
        )
    }
}