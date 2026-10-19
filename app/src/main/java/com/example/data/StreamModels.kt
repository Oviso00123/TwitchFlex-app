package com.example.data

import java.util.UUID

enum class CategoryType {
    IRL,
    TRENDING,
    GIOCHI,
    EDINATR,
    MECCANICO,
    LAVORO,
    MARATONA,
    NICHE,
    CHATTING
}

data class StreamCategory(
    val id: String,
    val name: String,
    val shortName: String,
    val type: CategoryType,
    val description: String,
    val iconEmoji: String,
    val subConversionRate: Float,
    val competitionFactor: Float
) {
    companion object {
        val ALL = listOf(
            StreamCategory(
                id = "irl",
                name = "IRL (In Real Life)",
                shortName = "IRL",
                type = CategoryType.IRL,
                description = "Streaming all'aperto o in movimento. Picchi improvvisi di spettatori con dialoghi e attività!",
                iconEmoji = "🚶‍♂️",
                subConversionRate = 1.0f,
                competitionFactor = 1.3f
            ),
            StreamCategory(
                id = "giochi",
                name = "Giochi (Gaming)",
                shortName = "Giochi",
                type = CategoryType.GIOCHI,
                description = "I titoli più giocati al mondo. Scalabilità illimitata oltre 50.000 spettatori.",
                iconEmoji = "🎮",
                subConversionRate = 1.0f,
                competitionFactor = 1.1f
            ),
            StreamCategory(
                id = "edinatr",
                name = "Edinatr",
                shortName = "Edinatr",
                type = CategoryType.EDINATR,
                description = "Format interattivo speciale con show, sfide e reazioni live ad alto coinvolgimento.",
                iconEmoji = "⚡",
                subConversionRate = 1.8f,
                competitionFactor = 0.8f
            ),
            StreamCategory(
                id = "trending",
                name = "Trending",
                shortName = "Trending",
                type = CategoryType.TRENDING,
                description = "Argomenti e novità virali del momento. Grande visibilità e crescita organica.",
                iconEmoji = "🔥",
                subConversionRate = 1.1f,
                competitionFactor = 1.2f
            ),
            StreamCategory(
                id = "meccanico",
                name = "Meccanico",
                shortName = "Meccanico",
                type = CategoryType.MECCANICO,
                description = "Lavorazioni in officina, motori, riparazioni e customizzazioni dal vivo. Community appassionata!",
                iconEmoji = "🔧",
                subConversionRate = 1.8f,
                competitionFactor = 0.7f
            ),
            StreamCategory(
                id = "lavoro",
                name = "Live a lavoro",
                shortName = "Lavoro",
                type = CategoryType.LAVORO,
                description = "La quotidianità lavorativa in diretta: ufficio, cucina, artigianato o smart working.",
                iconEmoji = "💼",
                subConversionRate = 1.3f,
                competitionFactor = 0.9f
            ),
            StreamCategory(
                id = "maratona",
                name = "Maratona (Subathon)",
                shortName = "Subathon",
                type = CategoryType.MARATONA,
                description = "Subathon no-stop! Il timer sale di +30s per ogni sub e +10s per ogni euro donato. Funzione Sleep Stream!",
                iconEmoji = "⏱️",
                subConversionRate = 2.4f,
                competitionFactor = 0.6f
            ),
            StreamCategory(
                id = "niche",
                name = "Nicchia Specialistica",
                shortName = "Nicchia",
                type = CategoryType.NICHE,
                description = "Meno spettatori complessivi ma fidelizzazione altissima e altissima conversione in sub.",
                iconEmoji = "🎯",
                subConversionRate = 2.0f,
                competitionFactor = 0.5f
            ),
            StreamCategory(
                id = "chatting",
                name = "Solo Chiacchiere (Just Chatting)",
                shortName = "Chat",
                type = CategoryType.CHATTING,
                description = "Interazione diretta e continua con la chat, reazioni a video e meme.",
                iconEmoji = "💬",
                subConversionRate = 1.2f,
                competitionFactor = 1.2f
            )
        )

        val DEFAULT = ALL[0]
    }
}

