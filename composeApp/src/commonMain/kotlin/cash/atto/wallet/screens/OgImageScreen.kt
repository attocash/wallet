package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.commons.AttoHeight
import cash.atto.wallet.MainScreenNavDestination
import cash.atto.wallet.components.common.AttoTag
import cash.atto.wallet.components.common.AttoWallet
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_bg
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.ui.dark_text_tertiary
import cash.atto.wallet.ui.dark_violet
import cash.atto.wallet.uistate.desktop.BalanceChipUiState
import cash.atto.wallet.uistate.overview.OverviewAccountUiState
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.uistate.overview.TransactionListUiState
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun OgImageScreen() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(dark_bg)
                .padding(horizontal = 56.dp, vertical = 52.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OgImageCopy()
            OgImageOverviewPreview()
        }
    }
}

@Composable
private fun OgImageCopy() {
    Column(
        modifier = Modifier.width(392.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.padding(top = 28.dp),
            text = "Your wallet for instant electronic cash.",
            color = dark_text_primary,
            style =
                MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.W700,
                    fontSize = 52.sp,
                    lineHeight = 58.sp,
                    letterSpacing = 0.sp,
                ),
        )

        Text(
            modifier = Modifier.padding(top = 18.dp),
            text = "Send, receive, stake, and manage Atto from a focused self-custody wallet built for the web.",
            color = dark_text_secondary,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.W400,
                    fontSize = 21.sp,
                    lineHeight = 30.sp,
                    letterSpacing = 0.sp,
                ),
        )

        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AttoTag(text = "SELF-CUSTODY", color = dark_accent)
            AttoTag(text = "FAST FINALITY", color = dark_success)
            AttoTag(text = "WEB WALLET", color = dark_violet)
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "wallet.atto.cash",
            color = dark_text_tertiary,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    letterSpacing = 0.sp,
                ),
        )
    }
}

@Composable
private fun OgImageOverviewPreview() {
    Box(
        modifier =
            Modifier
                .width(660.dp)
                .height(486.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(dark_surface)
                .border(1.dp, dark_border, RoundedCornerShape(16.dp)),
    ) {
        OgImageOverview()
    }
}

@Composable
private fun OgImageOverview() {
    val overviewState = remember { ogImageOverviewState() }

    AttoWallet(
        navState = MainScreenNavDestination.OVERVIEW,
        onNavStateChanged = {},
        balanceUiState =
            BalanceChipUiState(
                attoCoins = BigDecimal.parseString("124.50"),
                usdValue = BigDecimal.parseString("24.37"),
                priceUsd = BigDecimal.parseString("0.196"),
                apy = BigDecimal.parseString("8.4"),
                pendingReceivableCount = 2,
                pendingReceivableAmount = BigDecimal.parseString("32.00"),
            ),
        isWalletInitialized = true,
        hasCachedWork = true,
        onLock = {},
    ) {
        OverviewContent(
            uiState = overviewState,
            isWalletInitialized = true,
            onSendClick = {},
            onReceiveClick = {},
            onTransactionsClick = {},
            onStakingClick = {},
            onSelectAccount = { _ -> },
            onAddAccount = { _ -> },
            onToggleAccount = { _, _ -> },
            onNameAccount = { _, _ -> },
        )
    }
}

@OptIn(ExperimentalTime::class)
private fun ogImageOverviewState() =
    OverviewUiState(
        balance = BigDecimal.parseString("248.50"),
        priceUsd = BigDecimal.parseString("0.42"),
        apy = BigDecimal.parseString("8.4"),
        receiveAddress = "atto://1walletpreview7xqdk68p4x4zg3f49shbn7hfz6kng3zq6dzjftcmrs",
        accounts =
            listOf(
                OverviewAccountUiState(
                    index = 0U,
                    name = "Main Account",
                    address = "atto://1walletpreview7xqdk68p4x4zg3f49shbn7hfz6kng3zq6dzjftcmrs",
                    balance = BigDecimal.parseString("248.50"),
                    active = true,
                ),
            ),
        selectedAccountIndex = 0U,
        pendingReceivableCount = 2,
        pendingReceivableAmount = BigDecimal.parseString("32.00"),
        voterName = "Atto Live Representative",
        transactionListUiState =
            TransactionListUiState(
                transactions =
                    listOf(
                        TransactionUiState(
                            type = TransactionType.RECEIVE,
                            amount = "+ 84.20",
                            source = "atto://a1clientdepositupqur4sm8npn5w4kg9mrg1xwc5m5",
                            sourceLabel = "Client",
                            transactionLabel = "Wallet Redesign",
                            timestamp = ogImageTimestamp(1.minutes),
                            height = AttoHeight(8UL),
                            hash = "784e9f0d9f8e8a2c6a6f6e1a5b6c9d8e",
                        ),
                        TransactionUiState(
                            type = TransactionType.SEND,
                            amount = "- 12.00",
                            source = "atto://a1supplierpayoutf4s9d8a6p5n4m3k2j1h0g9f8",
                            sourceLabel = "Supplier",
                            transactionLabel = "Pencils",
                            timestamp = ogImageTimestamp(1.days),
                            height = AttoHeight(7UL),
                            hash = "113b31e377a2ff4833e9c13fb1ab4581",
                        ),
                        TransactionUiState(
                            type = TransactionType.RECEIVE,
                            amount = "+ 250.00",
                            source = "atto://a1treasuryreleasepu4c4cbyn13db8zw963r7xue",
                            sourceLabel = "Treasury",
                            transactionLabel = null,
                            timestamp = ogImageTimestamp(2.days),
                            height = AttoHeight(6UL),
                            hash = "6f04b20d58a5e23799c6c7a2a2c8c9b1",
                        ),
                        TransactionUiState(
                            type = TransactionType.CHANGE,
                            amount = null,
                            source = "atto://a1representative9g6w5z4y3x2v1u0t9s8r7q6p5",
                            sourceLabel = "Voter",
                            transactionLabel = null,
                            timestamp = ogImageTimestamp(14.days),
                            height = AttoHeight(5UL),
                            hash = "8d39045bade6d4ba229f27d65ab5c914",
                        ),
                    ),
                showHint = false,
            ),
    )

@OptIn(ExperimentalTime::class)
private fun ogImageTimestamp(baseOffset: Duration): Instant =
    Clock.System
        .now()
        .minus(baseOffset)
        .minus(Random.nextLong(12.hours.inWholeMinutes + 1).minutes)
