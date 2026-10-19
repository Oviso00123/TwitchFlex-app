package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GachaItem
import com.example.data.LeaderboardCreator
import com.example.data.MonetizationCalculator
import com.example.data.PlatformId
import com.example.data.TransactionType
import com.example.ui.theme.TwitchBlack
import com.example.ui.theme.TwitchBorder
import com.example.ui.theme.TwitchGold
import com.example.ui.theme.TwitchLiveRed
import com.example.ui.theme.TwitchSurface
import com.example.ui.theme.TwitchSurfaceElevated
import com.example.ui.theme.TwitchText
import com.example.ui.theme.TwitchTextMuted
import com.example.viewmodel.StreamViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreatorDashboardScreen(
    viewModel: StreamViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val monetizationUnlocked by viewModel.monetizationUnlocked.collectAsState()
    val totalSubs by viewModel.totalSubsAllTime.collectAsState()
    val totalEarnings by viewModel.totalEarningsAllTime.collectAsState()
    val availableBalance by viewModel.availableWalletBalance.collectAsState()
    val withdrawnTotal by viewModel.withdrawnTotal.collectAsState()
    val walletTransactions by viewModel.walletTransactions.collectAsState()
    val liveState by viewModel.liveState.collectAsState()
    val hypeStock by viewModel.hypeStock.collectAsState()
    val savedClips by viewModel.savedClips.collectAsState()
    val unlockedGachaItemIds by viewModel.unlockedGachaItemIds.collectAsState()
    val currentPlatform by viewModel.currentPlatform.collectAsState()
    val brandReputation by viewModel.brandReputation.collectAsState()

    var showWithdrawDialog by remember { mutableStateOf(false) }
    var withdrawAmountText by remember { mutableStateOf("") }
    var withdrawDestinationText by remember { mutableStateOf("PayPal (mouhamed.beye000@gmail.com)") }
    var selectedTransactionFilter by remember { mutableStateOf<TransactionType?>(null) }
    var gachaRewardAlert by remember { mutableStateOf<GachaItem?>(null) }
    var isMoneyBlurred by remember { mutableStateOf(false) }

    fun formatMoney(amount: Double): String {
        return if (isMoneyBlurred) "•••• €" else "%.2f €".format(amount)
    }

    val totalCurrentSubs = totalSubs + liveState.sessionSubsCount

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TwitchBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Title with Money Blur Privacy Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(TwitchGold.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = TwitchGold, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Economia & Payout TwitchFlex",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Guadagno Totale del Canale, Tasse Zyro e Prelievi",
                        fontSize = 11.sp,
                        color = TwitchTextMuted
                    )
                }
            }

            // Privacy Toggle Button (Money Blur)
            Button(
                onClick = { isMoneyBlurred = !isMoneyBlurred },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMoneyBlurred) TwitchGold.copy(alpha = 0.25f) else TwitchSurfaceElevated
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isMoneyBlurred) TwitchGold else TwitchBorder),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("money_privacy_toggle")
            ) {
                Icon(
                    imageVector = if (isMoneyBlurred) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isMoneyBlurred) "Mostra Cifre" else "Nascondi Cifre",
                    tint = if (isMoneyBlurred) TwitchGold else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isMoneyBlurred) "Money Blur: ON" else "Money Blur: OFF",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMoneyBlurred) TwitchGold else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. WALLET BALANCE & EXTERNAL TRANSFER CARD
        Card(
            modifier = Modifier.fillMaxWidth().testTag("wallet_balance_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchGold.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = TwitchGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Saldo Canale Disponibile", color = TwitchTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "🏛️ Tasse Zyro: 5,50€ / live",
                        color = TwitchLiveRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formatMoney(availableBalance),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.clickable {
                        if (isMoneyBlurred) isMoneyBlurred = false
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clickable {
                            if (isMoneyBlurred) isMoneyBlurred = false
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Guadagno Storico Netto: ${formatMoney(totalEarnings)}", color = TwitchGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Prelevato: ${formatMoney(withdrawnTotal)}", color = TwitchTextMuted, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showWithdrawDialog = true },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("withdraw_funds_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TwitchGold)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trasferimento Manuale Esterno (Prelievo)", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. CONTRATTI DI ESCLUSIVA & PIATTAFORME RIVALI
        Card(
            modifier = Modifier.fillMaxWidth().testTag("rival_platform_contracts_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(android.graphics.Color.parseColor(currentPlatform.themeAccentHex)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(currentPlatform.logoEmoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Piattaforma Attiva: ${currentPlatform.platformName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Split Sub: ${currentPlatform.subSplitPercent}% • Bonus live: ${"%.2f".format(currentPlatform.liveHourlyBonus)}€/h", color = TwitchTextMuted, fontSize = 11.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(android.graphics.Color.parseColor(currentPlatform.themeAccentHex)).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(android.graphics.Color.parseColor(currentPlatform.themeAccentHex)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("CONTRATTO ATTIVO", color = Color(android.graphics.Color.parseColor(currentPlatform.themeAccentHex)), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Offerte di Contratto Piattaforme Rivali:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                PlatformId.values().forEach { platform ->
                    val isCurrent = platform == currentPlatform
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = TwitchSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) Color(android.graphics.Color.parseColor(platform.themeAccentHex)) else TwitchBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(platform.logoEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(platform.platformName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("Split ${platform.subSplitPercent}% Creator", color = Color(android.graphics.Color.parseColor(platform.themeAccentHex)), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                            Text(platform.description, color = TwitchTextMuted, fontSize = 10.sp, modifier = Modifier.padding(vertical = 2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Regole: ${platform.moderationRules}", color = TwitchTextMuted, fontSize = 10.sp)
                                    if (platform.cancellationPenalty > 0) {
                                        Text("Penale rescissione: ${platform.cancellationPenalty} €", color = TwitchLiveRed, fontSize = 10.sp)
                                    }
                                }
                                Button(
                                    onClick = {
                                        viewModel.signPlatformContract(platform) { ok, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    enabled = !isCurrent,
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(android.graphics.Color.parseColor(platform.themeAccentHex))),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(if (isCurrent) "Firmato ✓" else "Firma Contratto ✍️", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. TRANSACTION HISTORY WITH ADVANCED FILTERS
        Card(
            modifier = Modifier.fillMaxWidth().testTag("transactions_ledger_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registro Movimenti & Tasse", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text("${walletTransactions.size} Transazioni", color = TwitchTextMuted, fontSize = 11.sp)
                }

                // Filter chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            label = "Tutte",
                            selected = selectedTransactionFilter == null,
                            onClick = { selectedTransactionFilter = null }
                        )
                    }
                    items(TransactionType.values()) { type ->
                        FilterChip(
                            label = "${type.iconEmoji} ${type.label}",
                            selected = selectedTransactionFilter == type,
                            onClick = { selectedTransactionFilter = type }
                        )
                    }
                }

                val filteredList = if (selectedTransactionFilter == null) {
                    walletTransactions
                } else {
                    walletTransactions.filter { it.type == selectedTransactionFilter }
                }

                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nessun movimento registrato per questo filtro.", color = TwitchTextMuted, fontSize = 11.sp)
                    }
                } else {
                    filteredList.take(15).forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(tx.type.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(tx.description, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(tx.timestamp))
                                    Text(dateStr, color = TwitchTextMuted, fontSize = 10.sp)
                                }
                            }
                            Text(
                                text = if (isMoneyBlurred) "•••• €" else if (tx.amount >= 0) "+${"%.2f".format(tx.amount)} €" else "${"%.2f".format(tx.amount)} €",
                                color = if (tx.amount >= 0) Color(0xFF00F5D4) else TwitchLiveRed,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable {
                                    if (isMoneyBlurred) isMoneyBlurred = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. HYPE STOCK MARKET
        Card(
            modifier = Modifier.fillMaxWidth().testTag("hype_stock_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Borsa del Canale (Hype Stock)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                    Text("+${hypeStock.changePercent}% Oggi", color = Color(0xFF00F5D4), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Valore Indice: ${"%.2f".format(hypeStock.currentPrice)} FLX",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("• Reputazione: ${hypeStock.reputationScore}/100", color = TwitchGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .background(TwitchSurfaceElevated, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    hypeStock.points.forEach { point ->
                        val barHeight = ((point - 90.0) / 40.0).coerceIn(0.2, 1.0)
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((barHeight * 28).dp)
                                .background(Color(0xFF00F5D4), RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. UNBOXING GACHA
        Card(
            modifier = Modifier.fillMaxWidth().testTag("gacha_crate_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unboxing Gacha Creator Crate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(
                    text = "Apri casse virtuali per sbloccare microfoni, webcam e periferiche che aumentano la retention della live!",
                    color = TwitchTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )

                Button(
                    onClick = {
                        val rewarded = viewModel.openGachaCrate()
                        gachaRewardAlert = rewarded
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("open_gacha_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("🎁 Apri Cassa Virtuale Gratis", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (unlockedGachaItemIds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Dotazione Sbloccata:", color = TwitchTextMuted, fontSize = 10.sp)
                    LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        items(viewModel.allGachaItems.filter { unlockedGachaItemIds.contains(it.id) }) { item ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .background(TwitchSurfaceElevated, RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(android.graphics.Color.parseColor(item.colorHex)), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.emoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(item.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. HOURLY EARNINGS & RICHIESTA AUMENTO
        Card(
            modifier = Modifier.fillMaxWidth().testTag("raise_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Tariffa Oraria & Contratto Zyro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "Tariffa Sub: 3,20 € netti per sub. Tasse fisse: 5,50 € a diretta (cut piattaforma: 10€ ogni 10 sub mostrato solo post-stream).",
                    color = TwitchTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Button(
                    onClick = { viewModel.launchRaiseEmailIntent(context) },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("request_raise_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Richiesta Aumento Tariffa Oraria", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    // WITHDRAW DIALOG
    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Trasferimento a Wallet Esterno", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Saldo disponibile: ${formatMoney(availableBalance)}", color = TwitchGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = withdrawAmountText,
                        onValueChange = { withdrawAmountText = it },
                        label = { Text("Importo da trasferire (€)") },
                        placeholder = { Text("es. 25.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = withdrawDestinationText,
                        onValueChange = { withdrawDestinationText = it },
                        label = { Text("Destinazione (PayPal / IBAN / Revolut)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = withdrawAmountText.toDoubleOrNull()
                        if (amount != null && amount > 0.0) {
                            if (amount <= availableBalance) {
                                viewModel.withdrawFunds(amount, withdrawDestinationText.trim()) { ok ->
                                    if (ok) {
                                        Toast.makeText(context, "Trasferimento di ${"%.2f".format(amount)} € completato con successo!", Toast.LENGTH_LONG).show()
                                        showWithdrawDialog = false
                                        withdrawAmountText = ""
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Importo superiore al saldo disponibile!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Inserisci un importo valido!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TwitchGold)
                ) {
                    Text("Conferma Prelievo", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) { Text("Annulla", color = Color.White) }
            },
            containerColor = TwitchSurface
        )
    }

    // GACHA ALERT
    gachaRewardAlert?.let { item ->
        AlertDialog(
            onDismissRequest = { gachaRewardAlert = null },
            title = { Text("🎉 Oggetto Trovato!", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(item.emoji, fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(item.name, color = Color(android.graphics.Color.parseColor(item.colorHex)), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text("Rarità: ${item.rarity}", color = TwitchGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(item.boostDescription, color = TwitchText, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            },
            confirmButton = {
                Button(onClick = { gachaRewardAlert = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Equipaggia", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = TwitchSurface
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(if (selected) MaterialTheme.colorScheme.primary else TwitchSurfaceElevated, RoundedCornerShape(16.dp))
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else TwitchBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TwitchTextMuted,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
