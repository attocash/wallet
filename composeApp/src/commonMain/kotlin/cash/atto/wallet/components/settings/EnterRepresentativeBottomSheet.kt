package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.representative_change
import attowallet.composeapp.generated.resources.representative_change_title
import attowallet.composeapp.generated.resources.representative_close
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.common.BottomSheet
import cash.atto.wallet.ui.AttoWalletTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun EnterRepresentativeBottomSheet(
    onChange: (String) -> Unit,
    onClose: () -> Unit
) {
    val input = remember {
        mutableStateOf("")
    }

    BottomSheet {
        Text(
            text = stringResource(Res.string.representative_change_title),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.h5
        )

        TextField(
            value = input.value,
            onValueChange = { input.value = it }
        )

        Spacer(Modifier.height(64.dp))

        Button(
            onClick = { onChange.invoke(input.value) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.representative_change))
        }

        AttoOutlinedButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.representative_close))
        }
    }
}

@Preview
@Composable
fun EnterRepresentativeBottomSheetPreview() {
    AttoWalletTheme {
        EnterRepresentativeBottomSheet(
            onChange = {},
            onClose = {}
        )
    }
}