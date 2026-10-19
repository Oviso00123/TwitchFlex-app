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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SocialTweet
import com.example.data.VirtualLurkerChannel
import com.example.data.VirtualModerator
import com.example.data.VirtualNewsArticle
import com.example.ui.theme.TwitchBlack
import com.example.ui.theme.TwitchBorder
import com.example.ui.theme.TwitchGold
import com.example.ui.theme.TwitchLiveRed
import com.example.ui.theme.TwitchSurface
import com.example.ui.theme.TwitchSurfaceElevated
import com.example.ui.theme.TwitchText
import com.example.ui.theme.TwitchTextMuted
import com.example.viewmodel.StreamViewModel

@Composable
fun CommunityScreen(
    viewModel: StreamViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val socialTweets by viewModel.socialTweets.collectAsState()
    val virtualNews by viewModel.virtualNews.collectAsState()
    val virtualModerators by viewModel.virtualModerators.collectAsState()
    val lurkerChannels by viewModel.lurkerChannels.collectAsState()
    val availableBalance by viewModel.availableWalletBalance.collectAsState()

    var selectedLurkerChannel by remember { mutableStateOf<VirtualLurkerChannel?>(null) }
    var lurkerChatInput by remember { mutableStateOf("") }

    val tabTitles = listOf("📰 Notizie", "🛡️ Mod Virtuali", "👀 Lurker", "🐦 Social", "📡 Rete P2P")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TwitchBlack)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = TwitchSurface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("community_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // 1. PRESSING MEDIATICO & NOTIZIE VIRTUALI
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Newspaper, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rassegna Stampa & Pressing Mediatico", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Text("Articoli di giornale fittizi generati sulle tue performance in live. Influenzano la reputazione e il flusso di donazioni!", color = TwitchTextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    items(virtualNews) { article ->
                        VirtualNewsCard(article = article)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            1 -> {
                // 2. MERCATO DEI MODERATORI VIRTUALI
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mercato dei Moderatori Virtuali", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Text("Ingaggia moderatori professionisti per difendere la chat da hater e bot mentre sei concentrato a giocare!", color = TwitchTextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    items(virtualModerators) { mod ->
                        VirtualModeratorCard(
                            mod = mod,
                            onToggleHire = { viewModel.hireOrFireModerator(mod.id) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            2 -> {
                // 3. MODALITÀ LURKER & INTERAZIONE DA SPETTATORE
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = TwitchGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Modalità Lurker & Auto-Promozione", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Text("Esplora gli altri streamer in live. Dona o regala sub dal tuo saldo per farti notare e attirare spettatori sul tuo canale!", color = TwitchTextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    items(lurkerChannels) { channel ->
                        LurkerChannelCard(
                            channel = channel,
                            onOpen = { selectedLurkerChannel = channel }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            3 -> {
                // 4. ZYROSOCIAL FEED
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tag, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Trending su TwitchFlex", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("#TwitchFlex • #SubathonRecord • #KickFlexWar • #GigaFuel", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    items(socialTweets) { tweet ->
                        TweetCard(tweet = tweet)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            4 -> {
                // 5. P2P LOCAL NETWORK
                LocalNetworkScreen(viewModel = viewModel)
            }
        }
    }

    // LURKER STREAM VIEWER DIALOG
    selectedLurkerChannel?.let { ch ->
        AlertDialog(
            onDismissRequest = { selectedLurkerChannel = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ch.avatarEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(ch.streamerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${ch.category} • 👥 ${ch.viewersCount} spettatori", color = TwitchTextMuted, fontSize = 11.sp)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("\"${ch.title}\"", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                    // Video simulated screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(vertical = 8.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, TwitchBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔴 LIVE IN CORSO", color = TwitchLiveRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(ch.bio, color = TwitchTextMuted, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 12.dp))
                        }
                    }

                    // Recent chat list
                    Text("Chat in diretta:", color = TwitchTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(TwitchSurfaceElevated, RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    ) {
                        LazyColumn {
                            items(ch.recentChatMessages) { m ->
                                Text("${m.username}: ${m.message}", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }

                    // Chat input
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = lurkerChatInput,
                            onValueChange = { lurkerChatInput = it },
                            placeholder = { Text("Scrivi in chat...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        IconButton(
                            onClick = {
                                if (lurkerChatInput.isNotBlank()) {
                                    viewModel.lurkerSendChat(ch.id, lurkerChatInput.trim())
                                    lurkerChatInput = ""
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Fai Auto-Promozione (Usa Saldo Canale):", color = TwitchGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.lurkerSendDonationOrSub(ch.id, 5.00, false) { ok, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TwitchGold),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Dona 5€ TTS 💎", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.lurkerSendDonationOrSub(ch.id, 10.00, true) { ok, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("2 Sub Bombs ⭐", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedLurkerChannel = null }) {
                    Text("Chiudi", color = Color.White)
                }
            },
            containerColor = TwitchSurface
        )
    }
}

@Composable
fun VirtualNewsCard(article: VirtualNewsArticle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text("${article.tagEmoji} ${article.newspaperName}", color = TwitchGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(article.dateAgo, color = TwitchTextMuted, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(article.headline, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Text(article.snippet, color = TwitchTextMuted, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Impatto Reputazione: ${if (article.reputationImpact >= 0) "+${article.reputationImpact}" else "${article.reputationImpact}"} pt", color = Color(0xFF00F5D4), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Boost Donazioni: +${((article.donationMultiplierBoost - 1.0f) * 100).toInt()}%", color = TwitchGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VirtualModeratorCard(
    mod: VirtualModerator,
    onToggleHire: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TwitchSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (mod.isHired) Color(0xFF00F5D4) else TwitchBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(42.dp).background(TwitchSurfaceElevated, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(mod.avatarEmoji, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(mod.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Efficienza: ${mod.efficiencyPercent}% • Costo: ${"%.2f".format(mod.costPerLive)} € / live", color = TwitchGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(mod.description, color = TwitchTextMuted, fontSize = 10.sp)
                }
            }

            Button(
                onClick = onToggleHire,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mod.isHired) TwitchLiveRed else Color(0xFF00F5D4)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (mod.isHired) "Licenzia" else "Ingaggia",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun LurkerChannelCard(
    channel: VirtualLurkerChannel,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = TwitchSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(42.dp).background(TwitchSurfaceElevated, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(channel.avatarEmoji, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(channel.streamerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(channel.title, color = TwitchText, fontSize = 11.sp, maxLines = 1)
                    Text("${channel.category} • 👥 ${channel.viewersCount} spettatori", color = TwitchTextMuted, fontSize = 10.sp)
                }
            }
            Text("Guarda >", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun TweetCard(tweet: SocialTweet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TwitchSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(TwitchSurfaceElevated, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(tweet.avatarEmoji, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tweet.authorName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tweet.handle, color = TwitchTextMuted, fontSize = 11.sp)
                    }
                    Text(tweet.timeAgo, color = TwitchTextMuted, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tweet.content,
                color = TwitchText,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF2A6D), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${tweet.likesCount}", color = TwitchTextMuted, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Repeat, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${tweet.retweetsCount}", color = TwitchTextMuted, fontSize = 11.sp)
                }
            }
        }
    }
}
