package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TwitchBlack
import com.example.ui.theme.TwitchBorder
import com.example.ui.theme.TwitchSurface
import com.example.ui.theme.TwitchSurfaceElevated
import com.example.ui.theme.TwitchText
import com.example.ui.theme.TwitchTextMuted

@Composable
fun AuthScreen(
    onLoginSuccess: (channelName: String, pin: String, birthDate: String, avatar: String, bio: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var channelName by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("01/01/2000") }
    var selectedAvatar by remember { mutableStateOf("🎮") }
    var bio by remember { mutableStateOf("Streamer ufficiale su Zyro Live") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val avatars = listOf("🎮", "👑", "🎙️", "⚡", "🚀", "🎧", "🕹️", "👾", "🥊", "🎯", "🦊", "🐱")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TwitchBlack)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .testTag("auth_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Zyro",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Piattaforma Creator & Simulatore Live",
                    fontSize = 13.sp,
                    color = TwitchTextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Avatar Selector
                Text(
                    text = "Scegli il tuo Avatar / Logo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TwitchText,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    avatars.take(6).forEach { emoji ->
                        AvatarBadge(
                            emoji = emoji,
                            isSelected = selectedAvatar == emoji,
                            onSelect = { selectedAvatar = emoji }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    avatars.drop(6).take(6).forEach { emoji ->
                        AvatarBadge(
                            emoji = emoji,
                            isSelected = selectedAvatar == emoji,
                            onSelect = { selectedAvatar = emoji }
                        )
                    }
                }

                // Channel Name Field
                OutlinedTextField(
                    value = channelName,
                    onValueChange = { channelName = it },
                    label = { Text("Nome Canale Streamer") },
                    placeholder = { Text("es. IlTuoNomeLive") },
                    leadingIcon = {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("channel_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TwitchSurfaceElevated,
                        unfocusedContainerColor = TwitchSurfaceElevated,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = TwitchBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // PIN / Password Field
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Password / PIN Canale") },
                    placeholder = { Text("Minimo 4 caratteri") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TwitchSurfaceElevated,
                        unfocusedContainerColor = TwitchSurfaceElevated,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = TwitchBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Birth Date Field
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Data di Nascita (GG/MM/AAAA)") },
                    placeholder = { Text("es. 15/05/1998") },
                    leadingIcon = {
                        Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("birth_date_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TwitchSurfaceElevated,
                        unfocusedContainerColor = TwitchSurfaceElevated,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = TwitchBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (channelName.trim().length < 2) {
                            errorMessage = "Inserisci un nome canale valido (almeno 2 caratteri)"
                            return@Button
                        }
                        if (pin.trim().length < 3) {
                            errorMessage = "Inserisci un PIN/Password di almeno 3 caratteri"
                            return@Button
                        }
                        errorMessage = null
                        onLoginSuccess(channelName.trim(), pin.trim(), birthDate.trim(), selectedAvatar, bio.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Accedi e Apri il Canale",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🔒 I dati restano salvati in locale sul tuo telefono",
                    fontSize = 11.sp,
                    color = TwitchTextMuted
                )
            }
        }
    }
}

@Composable
fun AvatarBadge(
    emoji: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else TwitchSurfaceElevated,
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else TwitchBorder,
                shape = CircleShape
            )
            .clickable { onSelect() }
            .testTag("avatar_$emoji"),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 20.sp)
    }
}
