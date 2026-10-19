package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.data.AutoModSeverity
import com.example.data.CustomChatCommand
import com.example.data.DevPayoutMode
import com.example.data.DevDonationRate
import com.example.data.StreamQuality
import com.example.ui.components.TuStylePurple
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.TwitchBlack
import com.example.ui.theme.TwitchBorder
import com.example.ui.theme.TwitchGold
import com.example.ui.theme.TwitchLiveRed
import com.example.ui.theme.TwitchSurface
import com.example.ui.theme.TwitchSurfaceElevated
import com.example.ui.theme.TwitchText
import com.example.ui.theme.TwitchTextMuted
import com.example.viewmodel.StreamViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: StreamViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsState()
    val currentThemeAccent by viewModel.themeAccent.collectAsState()
    val customCommands by viewModel.customCommands.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val geminiKeyVerified by viewModel.geminiKeyVerified.collectAsState()
    val geminiAiEnabled by viewModel.geminiAiEnabled.collectAsState()
    val autoModSeverity by viewModel.autoModSeverity.collectAsState()
    val customBlacklistWords by viewModel.customBlacklistWords.collectAsState()

    var inputApiKey by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
    var isVerifyingKey by remember { mutableStateOf(false) }
    var verificationStatusMessage by remember { mutableStateOf<String?>(null) }

    var showBackupExportDialog by remember { mutableStateOf(false) }
    var showBackupImportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var importInputText by remember { mutableStateOf("") }

    // Developer Secret Settings (PIN: 200908)
    val isDeveloperUnlocked by viewModel.isDeveloperUnlocked.collectAsState()
    val devPayoutMode by viewModel.devPayoutMode.collectAsState()
    val devCustomSubRate by viewModel.devCustomSubRate.collectAsState()
    val devDonationRate by viewModel.devDonationRate.collectAsState()
    val onlyCamMode by viewModel.onlyCamMode.collectAsState()
    var showDevPinDialog by remember { mutableStateOf(false) }
    var devPinInput by remember { mutableStateOf("") }

    // Custom Commands state
    var newCmdName by remember { mutableStateOf("") }
    var newCmdResp by remember { mutableStateOf("") }
    var editingCommand by remember { mutableStateOf<CustomChatCommand?>(null) }
    var editCmdName by remember { mutableStateOf("") }
    var editCmdResp by remember { mutableStateOf("") }

    // AutoMod new blacklist word
    var newBlacklistWord by remember { mutableStateOf("") }

    // P2P Settings
    val p2pEnabled by viewModel.preferences.p2pEnabledFlow.collectAsState(initial = true)
    val p2pVisibleNearby by viewModel.preferences.p2pVisibleNearbyFlow.collectAsState(initial = true)
    val p2pQuality by viewModel.preferences.p2pQualityFlow.collectAsState(initial = StreamQuality.MEDIUM)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TwitchBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Impostazioni TwitchFlex",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Gemini AI, Payout Zyro, AutoMod e Comandi Chat",
                    fontSize = 11.sp,
                    color = TwitchTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. SEZIONE API KEY DI GEMINI & CHAT IA AVANZATA
        Card(
            modifier = Modifier.fillMaxWidth().testTag("gemini_api_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (geminiKeyVerified) Color(0xFF00F5D4).copy(alpha = 0.6f) else TwitchBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chiave API Google Gemini", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(
                    text = "Inserisci la tua API Key di Gemini per generare chat intelligente in tempo reale e analisi coach post-live personalizzata.",
                    color = TwitchTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = inputApiKey,
                    onValueChange = { inputApiKey = it },
                    placeholder = { Text("Incolla AIzaSy...", fontSize = 12.sp, color = TwitchTextMuted) },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("gemini_key_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TwitchSurfaceElevated,
                        unfocusedContainerColor = TwitchSurfaceElevated,
                        focusedBorderColor = Color(0xFF00F5D4),
                        unfocusedBorderColor = TwitchBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isVerifyingKey = true
                            verificationStatusMessage = null
                            viewModel.verifyAndSaveGeminiKey(inputApiKey.trim()) { ok, msg ->
                                isVerifyingKey = false
                                verificationStatusMessage = msg
                            }
                        },
                        enabled = !isVerifyingKey,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4)),
                        modifier = Modifier.testTag("verify_gemini_key_button")
                    ) {
                        if (isVerifyingKey) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verifica in corso...", color = Color.Black, fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verifica Key", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (geminiKeyVerified) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF00F5D4).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("✅ Verificata", color = Color(0xFF00F5D4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                verificationStatusMessage?.let {
                    Text(
                        text = it,
                        color = if (geminiKeyVerified) Color(0xFF00F5D4) else TwitchLiveRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle abilitato SOLO DOPO verifica
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Chat IA Avanzata (Gemini Flash)", color = if (geminiKeyVerified) Color.White else TwitchTextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            text = if (geminiKeyVerified) "Attiva reazioni contestuali in tempo reale" else "Richiede prima la verifica della chiave",
                            color = TwitchTextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = geminiAiEnabled && geminiKeyVerified,
                        onCheckedChange = { viewModel.toggleGeminiAiEnabled(it) },
                        enabled = geminiKeyVerified,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F5D4), checkedTrackColor = Color(0xFF00F5D4).copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("gemini_ai_toggle")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. PANNELLO "ACCORDO E PAYOUT ZYRO"
        Card(
            modifier = Modifier.fillMaxWidth().testTag("zyro_payout_panel_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = TwitchGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Accordo e Payout Zyro (Dove e quanto paga)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text("Come e quanto paga la piattaforma:", color = TwitchGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                PayoutRuleItem(
                    title = "⭐ Tariffa Sub Reale: 3,20 €",
                    description = "Ogni Sub produce esattamente 3,20€ netti per lo streamer. Trattenuta piattaforma: 10,00€ ogni 10 Sub (mostrata solo nel report post-live)."
                )
                PayoutRuleItem(
                    title = "🏛️ Trattenuta Fissa Tasse Zyro: 5,50 €",
                    description = "A ogni fine live vengono addebitati esattamente 5,50€ dal saldo per copertura costi server, regia e banda P2P."
                )
                PayoutRuleItem(
                    title = "💳 Metodo di Pagamento",
                    description = "Accumulo continuo sul saldo del canale, con funzione di prelievo manuale istantaneo verso PayPal, IBAN bancario o Revolut."
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. AUTOMOD SYSTEM MANAGEMENT
        Card(
            modifier = Modifier.fillMaxWidth().testTag("automod_settings_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFF9800))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sistema AutoMod della Chat", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(
                    text = "Monitora e censura automaticamente le parole della blacklist sostituendole con asterischi (***).",
                    color = TwitchTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text("Livello di Severità:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AutoModSeverity.values().forEach { sev ->
                        val isSelected = sev == autoModSeverity
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) Color(0xFFFF9800) else TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                                .border(1.dp, if (isSelected) Color(0xFFFF9800) else TwitchBorder, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setAutoModSeverity(sev) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sev.displayName,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Text(autoModSeverity.description, color = TwitchTextMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(10.dp))

                // Custom blacklist words
                Text("Blacklist Parole Personalizzate (${customBlacklistWords.size}):", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newBlacklistWord,
                        onValueChange = { newBlacklistWord = it },
                        placeholder = { Text("Aggiungi parola proibita...", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = TwitchSurfaceElevated, unfocusedContainerColor = TwitchSurfaceElevated, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (newBlacklistWord.isNotBlank()) {
                                viewModel.addCustomBlacklistWord(newBlacklistWord.trim())
                                newBlacklistWord = ""
                            }
                        },
                        modifier = Modifier.size(42.dp).background(Color(0xFFFF9800), RoundedCornerShape(6.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aggiungi", tint = Color.Black, modifier = Modifier.size(18.dp))
                    }
                }

                if (customBlacklistWords.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        customBlacklistWords.take(8).forEach { word ->
                            Box(
                                modifier = Modifier
                                    .background(TwitchSurfaceElevated, RoundedCornerShape(12.dp))
                                    .border(1.dp, TwitchBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(word, color = Color.White, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Rimuovi",
                                        tint = TwitchLiveRed,
                                        modifier = Modifier.size(12.dp).clickable { viewModel.removeCustomBlacklistWord(word) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. COMANDI CHAT PERSONALIZZATI (GESTIONE COMPLETA: CREA, MODIFICA, ELIMINA)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("custom_commands_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comandi Chat Personalizzati (Crea / Modifica / Elimina)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                customCommands.forEach { cmd ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .background(TwitchSurfaceElevated, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cmd.command, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(cmd.response, color = TwitchText, fontSize = 11.sp, maxLines = 1)
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    editingCommand = cmd
                                    editCmdName = cmd.command
                                    editCmdResp = cmd.response
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = Color(0xFF00F5D4), modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { viewModel.deleteCustomCommand(cmd.command) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = TwitchLiveRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCmdName,
                        onValueChange = { newCmdName = it },
                        placeholder = { Text("!comando", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = TwitchSurfaceElevated, unfocusedContainerColor = TwitchSurfaceElevated, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = newCmdResp,
                        onValueChange = { newCmdResp = it },
                        placeholder = { Text("Risposta bot", fontSize = 11.sp) },
                        modifier = Modifier.weight(1.5f).height(44.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = TwitchSurfaceElevated, unfocusedContainerColor = TwitchSurfaceElevated, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (newCmdName.isNotBlank() && newCmdResp.isNotBlank()) {
                                viewModel.addCustomCommand(newCmdName.trim(), newCmdResp.trim())
                                newCmdName = ""
                                newCmdResp = ""
                            }
                        },
                        modifier = Modifier.size(42.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aggiungi", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. GESTIONE RETE LOCALE P2P
        Card(
            modifier = Modifier.fillMaxWidth().testTag("p2p_settings_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WifiTethering, contentDescription = null, tint = Color(0xFF00F5D4))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gestione Rete Locale P2P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Streaming P2P Abilitato", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = p2pEnabled,
                        onCheckedChange = { scope.launch { viewModel.preferences.setP2PEnabled(it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F5D4))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Visibile nelle Vicinanze (Discovery)", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = p2pVisibleNearby,
                        onCheckedChange = { scope.launch { viewModel.preferences.setP2PVisibleNearby(it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F5D4))
                    )
                }

                Text("Qualità Stream Locale:", color = TwitchTextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StreamQuality.values().forEach { q ->
                        val isSel = q == p2pQuality
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSel) Color(0xFF00F5D4) else TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                                .clickable { scope.launch { viewModel.preferences.setP2PQuality(q) } }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(q.label, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. ONLY CAM MODE (FOCUS MODE)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("only_cam_mode_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (onlyCamMode) Color(0xFF00F5D4) else TwitchBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (onlyCamMode) Color(0xFF00F5D4).copy(alpha = 0.2f) else TwitchSurfaceElevated,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Videocam,
                                contentDescription = null,
                                tint = if (onlyCamMode) Color(0xFF00F5D4) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Modalità \"Only Cam\"", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Esperienza live pulita senza distrazioni", color = TwitchTextMuted, fontSize = 10.sp)
                        }
                    }
                    Switch(
                        checked = onlyCamMode,
                        onCheckedChange = { viewModel.setOnlyCamMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F5D4)),
                        modifier = Modifier.testTag("only_cam_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quando abilitata, durante le dirette streaming vengono nascosti tutti gli overlay grafici (chat, widget sub goal, sponsor e banner), lasciando visibili solo il feed grezzo della fotocamera e i contatori di spettatori e timer.",
                    color = TwitchTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 7. BACKUP & EXPORT/IMPORT
        Card(
            modifier = Modifier.fillMaxWidth().testTag("backup_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("💾 Esportazione & Ripristino Salvataggio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "Conserva sub, livelli, guadagni e dotazione gacha senza perdere nulla durante gli aggiornamenti.",
                    color = TwitchTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                exportedJsonText = viewModel.exportSaveJson()
                                showBackupExportDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4))
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Esporta", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showBackupImportDialog = true },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TwitchSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F5D4))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Importa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 7. THEME ACCENT SELECTOR
        Card(
            modifier = Modifier.fillMaxWidth().testTag("theme_selector_card"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("🎨 Colore Accento TwitchFlex", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                ThemeAccent.values().forEach { accent ->
                    val isSelected = accent == currentThemeAccent
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(if (isSelected) accent.color.copy(alpha = 0.2f) else TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                            .border(1.dp, if (isSelected) accent.color else TwitchBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setThemeAccent(accent) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(18.dp).background(accent.color, CircleShape))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(accent.displayName, color = Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = accent.color, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 8. LOGOUT
        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("logout_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TwitchLiveRed.copy(alpha = 0.2f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchLiveRed)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = TwitchLiveRed, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Esci / Cambia Canale", color = TwitchLiveRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 9. SECRET DEVELOPER SETTINGS (PIN: 200908)
        if (!isDeveloperUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDevPinDialog = true }
                    .padding(vertical = 12.dp)
                    .testTag("dev_settings_trigger"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚙️ Menu Sviluppatore Nascosto (Accesso Riservato)",
                    color = TwitchTextMuted.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("dev_settings_card"),
                colors = CardDefaults.cardColors(containerColor = TwitchSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00F5D4))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛠️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Impostazioni Sviluppatore (PIN 200908)",
                                color = Color(0xFF00F5D4),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF00F5D4).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("SBLOCCATO", color = Color(0xFF00F5D4), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Payment Customization
                    Text("Modalità Calcolo Payout Sub:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    DevPayoutMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(
                                    if (devPayoutMode == mode) Color(0xFF00F5D4).copy(alpha = 0.15f) else TwitchSurface,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (devPayoutMode == mode) Color(0xFF00F5D4) else TwitchBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setDevPayoutMode(mode) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mode.displayName, color = Color.White, fontSize = 11.sp, fontWeight = if (devPayoutMode == mode) FontWeight.Bold else FontWeight.Normal)
                            if (devPayoutMode == mode) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (devPayoutMode == DevPayoutMode.CUSTOM_RATE) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tariffa Sub Personalizzata:", color = TwitchTextMuted, fontSize = 11.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { viewModel.setDevCustomSubRate(devCustomSubRate - 0.50) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TwitchSurface),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(28.dp)
                                ) { Text("-", color = Color.White, fontWeight = FontWeight.Bold) }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("%.2f €".format(devCustomSubRate), color = TwitchGold, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.setDevCustomSubRate(devCustomSubRate + 0.50) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TwitchSurface),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(28.dp)
                                ) { Text("+", color = Color.White, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Donation Rate Tuning
                    Text("Frequenza Donazioni Simulate (Tuning):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    DevDonationRate.values().forEach { rate ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(
                                    if (devDonationRate == rate) TuStylePurple.copy(alpha = 0.25f) else TwitchSurface,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (devDonationRate == rate) TuStylePurple else TwitchBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setDevDonationRate(rate) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(rate.displayName, color = Color.White, fontSize = 11.sp, fontWeight = if (devDonationRate == rate) FontWeight.Bold else FontWeight.Normal)
                            if (devDonationRate == rate) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = TuStylePurple, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // EDIT COMMAND DIALOG
    editingCommand?.let { cmd ->
        AlertDialog(
            onDismissRequest = { editingCommand = null },
            title = { Text("Modifica Comando Chat", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editCmdName,
                        onValueChange = { editCmdName = it },
                        label = { Text("Nome Comando (es. !discord)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editCmdResp,
                        onValueChange = { editCmdResp = it },
                        label = { Text("Risposta Automatica") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editCmdName.isNotBlank() && editCmdResp.isNotBlank()) {
                            viewModel.editCustomCommand(cmd.command, editCmdName.trim(), editCmdResp.trim())
                            editingCommand = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Salva Modifica", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCommand = null }) { Text("Annulla", color = Color.White) }
            },
            containerColor = TwitchSurface
        )
    }

    // EXPORT DIALOG
    if (showBackupExportDialog) {
        AlertDialog(
            onDismissRequest = { showBackupExportDialog = false },
            title = { Text("Salvataggio Backup Esportato", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Copia questo codice JSON per conservarlo o trasferirlo:", color = TwitchTextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("TwitchFlex Backup", exportedJsonText))
                        Toast.makeText(context, "Backup copiato negli appunti!", Toast.LENGTH_SHORT).show()
                        showBackupExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Copia negli Appunti", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupExportDialog = false }) { Text("Chiudi", color = Color.White) }
            },
            containerColor = TwitchSurface
        )
    }

    // IMPORT DIALOG
    if (showBackupImportDialog) {
        AlertDialog(
            onDismissRequest = { showBackupImportDialog = false },
            title = { Text("Importa Salvataggio", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Incolla qui il codice JSON del backup precedentemente salvato:", color = TwitchTextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        placeholder = { Text("Incolla JSON qui...") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = viewModel.importSaveJson(importInputText.trim())
                            if (success) {
                                Toast.makeText(context, "Salvataggio ripristinato con successo!", Toast.LENGTH_SHORT).show()
                                showBackupImportDialog = false
                            } else {
                                Toast.makeText(context, "Errore: Formato backup non valido", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4))
                ) {
                    Text("Ripristina Ora", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupImportDialog = false }) { Text("Annulla", color = Color.White) }
            },
            containerColor = TwitchSurface
        )
    }

    // DEVELOPER PIN DIALOG (PIN: 200908)
    if (showDevPinDialog) {
        AlertDialog(
            onDismissRequest = { showDevPinDialog = false },
            title = { Text("Accesso Sviluppatore", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Inserisci il PIN segreto per accedere alle impostazioni avanzate dello sviluppatore:", color = TwitchTextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = devPinInput,
                        onValueChange = { devPinInput = it },
                        label = { Text("PIN di Sblocco") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().testTag("dev_pin_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = viewModel.unlockDeveloperMenu(devPinInput.trim())
                        if (ok) {
                            Toast.makeText(context, "Accesso Sviluppatore Sbloccato con Successo!", Toast.LENGTH_SHORT).show()
                            showDevPinDialog = false
                            devPinInput = ""
                        } else {
                            Toast.makeText(context, "PIN Errato!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4))
                ) {
                    Text("Sblocca", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDevPinDialog = false }) { Text("Annulla", color = Color.White) }
            },
            containerColor = TwitchSurface
        )
    }
}

@Composable
fun PayoutRuleItem(
    title: String,
    description: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(description, color = TwitchTextMuted, fontSize = 11.sp, lineHeight = 15.sp)
    }
}
