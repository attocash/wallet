package cash.atto.wallet.components.common

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.*
import cash.atto.wallet.MainScreenNavDestination
import cash.atto.wallet.ui.*
import cash.atto.wallet.uistate.desktop.BalanceChipUiState
import cash.atto.wallet.uistate.overview.TransactionUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

val LogoutShellIcon: ImageVector
    get() = Icons.AutoMirrored.Outlined.Logout

@Composable
fun AttoWallet(
    navState: MainScreenNavDestination,
    onNavStateChanged: (MainScreenNavDestination) -> Unit,
    balanceUiState: BalanceChipUiState,
    isWalletInitialized: Boolean,
    hasCachedWork: Boolean,
    onLock: () -> Unit,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(dark_bg),
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.safeDrawing.asPaddingValues()),
        ) {
            val shellMaxWidth = maxWidth
            val compact = shellMaxWidth.isCompactWidth()
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                AttoTopBar(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    compact = compact,
                    navState = navState,
                    onNavStateChanged = onNavStateChanged,
                    isWalletInitialized = isWalletInitialized,
                    hasCachedWork = hasCachedWork,
                    onLock = onLock,
                )
                HorizontalDivider(color = dark_border)

                BoxWithConstraints(
                    modifier =
                        Modifier
                            .fillMaxSize(),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .widthIn(max = 1400.dp)
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .align(Alignment.TopCenter)
                                .padding(all = 20.dp),
                    ) {
                        this@BoxWithConstraints.content()
                    }
                }
            }
        }
    }
}

@Composable
private fun AttoTopBar(
    modifier: Modifier = Modifier,
    compact: Boolean,
    navState: MainScreenNavDestination,
    onNavStateChanged: (MainScreenNavDestination) -> Unit,
    isWalletInitialized: Boolean,
    hasCachedWork: Boolean,
    onLock: () -> Unit,
) {
    Row(
        modifier =
            modifier
                .widthIn(max = 1400.dp)
                .fillMaxWidth()
                .padding(horizontal = if (compact) 20.dp else 32.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AttoShellBrandMark()
            Text(
                text = "Atto Wallet",
                color = dark_text_primary,
                maxLines = 1,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 20.sp,
                    ),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isWalletInitialized) {
                AttoShellStatusIndicator(
                    compact = compact,
                    hasCachedWork = hasCachedWork,
                )
            }
            AttoCircleIconButton(
                icon = Icons.Outlined.Settings,
                contentDescription = "Open settings",
                tint = if (navState == MainScreenNavDestination.SETTINGS) dark_accent else dark_text_secondary,
                background = Color.Transparent,
                onClick = { onNavStateChanged(MainScreenNavDestination.SETTINGS) },
            )
            AttoCircleIconButton(
                icon = Icons.Outlined.Lock,
                contentDescription = "Lock wallet",
                tint = dark_text_secondary,
                background = Color.Transparent,
                onClick = onLock,
            )
        }
    }
}

@Composable
private fun AttoShellBrandMark() {
    Image(
        painter = painterResource(Res.drawable.logo),
        contentDescription = "Atto Wallet",
        modifier =
            Modifier
                .size(32.dp),
    )
}

@Composable
private fun AttoShellStatusIndicator(
    compact: Boolean,
    hasCachedWork: Boolean,
) {
    val statusColor = if (hasCachedWork) dark_success else dark_accent
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(if (compact) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(statusColor),
        )
        if (!compact) {
            Text(
                text = if (hasCachedWork) "Ready" else "Working",
                color = statusColor,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 13.sp,
                    ),
            )
        }
    }
}

@Composable
fun AttoShellNavRow(
    navState: MainScreenNavDestination,
    onNavStateChanged: (MainScreenNavDestination) -> Unit,
) {
    val scrollState = rememberScrollState()
    val items =
        listOf(
            MainScreenNavDestination.OVERVIEW to
                Pair(
                    Icons.Outlined.AccountBalanceWallet,
                    stringResource(Res.string.main_nav_overview),
                ),
            MainScreenNavDestination.SEND to Pair(Icons.Outlined.ArrowUpward, stringResource(Res.string.main_nav_send)),
            MainScreenNavDestination.RECEIVE to
                Pair(
                    Icons.Outlined.ArrowDownward,
                    stringResource(Res.string.main_nav_receive),
                ),
            MainScreenNavDestination.TRANSACTIONS to
                Pair(
                    Icons.Outlined.History,
                    stringResource(Res.string.main_nav_transactions),
                ),
            MainScreenNavDestination.SETTINGS to
                Pair(
                    Icons.Outlined.Settings,
                    stringResource(Res.string.main_nav_settings),
                ),
        )

    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(dark_surface)
                .border(1.dp, dark_border, RoundedCornerShape(8.dp))
                .horizontalScroll(scrollState)
                .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { (destination, presentation) ->
            val selected = destination == navState
            val background = if (selected) dark_accent else Color.Transparent
            val contentColor = if (selected) Color(0xFF111827) else dark_text_primary

            Row(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(background)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onNavStateChanged(destination) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = presentation.first,
                    contentDescription = presentation.second,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = presentation.second,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600),
                )
            }
        }
    }
}