enum class UserBadge(val emoji: String, val colorHex: String, val label: String) {
    BROADCASTER("👑", "#E91E63", "Streamer"),
    MODERATOR("⚔️", "#00F5D4", "Moderatore"),
    MOD("⚔️", "#00F5D4", "Moderatore"),
    TRUSTED_MOD("🛡️", "#00E5FF", "Mod di Fiducia"),
    VIP("💎", "#FFD700", "VIP"),
    REGULAR("🥈", "#C0C0C0", "Viewer Regolare"),
    SUBSCRIBER("⭐", "#9146FF", "Abbonato"),
    SUB("⭐", "#9146FF", "Abbonato"),
    PRIME("👑", "#00B5D8", "Prime"),
    HATER("☠️", "#FF3333", "Hater"),
    VIEWER("", "#ADADB8", "Spettatore")
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val message: String,
    val badge: UserBadge = UserBadge.VIEWER,
    val colorHex: String = "#FFFFFF",
    val timestamp: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false,
    val isLocalP2P: Boolean = false,
    val isHater: Boolean = false,
    val isBanned: Boolean = false,
    val isFilteredByAutoMod: Boolean = false,
    val isVipModerated: Boolean = false,
    val loyaltyScore: Int = 0
)

enum class SubTier(val displayName: String, val price: Double) {
    TIER_1("Livello 1", 3.99),
    TIER_2("Livello 2", 7.99),
    TIER_3("Livello 3", 19.99),
    PRIME("Twitch Prime", 0.0)
}

