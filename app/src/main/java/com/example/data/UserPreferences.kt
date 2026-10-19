package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeAccent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "twitchflex_stream_prefs")

enum class StreamQuality(val label: String, val jpegCompress: Int, val resolutionLabel: String, val fps: Int) {
    LOW("Bassa (360p)", 50, "360p", 15),
    MEDIUM("Media (720p)", 75, "720p", 24),
    HIGH("Alta (1080p)", 90, "1080p", 30)
}

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_CHANNEL_NAME = stringPreferencesKey("channel_name")
        private val KEY_PIN = stringPreferencesKey("channel_pin")
        private val KEY_BIRTH_DATE = stringPreferencesKey("birth_date")
        private val KEY_AVATAR = stringPreferencesKey("avatar_emoji")
        private val KEY_BIO = stringPreferencesKey("bio")
        private val KEY_OFFLINE_BANNER = stringPreferencesKey("offline_banner")
        private val KEY_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_MONETIZATION_UNLOCKED = booleanPreferencesKey("monetization_unlocked")
        private val KEY_TOTAL_SUBS = intPreferencesKey("total_subs_all_time")
        private val KEY_TOTAL_STREAM_SECONDS = longPreferencesKey("total_stream_seconds")
        private val KEY_TOTAL_EARNINGS = doublePreferencesKey("total_earnings_accumulated")
        private val KEY_THEME_ACCENT = stringPreferencesKey("theme_accent")
        private val KEY_COMMANDS_JSON = stringPreferencesKey("custom_commands_json")
        private val KEY_CLIPS_JSON = stringPreferencesKey("saved_clips_json")
        private val KEY_GACHA_ITEMS_JSON = stringPreferencesKey("gacha_items_json")

        // First Live Tracker
        private val KEY_FIRST_LIVE_DONE = booleanPreferencesKey("is_first_live_done")

        // Gemini AI Configuration
        private val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_user_api_key")
        private val KEY_GEMINI_KEY_VERIFIED = booleanPreferencesKey("gemini_api_key_verified")
        private val KEY_GEMINI_AI_ENABLED = booleanPreferencesKey("gemini_ai_enabled")

        // AutoMod Configuration
        private val KEY_AUTOMOD_SEVERITY = stringPreferencesKey("automod_severity")
        private val KEY_AUTOMOD_CUSTOM_WORDS = stringPreferencesKey("automod_custom_words")

        // Wallet & Economy
        private val KEY_AVAILABLE_WALLET_BALANCE = doublePreferencesKey("available_wallet_balance")
        private val KEY_WITHDRAWN_TOTAL = doublePreferencesKey("withdrawn_total")
        private val KEY_WALLET_TRANSACTIONS_JSON = stringPreferencesKey("wallet_transactions_json")

        // Platform & Contracts
        private val KEY_PLATFORM_CONTRACT = stringPreferencesKey("platform_contract_id")
        private val KEY_BRAND_REPUTATION = intPreferencesKey("brand_reputation_score")
        private val KEY_HIRED_MODERATORS_JSON = stringPreferencesKey("hired_moderators_json")
        private val KEY_SUBATHON_CAP_SECONDS = longPreferencesKey("subathon_cap_seconds")

        // P2P Local stream keys
        private val KEY_P2P_ENABLED = booleanPreferencesKey("p2p_enabled")
        private val KEY_P2P_VISIBLE_NEARBY = booleanPreferencesKey("p2p_visible_nearby")
        private val KEY_P2P_QUALITY = stringPreferencesKey("p2p_quality")
        private val KEY_P2P_PORT = intPreferencesKey("p2p_port")
        private val KEY_P2P_PIN = stringPreferencesKey("p2p_pin")

        // Channel Hub & Profile Keys
        private val KEY_HANDLE = stringPreferencesKey("channel_handle")
        private val KEY_CUSTOM_BADGE_EMOJI = stringPreferencesKey("custom_badge_emoji")
        private val KEY_PROFILE_IMAGE_URI = stringPreferencesKey("profile_image_uri")
        private val KEY_SUB_GOALS_JSON = stringPreferencesKey("sub_goals_json")
        private val KEY_CHANNEL_USERS_JSON = stringPreferencesKey("channel_users_json")
        private val KEY_ONLY_CAM_MODE = booleanPreferencesKey("only_cam_mode")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_CHANNEL_NAME] ?: ""
        val pin = prefs[KEY_PIN] ?: ""
        val birthDate = prefs[KEY_BIRTH_DATE] ?: ""
        val avatar = prefs[KEY_AVATAR] ?: "🎮"
        val bio = prefs[KEY_BIO] ?: ""
        val banner = prefs[KEY_OFFLINE_BANNER] ?: "Benvenuto sul canale!"
        val loggedIn = prefs[KEY_LOGGED_IN] ?: false
        val handle = prefs[KEY_HANDLE] ?: if (name.isNotBlank()) "@${name.lowercase().replace(" ", "")}" else "@Streamer"
        val customBadge = prefs[KEY_CUSTOM_BADGE_EMOJI] ?: "👑"
        val imageUri = prefs[KEY_PROFILE_IMAGE_URI]

        UserProfile(
            channelName = name,
            pin = pin,
            birthDate = birthDate,
            avatarEmoji = avatar,
            bio = bio,
            offlineBannerText = banner,
            isRegistered = loggedIn && name.isNotBlank(),
            handle = handle,
            customBadgeEmoji = customBadge,
            profileImageUri = imageUri
        )
    }

    val subGoalsFlow: Flow<List<SubGoal>> = context.dataStore.data.map { prefs ->
        parseSubGoals(prefs[KEY_SUB_GOALS_JSON] ?: "")
    }

    val channelUsersFlow: Flow<List<ChannelUser>> = context.dataStore.data.map { prefs ->
        parseChannelUsers(prefs[KEY_CHANNEL_USERS_JSON] ?: "")
    }

    val monetizationUnlockedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_MONETIZATION_UNLOCKED] ?: false
    }

    val totalSubsFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOTAL_SUBS] ?: 0
    }

    val totalStreamSecondsFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOTAL_STREAM_SECONDS] ?: 0L
    }

    val totalEarningsFlow: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOTAL_EARNINGS] ?: 0.0
    }

    val availableWalletBalanceFlow: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[KEY_AVAILABLE_WALLET_BALANCE] ?: (prefs[KEY_TOTAL_EARNINGS] ?: 0.0)
    }

    val withdrawnTotalFlow: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[KEY_WITHDRAWN_TOTAL] ?: 0.0
    }

    val walletTransactionsFlow: Flow<List<WalletTransaction>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_WALLET_TRANSACTIONS_JSON] ?: ""
        parseWalletTransactions(json)
    }

    val firstLiveDoneFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_FIRST_LIVE_DONE] ?: false
    }

    val geminiApiKeyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_GEMINI_API_KEY] ?: ""
    }

    val geminiKeyVerifiedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_GEMINI_KEY_VERIFIED] ?: false
    }

    val geminiAiEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_GEMINI_AI_ENABLED] ?: true
    }

    val autoModSeverityFlow: Flow<AutoModSeverity> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_AUTOMOD_SEVERITY] ?: AutoModSeverity.MEDIUM.name
        try { AutoModSeverity.valueOf(raw) } catch (_: Exception) { AutoModSeverity.MEDIUM }
    }

    val customBlacklistWordsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_AUTOMOD_CUSTOM_WORDS] ?: ""
        if (raw.isBlank()) emptySet()
        else raw.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
    }

    val themeAccentFlow: Flow<ThemeAccent> = context.dataStore.data.map { prefs ->
        val savedName = prefs[KEY_THEME_ACCENT] ?: ThemeAccent.PURPLE.name
        try { ThemeAccent.valueOf(savedName) } catch (_: Exception) { ThemeAccent.PURPLE }
    }

    val customCommandsFlow: Flow<List<CustomChatCommand>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_COMMANDS_JSON] ?: ""
        parseCommands(json)
    }

    val savedClipsFlow: Flow<List<StreamClip>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_CLIPS_JSON] ?: ""
        parseClips(json)
    }

    val gachaUnlockedFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_GACHA_ITEMS_JSON] ?: ""
        parseGachaItemIds(json)
    }

    val p2pEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_P2P_ENABLED] ?: true
    }

    val p2pVisibleNearbyFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_P2P_VISIBLE_NEARBY] ?: true
    }

    val p2pQualityFlow: Flow<StreamQuality> = context.dataStore.data.map { prefs ->
        val saved = prefs[KEY_P2P_QUALITY] ?: StreamQuality.MEDIUM.name
        try { StreamQuality.valueOf(saved) } catch (_: Exception) { StreamQuality.MEDIUM }
    }

    val p2pPortFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_P2P_PORT] ?: 8080
    }

    val platformContractFlow: Flow<PlatformId> = context.dataStore.data.map { prefs ->
        val saved = prefs[KEY_PLATFORM_CONTRACT] ?: PlatformId.TWITCHFLEX.name
        try { PlatformId.valueOf(saved) } catch (_: Exception) { PlatformId.TWITCHFLEX }
    }

    val brandReputationFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_BRAND_REPUTATION] ?: 80
    }

    val hiredModeratorsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        parseStringList(prefs[KEY_HIRED_MODERATORS_JSON] ?: "")
    }

    val subathonCapSecondsFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_SUBATHON_CAP_SECONDS] ?: 43200L
    }

    val onlyCamModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONLY_CAM_MODE] ?: false
    }

    suspend fun setOnlyCamMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONLY_CAM_MODE] = enabled
        }
    }

    suspend fun saveRegistration(
        channelName: String,
        pin: String,
        birthDate: String,
        avatarEmoji: String,
        bio: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CHANNEL_NAME] = channelName
            prefs[KEY_PIN] = pin
            prefs[KEY_BIRTH_DATE] = birthDate
            prefs[KEY_AVATAR] = avatarEmoji
            prefs[KEY_BIO] = bio
            prefs[KEY_LOGGED_IN] = true
        }
    }

    suspend fun updateOfflineBanner(bannerText: String) {
        context.dataStore.edit { prefs -> prefs[KEY_OFFLINE_BANNER] = bannerText }
    }

    suspend fun updateMonetizationUnlocked(unlocked: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_MONETIZATION_UNLOCKED] = unlocked }
    }

    suspend fun markFirstLiveDone() {
        context.dataStore.edit { prefs -> prefs[KEY_FIRST_LIVE_DONE] = true }
    }

    suspend fun saveGeminiApiKey(key: String, verified: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GEMINI_API_KEY] = key
            prefs[KEY_GEMINI_KEY_VERIFIED] = verified
            if (!verified) {
                prefs[KEY_GEMINI_AI_ENABLED] = false
            }
        }
    }

    suspend fun setGeminiAiEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_GEMINI_AI_ENABLED] = enabled }
    }

    suspend fun setAutoModSeverity(severity: AutoModSeverity) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTOMOD_SEVERITY] = severity.name }
    }

    suspend fun addCustomBlacklistWord(word: String) {
        val current = customBlacklistWordsFlow.first().toMutableSet()
        val clean = word.trim().lowercase()
        if (clean.isNotBlank()) {
            current.add(clean)
            context.dataStore.edit { prefs ->
                prefs[KEY_AUTOMOD_CUSTOM_WORDS] = current.joinToString(",")
            }
        }
    }

    suspend fun removeCustomBlacklistWord(word: String) {
        val current = customBlacklistWordsFlow.first().toMutableSet()
        current.remove(word.trim().lowercase())
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTOMOD_CUSTOM_WORDS] = current.joinToString(",")
        }
    }

    suspend fun setPlatformContract(platform: PlatformId) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PLATFORM_CONTRACT] = platform.name
            // Automatically switch theme accent to match platform contract!
            prefs[KEY_THEME_ACCENT] = when (platform) {
                PlatformId.TWITCHFLEX -> ThemeAccent.PURPLE.name
                PlatformId.KICKFLEX -> ThemeAccent.KICK.name
                PlatformId.TROVOVERSE -> ThemeAccent.TROVO.name
            }
        }
    }

    suspend fun updateBrandReputation(delta: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_BRAND_REPUTATION] ?: 80
            prefs[KEY_BRAND_REPUTATION] = (current + delta).coerceIn(0, 100)
        }
    }

    suspend fun toggleHiredModerator(modId: String, isHired: Boolean) {
        val current = hiredModeratorsFlow.first().toMutableList()
        if (isHired && !current.contains(modId)) {
            current.add(modId)
        } else if (!isHired) {
            current.remove(modId)
        }
        val arr = JSONArray()
        current.forEach { arr.put(it) }
        context.dataStore.edit { prefs ->
            prefs[KEY_HIRED_MODERATORS_JSON] = arr.toString()
        }
    }

    suspend fun setSubathonCapSeconds(capSeconds: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SUBATHON_CAP_SECONDS] = capSeconds
        }
    }

    suspend fun addSubsAndEarnings(
        subsToAdd: Int,
        grossEarnings: Double,
        netEarnings: Double,
        zyroTax: Double
    ) {
        context.dataStore.edit { prefs ->
            val currentSubs = prefs[KEY_TOTAL_SUBS] ?: 0
            val newSubs = currentSubs + subsToAdd
            prefs[KEY_TOTAL_SUBS] = newSubs

            val currentEarnings = prefs[KEY_TOTAL_EARNINGS] ?: 0.0
            prefs[KEY_TOTAL_EARNINGS] = currentEarnings + netEarnings

            val currentBalance = prefs[KEY_AVAILABLE_WALLET_BALANCE] ?: currentEarnings
            prefs[KEY_AVAILABLE_WALLET_BALANCE] = currentBalance + netEarnings

            if (newSubs >= MonetizationCalculator.UNLOCK_THRESHOLD_SUBS) {
                prefs[KEY_MONETIZATION_UNLOCKED] = true
            }
        }

        // Add transaction log
        if (grossEarnings > 0.0) {
            addTransaction(
                WalletTransaction(
                    type = TransactionType.SUB_PAYOUT,
                    amount = grossEarnings,
                    description = "Incassi lordi sessione di live (+${subsToAdd} sub)"
                )
            )
        }
        addTransaction(
            WalletTransaction(
                type = TransactionType.ZYRO_TAX,
                amount = -zyroTax,
                description = "Tasse di Zyro - Costi server e gestione piattaforma"
            )
        )
    }

    suspend fun withdrawFunds(amount: Double, destination: String): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val balance = prefs[KEY_AVAILABLE_WALLET_BALANCE] ?: 0.0
            if (balance >= amount && amount > 0.0) {
                prefs[KEY_AVAILABLE_WALLET_BALANCE] = balance - amount
                val currentWithdrawn = prefs[KEY_WITHDRAWN_TOTAL] ?: 0.0
                prefs[KEY_WITHDRAWN_TOTAL] = currentWithdrawn + amount
                success = true
            }
        }
        if (success) {
            addTransaction(
                WalletTransaction(
                    type = TransactionType.WALLET_WITHDRAWAL,
                    amount = -amount,
                    description = "Trasferimento esterno verso $destination",
                    destinationAccount = destination
                )
            )
        }
        return success
    }

    suspend fun addTransaction(transaction: WalletTransaction) {
        val current = walletTransactionsFlow.first().toMutableList()
        current.add(0, transaction)
        val array = JSONArray()
        current.take(80).forEach { t ->
            array.put(JSONObject().apply {
                put("id", t.id)
                put("type", t.type.name)
                put("amount", t.amount)
                put("desc", t.description)
                put("dest", t.destinationAccount)
                put("time", t.timestamp)
            })
        }
        context.dataStore.edit { prefs ->
            prefs[KEY_WALLET_TRANSACTIONS_JSON] = array.toString()
        }
    }

    suspend fun addStreamDuration(seconds: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_TOTAL_STREAM_SECONDS] ?: 0L
            prefs[KEY_TOTAL_STREAM_SECONDS] = current + seconds
        }
    }

    suspend fun saveThemeAccent(accent: ThemeAccent) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME_ACCENT] = accent.name }
    }

    suspend fun setP2PEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_P2P_ENABLED] = enabled }
    }

    suspend fun setP2PVisibleNearby(visible: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_P2P_VISIBLE_NEARBY] = visible }
    }

    suspend fun setP2PQuality(quality: StreamQuality) {
        context.dataStore.edit { prefs -> prefs[KEY_P2P_QUALITY] = quality.name }
    }

    suspend fun setP2PPort(port: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_P2P_PORT] = port }
    }

    suspend fun saveCustomCommands(commands: List<CustomChatCommand>) {
        val array = JSONArray()
        commands.forEach { cmd ->
            array.put(JSONObject().apply {
                put("cmd", cmd.command)
                put("resp", cmd.response)
            })
        }
        context.dataStore.edit { prefs ->
            prefs[KEY_COMMANDS_JSON] = array.toString()
        }
    }

    suspend fun updateCustomCommand(oldCommand: String, newCommand: String, newResponse: String) {
        val current = customCommandsFlow.first().toMutableList()
        val index = current.indexOfFirst { it.command.equals(oldCommand, ignoreCase = true) }
        val formattedCmd = if (newCommand.startsWith("!")) newCommand else "!$newCommand"
        if (index != -1) {
            current[index] = CustomChatCommand(formattedCmd, newResponse)
        } else {
            current.add(CustomChatCommand(formattedCmd, newResponse))
        }
        saveCustomCommands(current)
    }

    suspend fun addCustomCommand(command: String, response: String) {
        val current = customCommandsFlow.first().toMutableList()
        val formattedCmd = if (command.startsWith("!")) command else "!$command"
        current.removeAll { it.command.equals(formattedCmd, ignoreCase = true) }
        current.add(CustomChatCommand(formattedCmd, response))
        saveCustomCommands(current)
    }

    suspend fun editCustomCommand(oldCommand: String, newCommand: String, newResponse: String) {
        updateCustomCommand(oldCommand, newCommand, newResponse)
    }

    suspend fun deleteCustomCommand(command: String) {
        val current = customCommandsFlow.first().toMutableList()
        current.removeAll { it.command.equals(command, ignoreCase = true) }
        saveCustomCommands(current)
    }

    suspend fun addSavedClip(clip: StreamClip) {
        val current = savedClipsFlow.first().toMutableList()
        current.add(0, clip)
        val array = JSONArray()
        current.take(50).forEach { c ->
            array.put(JSONObject().apply {
                put("id", c.id)
                put("title", c.title)
                put("category", c.categoryName)
                put("views", c.peakViewers)
                put("duration", c.durationFormatted)
                put("timestamp", c.timestamp)
            })
        }
        context.dataStore.edit { prefs ->
            prefs[KEY_CLIPS_JSON] = array.toString()
        }
    }

    suspend fun addClip(clip: StreamClip) {
        addSavedClip(clip)
    }

    suspend fun addTransactionLog(
        type: TransactionType,
        amount: Double,
        desc: String,
        dest: String
    ) {
        addTransaction(
            WalletTransaction(
                type = type,
                amount = amount,
                description = desc,
                destinationAccount = dest
            )
        )
    }

    suspend fun exportSaveJson(): String = exportBackupJson()
    suspend fun importSaveJson(json: String): Boolean = importBackupJson(json)

    suspend fun unlockGachaItem(itemId: String) {
        val current = gachaUnlockedFlow.first().toMutableSet()
        current.add(itemId)
        val array = JSONArray()
        current.forEach { array.put(it) }
        context.dataStore.edit { prefs ->
            prefs[KEY_GACHA_ITEMS_JSON] = array.toString()
        }
    }

    suspend fun exportBackupJson(): String {
        val prefs = context.dataStore.data.first()
        val json = JSONObject().apply {
            put("version", 4)
            put("app", "TwitchFlex")
            put("channel_name", prefs[KEY_CHANNEL_NAME] ?: "")
            put("channel_pin", prefs[KEY_PIN] ?: "")
            put("birth_date", prefs[KEY_BIRTH_DATE] ?: "")
            put("avatar_emoji", prefs[KEY_AVATAR] ?: "🎮")
            put("bio", prefs[KEY_BIO] ?: "")
            put("offline_banner", prefs[KEY_OFFLINE_BANNER] ?: "")
            put("monetization_unlocked", prefs[KEY_MONETIZATION_UNLOCKED] ?: false)
            put("total_subs", prefs[KEY_TOTAL_SUBS] ?: 0)
            put("total_stream_seconds", prefs[KEY_TOTAL_STREAM_SECONDS] ?: 0L)
            put("total_earnings", prefs[KEY_TOTAL_EARNINGS] ?: 0.0)
            put("available_wallet_balance", prefs[KEY_AVAILABLE_WALLET_BALANCE] ?: 0.0)
            put("withdrawn_total", prefs[KEY_WITHDRAWN_TOTAL] ?: 0.0)
            put("theme_accent", prefs[KEY_THEME_ACCENT] ?: ThemeAccent.PURPLE.name)
            put("custom_commands", prefs[KEY_COMMANDS_JSON] ?: "")
            put("saved_clips", prefs[KEY_CLIPS_JSON] ?: "")
            put("gacha_items", prefs[KEY_GACHA_ITEMS_JSON] ?: "")
            put("gemini_api_key", prefs[KEY_GEMINI_API_KEY] ?: "")
            put("gemini_verified", prefs[KEY_GEMINI_KEY_VERIFIED] ?: false)
            put("automod_severity", prefs[KEY_AUTOMOD_SEVERITY] ?: AutoModSeverity.MEDIUM.name)
            put("automod_custom_words", prefs[KEY_AUTOMOD_CUSTOM_WORDS] ?: "")
        }
        return json.toString(2)
    }

    suspend fun importBackupJson(backupString: String): Boolean {
        return try {
            val root = JSONObject(backupString)
            context.dataStore.edit { prefs ->
                if (root.has("channel_name")) prefs[KEY_CHANNEL_NAME] = root.getString("channel_name")
                if (root.has("channel_pin")) prefs[KEY_PIN] = root.getString("channel_pin")
                if (root.has("birth_date")) prefs[KEY_BIRTH_DATE] = root.getString("birth_date")
                if (root.has("avatar_emoji")) prefs[KEY_AVATAR] = root.getString("avatar_emoji")
                if (root.has("bio")) prefs[KEY_BIO] = root.getString("bio")
                if (root.has("offline_banner")) prefs[KEY_OFFLINE_BANNER] = root.getString("offline_banner")
                if (root.has("monetization_unlocked")) prefs[KEY_MONETIZATION_UNLOCKED] = root.getBoolean("monetization_unlocked")
                if (root.has("total_subs")) prefs[KEY_TOTAL_SUBS] = root.getInt("total_subs")
                if (root.has("total_stream_seconds")) prefs[KEY_TOTAL_STREAM_SECONDS] = root.getLong("total_stream_seconds")
                if (root.has("total_earnings")) prefs[KEY_TOTAL_EARNINGS] = root.getDouble("total_earnings")
                if (root.has("available_wallet_balance")) prefs[KEY_AVAILABLE_WALLET_BALANCE] = root.getDouble("available_wallet_balance")
                if (root.has("withdrawn_total")) prefs[KEY_WITHDRAWN_TOTAL] = root.getDouble("withdrawn_total")
                if (root.has("theme_accent")) prefs[KEY_THEME_ACCENT] = root.getString("theme_accent")
                if (root.has("custom_commands")) prefs[KEY_COMMANDS_JSON] = root.getString("custom_commands")
                if (root.has("saved_clips")) prefs[KEY_CLIPS_JSON] = root.getString("saved_clips")
                if (root.has("gacha_items")) prefs[KEY_GACHA_ITEMS_JSON] = root.getString("gacha_items")
                if (root.has("gemini_api_key")) prefs[KEY_GEMINI_API_KEY] = root.getString("gemini_api_key")
                if (root.has("gemini_verified")) prefs[KEY_GEMINI_KEY_VERIFIED] = root.getBoolean("gemini_verified")
                if (root.has("automod_severity")) prefs[KEY_AUTOMOD_SEVERITY] = root.getString("automod_severity")
                if (root.has("automod_custom_words")) prefs[KEY_AUTOMOD_CUSTOM_WORDS] = root.getString("automod_custom_words")
                prefs[KEY_LOGGED_IN] = true
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs -> prefs[KEY_LOGGED_IN] = false }
    }

    private fun parseCommands(json: String): List<CustomChatCommand> {
        val list = mutableListOf<CustomChatCommand>()
        if (json.isBlank()) {
            return listOf(
                CustomChatCommand("!discord", "Unisciti al server Discord ufficiale: discord.gg/twitchflex 🔥"),
                CustomChatCommand("!social", "Seguimi su Instagram & TikTok: @twitchflex_stream 📸"),
                CustomChatCommand("!setup", "PC: RTX 4080 • Mic: Shure SM7B • Cam: Sony A6400 4K ⚡"),
                CustomChatCommand("!regole", "No bestemmie, no insulti, rispetto per tutti in chat! 🛡️")
            )
        }
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(CustomChatCommand(obj.getString("cmd"), obj.getString("resp")))
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseClips(json: String): List<StreamClip> {
        val list = mutableListOf<StreamClip>()
        if (json.isBlank()) return emptyList()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    StreamClip(
                        id = obj.optString("id", ""),
                        title = obj.optString("title", "Clip"),
                        categoryName = obj.optString("category", "Live"),
                        peakViewers = obj.optInt("viewers", 50),
                        durationFormatted = obj.optString("duration", "00:30"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseGachaItemIds(json: String): List<String> {
        val list = mutableListOf<String>()
        if (json.isBlank()) return listOf("setup_keyboard")
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseStringList(json: String): List<String> {
        val list = mutableListOf<String>()
        if (json.isBlank()) return emptyList()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseWalletTransactions(json: String): List<WalletTransaction> {
        val list = mutableListOf<WalletTransaction>()
        if (json.isBlank()) return emptyList()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeStr = obj.optString("type", TransactionType.SUB_PAYOUT.name)
                val type = try { TransactionType.valueOf(typeStr) } catch (_: Exception) { TransactionType.SUB_PAYOUT }
                list.add(
                    WalletTransaction(
                        id = obj.optString("id", ""),
                        type = type,
                        amount = obj.optDouble("amount", 0.0),
                        description = obj.optString("desc", ""),
                        destinationAccount = obj.optString("dest", ""),
                        timestamp = obj.optLong("time", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    suspend fun updateUserProfile(
        channelName: String,
        handle: String,
        bio: String,
        avatarEmoji: String,
        customBadgeEmoji: String,
        profileImageUri: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CHANNEL_NAME] = channelName
            prefs[KEY_HANDLE] = handle
            prefs[KEY_BIO] = bio
            prefs[KEY_AVATAR] = avatarEmoji
            prefs[KEY_CUSTOM_BADGE_EMOJI] = customBadgeEmoji
            if (profileImageUri != null) {
                prefs[KEY_PROFILE_IMAGE_URI] = profileImageUri
            }
        }
    }

    suspend fun saveSubGoals(goals: List<SubGoal>) {
        val array = JSONArray()
        goals.forEach { g ->
            array.put(JSONObject().apply {
                put("id", g.id)
                put("title", g.title)
                put("current", g.currentAmount)
                put("target", g.targetAmount)
                put("active", g.isActive)
                put("type", g.type.name)
            })
        }
        context.dataStore.edit { prefs ->
            prefs[KEY_SUB_GOALS_JSON] = array.toString()
        }
    }

    private fun parseSubGoals(json: String): List<SubGoal> {
        val list = mutableListOf<SubGoal>()
        if (json.isBlank() || json.contains("Goal Nuova Cam HD")) {
            return emptyList()
        }
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeStr = obj.optString("type", SubGoalType.NEW_SUBS.name)
                val type = try { SubGoalType.valueOf(typeStr) } catch (_: Exception) { SubGoalType.NEW_SUBS }
                list.add(
                    SubGoal(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        title = obj.optString("title", "Nuovo Goal"),
                        currentAmount = obj.optInt("current", 0),
                        targetAmount = obj.optInt("target", 10),
                        isActive = obj.optBoolean("active", false),
                        type = type
                    )
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return list
    }

    suspend fun saveChannelUsers(users: List<ChannelUser>) {
        val array = JSONArray()
        users.forEach { u ->
            array.put(JSONObject().apply {
                put("id", u.id)
                put("username", u.username)
                put("isSub", u.isSub)
                put("subTier", u.subTier)
                put("months", u.monthsSubbed)
                put("isVip", u.isVip)
                put("isMod", u.isMod)
                put("renewal", u.renewalDate)
                put("activity", u.chatActivityScore)
                put("avatar", u.avatarEmoji)
                put("daysAgo", u.followedDaysAgo)
            })
        }
        context.dataStore.edit { prefs ->
            prefs[KEY_CHANNEL_USERS_JSON] = array.toString()
        }
    }

    private fun parseChannelUsers(json: String): List<ChannelUser> {
        val list = mutableListOf<ChannelUser>()
        if (json.isBlank() || json.contains("UserGamer99")) {
            return emptyList()
        }
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChannelUser(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        username = obj.optString("username", "Viewer"),
                        isSub = obj.optBoolean("isSub", false),
                        subTier = obj.optInt("subTier", 1),
                        monthsSubbed = obj.optInt("months", 1),
                        isVip = obj.optBoolean("isVip", false),
                        isMod = obj.optBoolean("isMod", false),
                        renewalDate = obj.optString("renewal", "-"),
                        chatActivityScore = obj.optInt("activity", 80),
                        avatarEmoji = obj.optString("avatar", "😎"),
                        followedDaysAgo = obj.optInt("daysAgo", 0)
                    )
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return list
    }
}