@Composable
fun AttoPageFrame(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    scrollable: Boolean = true,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        onBack?.let {
            AttoBackButton(onClick = it)
        }

        val contentModifier =
            Modifier
                .weight(1f)
                .fillMaxWidth()

        val layoutModifier =
            if (scrollable) {
                contentModifier.verticalScroll(rememberScrollState())
            } else {
                contentModifier
            }

        Column(
            modifier = layoutModifier,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val stackActions = isCompactWidth()

                if (stackActions) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = title,
                                color = dark_text_primary,
                                style =
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.W600,
                                        fontSize = 32.sp,
                                    ),
                            )
                            Text(
                                text = subtitle,
                                color = dark_text_secondary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        actions?.let { actionContent ->
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                content = actionContent,
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = title,
                                color = dark_text_primary,
                                style =
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.W600,
                                        fontSize = 32.sp,
                                    ),
                            )
                            Text(
                                text = subtitle,
                                color = dark_text_secondary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        actions?.let { actionContent ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                content = actionContent,
                            )
                        }
                    }
                }
            }

            content()
        }
    }
}

@Composable
fun AttoPanelCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(dark_surface)
                .border(1.dp, dark_border, RoundedCornerShape(12.dp))
                .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
fun AttoMetricPill(
    label: String,
    value: String,
) {
    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(dark_surface)
                .border(1.dp, dark_border, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            color = dark_text_secondary,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = value,
            color = dark_text_primary,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600),
        )
    }
}

@Composable
fun AttoCircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = dark_text_primary,
    background: Color = Color(0x40192639),
) {
    Box(
        modifier =
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(background)
                .border(
                    width = if (background == Color.Transparent) 0.dp else 1.dp,
                    color = if (background == Color.Transparent) Color.Transparent else Color(0x1FFFFFFF),
                    shape = CircleShape,
                ).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun AttoTransactionList(
    transactions: List<TransactionUiState?>,
    modifier: Modifier = Modifier,
    emptyMessage: String,
) {
    val items = transactions.filterNotNull()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (items.isEmpty()) {
            AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = emptyMessage,
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { transaction ->
                    AttoTransactionCard(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun AttoTransactionSection(
    title: String,
    transactions: List<TransactionUiState?>,
    modifier: Modifier = Modifier,
    emptyMessage: String,
    onTransactionClick: ((TransactionUiState) -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            color = dark_text_primary,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W600),
        )
        AttoTransactionList(
            transactions = transactions,
            modifier = Modifier.fillMaxWidth(),
            emptyMessage = emptyMessage,
            onTransactionClick = onTransactionClick,
        )
    }
}

@Composable
fun AttoTransactionList(
    transactions: List<TransactionUiState?>,
    modifier: Modifier = Modifier,
    emptyMessage: String,
    onTransactionClick: ((TransactionUiState) -> Unit)?,
) {
    val items = transactions.filterNotNull()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (items.isEmpty()) {
            AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = emptyMessage,
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { transaction ->
                    AttoTransactionCard(
                        transaction = transaction,
                        onClick = onTransactionClick?.let { { it(transaction) } },
                    )
                }
            }
        }
    }
}

@Composable
fun AttoSettingsInfoCard(
    title: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    AttoPanelCard(modifier = modifier) {
        Text(
            text = title,
            color = dark_text_primary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600),
        )
        HorizontalDivider(color = dark_border)
        lines.forEach { line ->
            Text(
                text = line,
                color = dark_text_secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun AttoBackButton(onClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(dark_surface)
                .border(1.dp, dark_border, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onClick() }
                .padding(10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            tint = dark_text_primary,
            modifier = Modifier.size(20.dp),
        )
    }
}
