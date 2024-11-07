package cash.atto.wallet.components.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.overview_receive_copy
import attowallet.composeapp.generated.resources.overview_receive_share
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.common.BottomSheet
import cash.atto.wallet.components.common.BottomSheetStud
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import qrgenerator.QRCodeImage

@Composable
fun ReceiveAttoBottomSheet(
    address: String?,
    onCopy: () -> Unit,
    onShare: () -> Unit
) = address?.let {
    BottomSheet {
        ReceiveAttoContent(
            address = address,
            onCopy = onCopy
        )

        AttoOutlinedButton(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.overview_receive_share))
        }
    }
}

@Composable
fun ReceiveAttoContent(
    address: String,
    onCopy: () -> Unit,
    qrCodeSize: Dp = 128.dp
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(address)

        QRCodeImage(
            url = address,
            contentDescription = "QR",
            modifier = Modifier.height(qrCodeSize)
        )

        Button(
            onClick = onCopy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.overview_receive_copy))
        }
    }
}

@Preview
@Composable
fun ReceiveAttoContentPreview() {
    AttoWalletTheme {
        ReceiveAttoContent(
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
            onShare = {}
        )
    }
}