data class SubEvent(
    val id: String = UUID.randomUUID().toString(),
    val subscriberName: String,
    val tier: SubTier,
    val months: Int = 1,
    val message: String = "",
    val payoutAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class DaySubscriber(
    val id: String = UUID.randomUUID().toString(),
    val fullName: String,
    val tier: SubTier,
    val timeFormatted: String,
    val payoutAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class DonationEvent(
    val id: String = UUID.randomUUID().toString(),
    val donorName: String,
    val bits: Int,
    val euros: Double,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AutoModSeverity(val displayName: String, val description: String) {
    LOW("Basso", "Filtra solo insulti gravi e volgarità pesanti"),
    MEDIUM("Medio", "Filtra insulti, bestemmie, spam e parole offensive"),
    HIGH("Alto", "Filtra insulti, spam, link, caps eccessivo e slang aggressivo")
}

object AutoModFilter {
    val LOW_WORDS = listOf(
        "cagna", "stronzo", "bastardo", "troia", "puttana", "ritardato", "frocio"
    )
    val MEDIUM_WORDS = LOW_WORDS + listOf(
        "merda", "cazzo", "idiota", "scam", "truffa", "nabbo", "schifo", "cancro",
        "deficiente", "hater", "mongolo", "cretino", "fesso", "porco"
    )
    val HIGH_WORDS = MEDIUM_WORDS + listOf(
        "noob", "scarso", "bot", "disinstalla", "fai schifo", "follow4follow",
        "sub4sub", "discord.gg", "guarda il mio canale", "pessimo", "vergognati",
        "dona a me", "clown", "l streamer"
    )

    fun filterMessage(text: String, severity: AutoModSeverity, customWords: Set<String>): Pair<String, Boolean> {
        val baseWords = when (severity) {
            AutoModSeverity.LOW -> LOW_WORDS
            AutoModSeverity.MEDIUM -> MEDIUM_WORDS
            AutoModSeverity.HIGH -> HIGH_WORDS
        }
        val allForbidden = (baseWords + customWords.map { it.lowercase() }).distinct()
        var filtered = text
        var hadCensorship = false

        for (forbidden in allForbidden) {
            if (forbidden.isBlank()) continue
            val pattern = "(?i)\\b${Regex.escape(forbidden)}\\b".toRegex()
            if (pattern.containsMatchIn(filtered)) {
                hadCensorship = true
                val stars = "*".repeat(forbidden.length.coerceAtLeast(3))
                filtered = pattern.replace(filtered, stars)
            }
        }
        return Pair(filtered, hadCensorship)
    }
}

enum class TransactionType(val label: String, val iconEmoji: String, val isDeduction: Boolean) {
    SUB_PAYOUT("Abbonamento Sub", "⭐", false),
    HOURLY_PAYOUT("Tariffa Oraria Zyro", "⏱️", false),
    DONATION_BITS("Donazione Bits TTS", "💎", false),
    SPONSOR_REWARD("Compenso Sponsor", "💼", false),
    BOUNTY_REWARD("Sfida Spettatore (Bounty)", "🎁", false),
    PLATFORM_STIPEND("Stipendio Piattaforma", "📜", false),
    ZYRO_TAX("Tasse di Zyro (Piattaforma)", "🏛️", true),
    MODERATOR_SALARY("Stipendio Moderatore", "🛡️", true),
    SELF_PROMO_DONATION("Auto-promozione Lurker", "👀", true),
    WALLET_WITHDRAWAL("Trasferimento Esterno (Prelievo)", "💳", true)
}

data class WalletTransaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val destinationAccount: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class StreamSessionSummary(
    val durationSeconds: Long,
    val peakViewers: Int,
    val totalSubs: Int,
    val grossSubEarnings: Double,
    val grossHourlyEarnings: Double,
    val grossDonationEarnings: Double,
    val grossSponsorEarnings: Double = 0.0,
    val zyroTax: Double = 5.50,
    val platformCut: Double = 0.0,
    val netEarnings: Double,
    val categoryName: String,
    val aiCoachFeedback: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// 1. BRAND DEALS & MISSIONI SPONSOR A TEMPO
// ==========================================
enum class BrandId(
    val brandName: String,
    val categoryTag: String,
    val emoji: String,
    val colorHex: String,
    val defaultSlogan: String
) {
    GIGAFUEL("GigaFuel", "Energy Drink", "⚡", "#FFB100", "L'energia infinita per ogni maratona da vero streamer!"),
    RAZEK("RazeK", "Periferiche Gaming & Tastiere RGB", "⌨️", "#00F5D4", "Precisione ottica e illuminazione Chroma personalizzata."),
    BORGOR_KING("Borgor King", "Fast Food Croccante", "🍔", "#FF5722", "Il panino con triplo bacon che ti sazia in live!"),
    COREX("CoreX", "Hardware PC & GPU", "💻", "#00E5FF", "Massimi FPS, temperature polari e zero frame drop."),
    HYPEWEAR("HypeWear", "Abbigliamento Streetwear", "🧢", "#E040FB", "Stile underground per creator fuori dal comune.")
}

enum class SponsorMissionType(val title: String, val shortDesc: String) {
    PITCH_60S("Il Pitch di 60 Secondi", "Parla con entusiasmo del prodotto al microfono per 60 secondi!"),
    SHOW_PRODUCT_30S("Mostra il Prodotto", "Tieni il prodotto davanti alla fotocamera per 30 secondi!"),
    DISCOUNT_CODE("Il Codice Sconto", "Fai inserire o pronuncia il codice promozionale entro 2 minuti!"),
    BRAND_SAFE_SILENCE("Regola del Silenzio (Brand Safe)", "Nessun insulto o parola offensiva per 5 minuti!")
}

data class ActiveSponsorMission(
    val id: String = UUID.randomUUID().toString(),
    val brand: BrandId,
    val type: SponsorMissionType,
    val title: String,
    val description: String,
    val rewardEuros: Double,
    val discountCode: String = "FLEX20",
    val durationSecondsTotal: Int,
    val secondsRemaining: Int,
    val progressPercent: Float = 0f,
    val isHoldingProduct: Boolean = false,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false
)

// ==========================================
// 2. CONTRATTI DI ESCLUSIVA & PIATTAFORME RIVALI
// ==========================================
enum class PlatformId(
    val platformName: String,
    val subSplitPercent: Int,
    val monthlyStipend: Double,
    val liveHourlyBonus: Double,
    val moderationRules: String,
    val themeAccentHex: String,
    val cancellationPenalty: Double,
    val description: String,
    val logoEmoji: String
) {
    TWITCHFLEX(
        platformName = "TwitchFlex",
        subSplitPercent = 50,
        monthlyStipend = 0.0,
        liveHourlyBonus = 2.50,
        moderationRules = "Standard (AutoMod Severo)",
        themeAccentHex = "#9146FF",
        cancellationPenalty = 0.0,
        description = "La piattaforma originale di riferimento. Grande pubblico e algoritmi bilanciati.",
        logoEmoji = "🟣"
    ),
    KICKFLEX(
        platformName = "KickFlex",
        subSplitPercent = 95,
        monthlyStipend = 250.0,
        liveHourlyBonus = 15.00,
        moderationRules = "Permissiva (Tolleranza alta, contratti milionari)",
        themeAccentHex = "#53FC18",
        cancellationPenalty = 120.0,
        description = "Split 95/5 senza precedenti! Trattieni quasi tutto il valore dei sub con stipendio orario alto.",
        logoEmoji = "🟢"
    ),
    TROVOVERSE(
        platformName = "TrovoVerse",
        subSplitPercent = 70,
        monthlyStipend = 100.0,
        liveHourlyBonus = 8.00,
        moderationRules = "Gamificata (Incentivi a gettoni e scatole premio)",
        themeAccentHex = "#00E676",
        cancellationPenalty = 60.0,
        description = "Piattaforma dinamica con missioni comunitarie, gemme e bonus continui.",
        logoEmoji = "🔷"
    )
}

data class PlatformContract(
    val currentPlatform: PlatformId = PlatformId.TWITCHFLEX,
    val signedTimestamp: Long = System.currentTimeMillis(),
    val totalEarningsOnPlatform: Double = 0.0
)

// ==========================================
// 3. SUBATHON SYSTEM (MODALITÀ MARATONA)
// ==========================================
data class SubathonMilestone(
    val id: String = UUID.randomUUID().toString(),
    val targetSubs: Int,
    val rewardText: String,
    val isReached: Boolean = false
)

data class SubathonState(
    val isEnabled: Boolean = false,
    val secondsRemaining: Long = 7200L, // Default 2 ore iniziali
    val timerCapSeconds: Long = 43200L, // Max 12 ore di tetto
    val subIncrementSeconds: Int = 30,  // +30s per sub
    val euroDonationIncrementSeconds: Int = 10, // +10s per euro
    val isSleepStreamMode: Boolean = false,
    val milestones: List<SubathonMilestone> = listOf(
        SubathonMilestone(targetSubs = 10, rewardText = "🎵 Canzone scelta dalla chat in karaoke"),
        SubathonMilestone(targetSubs = 25, rewardText = "🍕 Ordino pizza speciale in live"),
        SubathonMilestone(targetSubs = 50, rewardText = "🎨 Tour della camera & setup completo"),
        SubathonMilestone(targetSubs = 100, rewardText = "⭐ Maratona estesa a 24 ore no stop!")
    )
)

// ==========================================
// 4. SCHERMATA AFK & MINI-GIOCHI IN CHAT
// ==========================================
data class AfkState(
    val isAfk: Boolean = false,
    val afkSecondsElapsed: Int = 0,
    val message: String = "Torno subito! ☕ Pausa caffè per lo streamer",
    val activeQuizQuestion: String = "Qual è il gioco più giocato su TwitchFlex?",
    val activeQuizOptions: List<String> = listOf("A) Fortnite", "B) GTA V", "C) Minecraft", "D) IRL Tour"),
    val activeQuizAnswerIndex: Int = 0,
    val quizWinner: String? = null,
    val lotteryWinner: String? = null
)

// ==========================================
// 5. EVOLUZIONE FAN & MOD DI FIDUCIA
// ==========================================
enum class FanRank(val label: String, val badge: UserBadge, val minPoints: Int) {
    VIEWER("Spettatore Base", UserBadge.VIEWER, 0),
    REGULAR("Viewer Regolare", UserBadge.REGULAR, 15),
    VIP("VIP della Community", UserBadge.VIP, 45),
    TRUSTED_MOD("Mod di Fiducia", UserBadge.TRUSTED_MOD, 100)
}

data class FanUser(
    val username: String,
    val loyaltyPoints: Int,
    val rank: FanRank,
    val messagesCount: Int = 0,
    val subsGifted: Int = 0
)

// ==========================================
// 6. SFIDE SU DONAZIONE (VIEWER BOUNTIES)
// ==========================================
data class ViewerBounty(
    val id: String = UUID.randomUUID().toString(),
    val donorName: String,
    val bountyEuros: Double,
    val challengeDescription: String,
    val durationSeconds: Int = 60,
    val secondsLeft: Int = 60,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// 7. PRESSING MEDIATICO & NOTIZIE VIRTUALI
// ==========================================
data class VirtualNewsArticle(
    val id: String = UUID.randomUUID().toString(),
    val newspaperName: String,
    val headline: String,
    val snippet: String,
    val dateAgo: String = "Poco fa",
    val reputationImpact: Int, // es. +10, -5
    val donationMultiplierBoost: Float = 1.0f,
    val tagEmoji: String = "📰"
)

// ==========================================
// 8. MERCATO DEI MODERATORI VIRTUALI
// ==========================================
data class VirtualModerator(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val costPerLive: Double,
    val efficiencyPercent: Int,
    val description: String,
    val isHired: Boolean = false
) {
    companion object {
        val ALL = listOf(
            VirtualModerator(
                id = "autobot_v1",
                name = "AutoBot v1.0",
                avatarEmoji = "🤖",
                costPerLive = 4.50,
                efficiencyPercent = 55,
                description = "Script automatizzato basico. Riconosce spam e link ovvi, costa poco.",
                isHired = false
            ),
            VirtualModerator(
                id = "mod_sara",
                name = "Sara (Mod Esperta)",
                avatarEmoji = "👩‍💻",
                costPerLive = 18.00,
                efficiencyPercent = 82,
                description = "Esperta della community. Silenzia i troll, accoglie i nuovi sub ed è sempre attiva.",
                isHired = false
            ),
            VirtualModerator(
                id = "gigamod_marco",
                name = "GigaMod Marco",
                avatarEmoji = "🛡️",
                costPerLive = 38.00,
                efficiencyPercent = 98,
                description = "Il moderatore supremo. Reattività istantanea a insulti e raid negativi, risponde con comandi personalizzati.",
                isHired = false
            )
        )
    }
}

// ==========================================
// 9. GUASTI TECNICI & HARDWARE GLITCHES
// ==========================================
enum class GlitchType(
    val title: String,
    val description: String,
    val emoji: String,
    val fixAction: String,
    val tapsNeeded: Int
) {
    MIC_DISTORTION("Microfono che gracchia!", "La voce è robotica e distorta! Ricalibra il cavo XLR!", "🎙️", "Ricollega Jack 🔌", 3),
    GPU_OVERHEAT("Surriscaldamento GPU!", "La scheda video tocca i 95°C! Attiva il raffreddamento forzato!", "💻", "Overboost Ventole ❄️", 4),
    CAMERA_BLUR("Webcam sfocata!", "L'autofocus è andato in tilt! Ricalibra le lenti!", "📹", "Pulisci Obiettivo 🔍", 3),
    ETHERNET_DROP("Cavo Ethernet staccato!", "Il bitrate sta crollando a 0 kbps! Rimetti il cavo prima che cada la live!", "📶", "Ricollega Cavo ⚡", 4)
}

data class ActiveHardwareGlitch(
    val id: String = UUID.randomUUID().toString(),
    val type: GlitchType,
    val tapsCompleted: Int = 0,
    val secondsLeft: Int = 15,
    val isFixed: Boolean = false
)

// ==========================================
// 10. MODALITÀ LURKER & CANALI VIRTUALI
// ==========================================
data class VirtualLurkerChannel(
    val id: String,
    val streamerName: String,
    val avatarEmoji: String,
    val category: String,
    val title: String,
    val viewersCount: Int,
    val bio: String,
    val recentChatMessages: List<ChatMessage>
)

// ==========================================
// ALTRI MODELLI ESISTENTI (Invariati)
// ==========================================
enum class RandomEventType(
    val title: String,
    val description: String,
    val actionLabel: String,
    val emoji: String,
    val timeLimitSeconds: Int
) {
    CAT_KEYBOARD(
        "Il gatto sulla tastiera!",
        "Il gatto sta camminando sui tasti e rischia di chiudere il gioco!",
        "Allontana il gatto 🐱",
        "🐾",
        10
    ),
    CONNECTION_LAG(
        "Lag temporaneo!",
        "I frame stanno droppando! Riavvia il bitrate per non perdere spettatori!",
        "Riavvia Bitrate ⚡",
        "📶",
        12
    ),
    PIZZA_DELIVERY(
        "È arrivato il rider della pizza!",
        "Il citofono suona: intrattieni la chat o prendi una fetta in live!",
        "Prendi la Pizza 🍕",
        "🍕",
        15
    ),
    MICROPHONE_GLITCH(
        "Microfono che gracchia!",
        "La chat scrive 'NON SI SENTE', regola subito la sensibilità audio!",
        "Regola Gain 🎙️",
        "🎙️",
        10
    )
}

data class ActiveRandomEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: RandomEventType,
    val secondsRemaining: Int,
    val resolved: Boolean = false
)

data class CustomChatCommand(
    val command: String,
    val response: String
)

data class StreamClip(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val categoryName: String,
    val peakViewers: Int,
    val durationFormatted: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SocialTweet(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val handle: String,
    val avatarEmoji: String,
    val content: String,
    val likesCount: Int,
    val retweetsCount: Int,
    val timeAgo: String = "Adesso"
)

data class GachaItem(
    val id: String,
    val name: String,
    val rarity: String,
    val emoji: String,
    val boostDescription: String,
    val colorHex: String
)

data class LeaderboardCreator(
    val rank: Int,
    val name: String,
    val avatarEmoji: String,
    val category: String,
    val subsCount: Int,
    val viewersAvg: Int,
    val isPlayer: Boolean = false
)

object MonetizationCalculator {
    const val UNLOCK_THRESHOLD_SUBS = 10
    const val ZYRO_FIXED_TAX = 5.50

    fun getPayoutPerSub(totalSubs: Int, platform: PlatformId = PlatformId.TWITCHFLEX): Double {
        val baseRate = 3.20
        return when (platform) {
            PlatformId.TWITCHFLEX -> baseRate
            PlatformId.KICKFLEX -> baseRate * 1.60
            PlatformId.TROVOVERSE -> baseRate * 1.25
        }
    }

    fun getTierName(totalSubs: Int, platform: PlatformId = PlatformId.TWITCHFLEX): String {
        val prefix = when (platform) {
            PlatformId.TWITCHFLEX -> "TwitchFlex"
            PlatformId.KICKFLEX -> "KickFlex Partner"
            PlatformId.TROVOVERSE -> "Trovo Master"
        }
        val rate = "%.2f".format(getPayoutPerSub(totalSubs, platform))
        return when {
            totalSubs < 100 -> "$prefix Esordiente ($rate € / Sub)"
            totalSubs <= 10_000 -> "$prefix Affiliato ($rate € / Sub)"
            totalSubs <= 100_000 -> "$prefix Verificato ($rate € / Sub)"
            totalSubs <= 1_000_000 -> "$prefix Top Creator ($rate € / Sub)"
            else -> "$prefix Leggenda Ufficiale ($rate € / Sub)"
        }
    }

    fun calculateHourlyEarnings(durationSeconds: Long, viewers: Int, platform: PlatformId = PlatformId.TWITCHFLEX): Double {
        // Glitch penny-per-second eliminato al 100%: nessun incremento continuo a ogni secondo
        return 0.0
    }
}

enum class DevPayoutMode(val displayName: String) {
    DEFAULT("Predefinito (3.20€ / Sub, -5.50€ Tassa)"),
    CUSTOM_RATE("Tariffa Personalizzata per Sub"),
    PERFORMANCE("Basato su Performance Live")
}

enum class DevDonationRate(val displayName: String) {
    NATURAL("Naturale / Bassa (Consigliata)"),
    MEDIUM("Media"),
    DISABLED("Disattivata")
}

data class UserProfile(
    val channelName: String,
    val pin: String,
    val birthDate: String,
    val avatarEmoji: String = "🎮",
    val bio: String = "Streamer su TwitchFlex",
    val offlineBannerText: String = "Bentornato su TwitchFlex! In live quasi ogni giorno alle 21:00!",
    val isRegistered: Boolean = false,
    val handle: String = "@MaloReplay",
    val customBadgeEmoji: String = "👑",
    val profileImageUri: String? = null
)

// ==========================================
// PANNELLO GESTIONE CANALE & HUB CREATOR
// ==========================================

data class ChannelUser(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val isSub: Boolean = true,
    val subTier: Int = 1, // 1, 2, 3 o 4 (Gifted)
    val monthsSubbed: Int = 1,
    val isVip: Boolean = false,
    val isMod: Boolean = false,
    val renewalDate: String = "15/11/2026",
    val chatActivityScore: Int = 85, // 0-100 attività chat
    val avatarEmoji: String = "😎",
    val followedDaysAgo: Int = 45
)

enum class SubGoalType(val label: String, val unit: String, val emoji: String) {
    NEW_SUBS("Nuove Sub", "Sub", "⭐"),
    DONATIONS_EURO("Obiettivo Donazioni (€)", "€", "💎"),
    NEW_FOLLOWERS("Nuovi Follower", "Follower", "👥")
}

data class SubGoal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val currentAmount: Int,
    val targetAmount: Int,
    val isActive: Boolean = true,
    val type: SubGoalType = SubGoalType.NEW_SUBS
)

// ==========================================
// QUEST & MISSIONI SEGRETE DEL CREATOR
// ==========================================

enum class QuestType(val emoji: String, val label: String) {
    SECRET_WORD("🤫", "Parola Segreta"),
    FAST_POLL("📊", "Sondaggio Lampo"),
    ANSWER_CHAT("💬", "Risposta Community"),
    HYPE_BOOST("🚀", "Hype Train Booster"),
    SNAP_CLIP("🎬", "Snap Clip")
}

data class CreatorSecretQuest(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val triggerKeyword: String? = null,
    val questType: QuestType,
    val popularityBonus: Int = 20,
    val isCompleted: Boolean = false
)

// ==========================================
// SONDAGGI DINAMICI (CONDITIONAL POLLS)
// ==========================================

data class PollOption(
    val id: Int,
    val text: String,
    val votes: Int = 0,
    val keywordBias: String = ""
)

data class StreamPoll(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val options: List<PollOption>,
    val totalVotes: Int = 0,
    val isActive: Boolean = true,
    val secondsLeft: Int = 60
)

// ==========================================
// RITMO E FILTRI DELLA CHAT LIVE
// ==========================================

enum class ChatRhythm(val label: String, val speedMs: Long) {
    CALM("Modalità Chill ☕", 3200L),
    HYPE("Hype Wave 🔥", 800L)
}

enum class ChatFilter(val label: String, val iconEmoji: String) {
    ALL("Tutti", "💬"),
    HIGHLIGHTS("Highlight (Sub/Donazioni/VIP)", "⭐"),
    QUESTIONS("Domande (?)", "❓")
}

// ==========================================
// MINIGIOCO HYPE TRAIN
// ==========================================

data class HypeTrainState(
    val isActive: Boolean = false,
    val level: Int = 1,
    val progress: Float = 0f, // 0f .. 1f
    val secondsLeft: Int = 30
)

