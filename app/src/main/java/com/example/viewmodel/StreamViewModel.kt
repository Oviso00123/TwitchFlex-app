package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffect
import com.example.audio.SoundboardManager
import com.example.audio.VoiceActivityDetector
import com.example.data.ActiveHardwareGlitch
import com.example.data.ActiveRandomEvent
import com.example.data.ActiveSponsorMission
import com.example.data.AfkState
import com.example.data.AutoModFilter
import com.example.data.AutoModSeverity
import com.example.data.BrandId
import com.example.data.CategoryType
import com.example.data.ChatMessage
import com.example.data.CustomChatCommand
import com.example.data.DaySubscriber
import com.example.data.DonationEvent
import com.example.data.FanRank
import com.example.data.FanUser
import com.example.data.GachaItem
import com.example.data.GeminiChatService
import com.example.data.GeminiChatRepository
import com.example.data.GlitchType
import com.example.data.LeaderboardCreator
import com.example.data.MonetizationCalculator
import com.example.data.PlatformId
import com.example.data.RandomEventType
import com.example.data.SocialTweet
import com.example.data.SponsorMissionType
import com.example.data.StreamCategory
import com.example.data.StreamClip
import com.example.data.StreamSessionSummary
import com.example.data.SubEvent
import com.example.data.SubTier
import com.example.data.SubathonState
import com.example.data.TransactionType
import com.example.data.UserBadge
import com.example.data.UserPreferences
import com.example.data.UserProfile
import com.example.data.ChannelUser
import com.example.data.SubGoal
import com.example.data.SubGoalType
import com.example.data.ViewerBounty
import com.example.data.VirtualLurkerChannel
import com.example.data.DevPayoutMode
import com.example.data.DevDonationRate
import com.example.data.VirtualModerator
import com.example.data.VirtualNewsArticle
import com.example.data.WalletTransaction
import com.example.data.CreatorSecretQuest
import com.example.data.QuestType
import com.example.data.StreamPoll
import com.example.data.PollOption
import com.example.data.ChatRhythm
import com.example.data.ChatFilter
import com.example.data.HypeTrainState
import com.example.p2p.LocalStreamDiscovery
import com.example.p2p.LocalStreamServer
import com.example.p2p.NotificationHelper
import com.example.ui.theme.ThemeAccent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class LiveUiState(
    val isLive: Boolean = false,
    val streamTitle: String = "Diretta Streaming",
    val durationSeconds: Long = 0L,
    val viewersCount: Int = 0,
    val peakViewers: Int = 0,
    val currentCategory: StreamCategory = StreamCategory.DEFAULT,
    val isMuted: Boolean = false,
    val isFrontCamera: Boolean = true,
    val isNightStream: Boolean = false,
    val chatMessages: List<ChatMessage> = emptyList(),
    val latestSubAlert: SubEvent? = null,
    val latestDonationAlert: DonationEvent? = null,
    val activeRandomEvent: ActiveRandomEvent? = null,
    val gratitudeBonusActive: Boolean = false,
    val gratitudeSecondsLeft: Int = 0,
    val aiCooldownSeconds: Int = 0,
    val sessionSubEarnings: Double = 0.0,
    val sessionDonationEarnings: Double = 0.0,
    val sessionHourlyEarnings: Double = 0.0,
    val sessionSponsorEarnings: Double = 0.0,
    val sessionBountyEarnings: Double = 0.0,
    val sessionSubsCount: Int = 0,
    val subGoalCurrent: Int = 0,
    val subGoalTarget: Int = 0,
    val sponsorBannerText: String = "",
    val localIpAddress: String = "127.0.0.1",
    val localServerRunning: Boolean = false,
    val activeSponsorMission: ActiveSponsorMission? = null,
    val afkState: AfkState = AfkState(),
    val activeViewerBounty: ViewerBounty? = null,
    val activeHardwareGlitch: ActiveHardwareGlitch? = null,
    val subathonState: SubathonState = SubathonState(),
    val isCamBlurred: Boolean = false,
    val creatorEnergy: Float = 100f,
    val chatRhythm: ChatRhythm = ChatRhythm.CALM,
    val activeQuests: List<CreatorSecretQuest> = emptyList(),
    val activePoll: StreamPoll? = null,
    val hypeTrain: HypeTrainState = HypeTrainState(),
    val celebrationFxTriggered: Boolean = false,
    val lastSnappedClip: StreamClip? = null,
    val hasRareBigDonationOccurred: Boolean = false
)

data class HypeStockData(
    val currentPrice: Double = 0.0,
    val changePercent: Double = 0.0,
    val reputationScore: Int = 0,
    val points: List<Double> = emptyList()
)

class StreamViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = UserPreferences(application)
    val voiceDetector = VoiceActivityDetector(application)
    val soundboardManager = SoundboardManager(application)
    val geminiRepository = GeminiChatRepository()
    val geminiService = GeminiChatService(geminiRepository)
    val notificationHelper = NotificationHelper(application)
    val streamDiscovery = LocalStreamDiscovery()

    private val _userProfile = MutableStateFlow(UserProfile("", "", "", "🎮", "", "", false))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _themeAccent = MutableStateFlow(ThemeAccent.PURPLE)
    val themeAccent: StateFlow<ThemeAccent> = _themeAccent.asStateFlow()

    private val _monetizationUnlocked = MutableStateFlow(false)
    val monetizationUnlocked: StateFlow<Boolean> = _monetizationUnlocked.asStateFlow()

    private val _totalSubsAllTime = MutableStateFlow(0)
    val totalSubsAllTime: StateFlow<Int> = _totalSubsAllTime.asStateFlow()

    private val _totalStreamSecondsAllTime = MutableStateFlow(0L)
    val totalStreamSecondsAllTime: StateFlow<Long> = _totalStreamSecondsAllTime.asStateFlow()

    private val _totalEarningsAllTime = MutableStateFlow(0.0)
    val totalEarningsAllTime: StateFlow<Double> = _totalEarningsAllTime.asStateFlow()

    private val _availableWalletBalance = MutableStateFlow(0.0)
    val availableWalletBalance: StateFlow<Double> = _availableWalletBalance.asStateFlow()

    private val _withdrawnTotal = MutableStateFlow(0.0)
    val withdrawnTotal: StateFlow<Double> = _withdrawnTotal.asStateFlow()

    private val _walletTransactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val walletTransactions: StateFlow<List<WalletTransaction>> = _walletTransactions.asStateFlow()

    private val _customCommands = MutableStateFlow<List<CustomChatCommand>>(emptyList())
    val customCommands: StateFlow<List<CustomChatCommand>> = _customCommands.asStateFlow()

    private val _savedClips = MutableStateFlow<List<StreamClip>>(emptyList())
    val savedClips: StateFlow<List<StreamClip>> = _savedClips.asStateFlow()

    private val _unlockedGachaItemIds = MutableStateFlow<List<String>>(emptyList())
    val unlockedGachaItemIds: StateFlow<List<String>> = _unlockedGachaItemIds.asStateFlow()

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _geminiKeyVerified = MutableStateFlow(false)
    val geminiKeyVerified: StateFlow<Boolean> = _geminiKeyVerified.asStateFlow()

    private val _geminiAiEnabled = MutableStateFlow(true)
    val geminiAiEnabled: StateFlow<Boolean> = _geminiAiEnabled.asStateFlow()

    private val _isFirstLiveDone = MutableStateFlow(false)
    val isFirstLiveDone: StateFlow<Boolean> = _isFirstLiveDone.asStateFlow()

    private val _autoModSeverity = MutableStateFlow(AutoModSeverity.MEDIUM)
    val autoModSeverity: StateFlow<AutoModSeverity> = _autoModSeverity.asStateFlow()

    private val _customBlacklistWords = MutableStateFlow<Set<String>>(emptySet())
    val customBlacklistWords: StateFlow<Set<String>> = _customBlacklistWords.asStateFlow()

    private val _liveState = MutableStateFlow(LiveUiState())
    val liveState: StateFlow<LiveUiState> = _liveState.asStateFlow()

    private val _daySubscribers = MutableStateFlow<List<DaySubscriber>>(emptyList())
    val daySubscribers: StateFlow<List<DaySubscriber>> = _daySubscribers.asStateFlow()

    private val _postStreamSummary = MutableStateFlow<StreamSessionSummary?>(null)
    val postStreamSummary: StateFlow<StreamSessionSummary?> = _postStreamSummary.asStateFlow()

    private val _hypeStock = MutableStateFlow(HypeStockData())
    val hypeStock: StateFlow<HypeStockData> = _hypeStock.asStateFlow()

    private val _socialTweets = MutableStateFlow<List<SocialTweet>>(emptyList())
    val socialTweets: StateFlow<List<SocialTweet>> = _socialTweets.asStateFlow()

    private val _subAlertFlow = MutableSharedFlow<SubEvent>()
    val subAlertFlow: SharedFlow<SubEvent> = _subAlertFlow.asSharedFlow()

    // 1. Contratti & Piattaforme Rivali
    private val _currentPlatform = MutableStateFlow(PlatformId.TWITCHFLEX)
    val currentPlatform: StateFlow<PlatformId> = _currentPlatform.asStateFlow()

    // 2. Brand Reputation
    private val _brandReputation = MutableStateFlow(0)
    val brandReputation: StateFlow<Int> = _brandReputation.asStateFlow()

    // 3. Moderatori Virtuali
    private val _virtualModerators = MutableStateFlow<List<VirtualModerator>>(VirtualModerator.ALL)
    val virtualModerators: StateFlow<List<VirtualModerator>> = _virtualModerators.asStateFlow()

    // 4. Pressing Mediatico & Notizie Virtuali
    private val _virtualNews = MutableStateFlow<List<VirtualNewsArticle>>(emptyList())
    val virtualNews: StateFlow<List<VirtualNewsArticle>> = _virtualNews.asStateFlow()

    // 5. Fan Loyalty & VIP
    private val _fanUsers = MutableStateFlow<Map<String, FanUser>>(emptyMap())
    val fanUsers: StateFlow<Map<String, FanUser>> = _fanUsers.asStateFlow()

    // 6. Modalità Lurker (Canali Virtuali)
    private val _lurkerChannels = MutableStateFlow<List<VirtualLurkerChannel>>(emptyList())
    val lurkerChannels: StateFlow<List<VirtualLurkerChannel>> = _lurkerChannels.asStateFlow()

    // 7. Pannello Gestione Canale & Hub Creator
    private val _subGoals = MutableStateFlow<List<SubGoal>>(emptyList())
    val subGoals: StateFlow<List<SubGoal>> = _subGoals.asStateFlow()

    val activeSubGoal: StateFlow<SubGoal?> = _subGoals.map { list ->
        list.firstOrNull { it.isActive }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _channelUsers = MutableStateFlow<List<ChannelUser>>(emptyList())
    val channelUsers: StateFlow<List<ChannelUser>> = _channelUsers.asStateFlow()

    // Only Cam Mode for Live HUD
    private val _onlyCamMode = MutableStateFlow(false)
    val onlyCamMode: StateFlow<Boolean> = _onlyCamMode.asStateFlow()

    fun setOnlyCamMode(enabled: Boolean) {
        _onlyCamMode.value = enabled
        viewModelScope.launch { preferences.setOnlyCamMode(enabled) }
    }

    // Real Stream Viewer History for Analytics Line Chart
    private val _viewerHistoryPoints = MutableStateFlow<List<Float>>(emptyList())
    val viewerHistoryPoints: StateFlow<List<Float>> = _viewerHistoryPoints.asStateFlow()

    // Coroutine Jobs
    private var streamTimerJob: Job? = null
    private var viewersSimulationJob: Job? = null
    private var aiChatLoopJob: Job? = null
    private var voiceComprehensionJob: Job? = null
    private var localChatFillJob: Job? = null
    private var randomEventJob: Job? = null
    private var afkLoopJob: Job? = null
    private var localServer: LocalStreamServer? = null

    val allGachaItems = listOf(
        GachaItem("mic_pro", "Microfono Shure SM7B", "Epico", "🎙️", "+15% Chiarezza voce e rispetto chat", "#9146FF"),
        GachaItem("chair_neon", "Sedia Gaming Neon RGB", "Raro", "🪑", "+10% Resistenza e stamina live", "#00F5D4"),
        GachaItem("cam_4k", "Webcam 4K Ultra Broadcast", "Leggendario", "📹", "+25% Qualità video e spettatori IRL", "#FFD700"),
        GachaItem("setup_keyboard", "Tastiera Meccanica Custom", "Comune", "⌨️", "+5% Hype durante il gaming", "#ADADB8"),
        GachaItem("light_ring", "Ring Light da Studio", "Raro", "💡", "+10% Illuminazione streamer", "#00F5D4"),
        GachaItem("trophy_gold", "Trofeo Zyro Creator 100k", "Leggendario", "🏆", "+30% Guadagni da donazioni e bits", "#FFD700")
    )

    private val vastNicknamePool = listOf(
        // Gamer Tags & Twitch handles
        "ShadowNinja_99", "PixelSniper_X", "CyberViper", "VortexGamer", "GhostRider_IT",
        "Frosty_Boy", "GlitchKing", "NovaStrike", "HyperZod", "BlazeRunner_01",
        "Apex_Drifter", "KhaosKnight", "TitanPulse", "RogueRaven", "ZeroGravity_x",
        "EchoPhantom", "IronClaw92", "SonicWolf", "QuantumBlade", "StormBreaker_IT",
        // YouTuber & Streamer style handles
        "FraxGaming", "Matte_Replay", "DoctorMeme", "TheRealSamu", "ZioGamerTV",
        "AlePlays", "MrStex", "PoldoFan", "Dario_Moccia_Archive", "IlGamerTranquillo",
        "CapitanTwitch", "MemeLord_IT", "TechBro_Review", "SpeedyGamer", "Gabbo_Fan",
        // Realistic usernames
        "marco.rossi99", "chiara_ricci03", "lorenzo_ferri", "giulia_martini", "dave_colombo",
        "edo_santoro", "marta_barbieri", "alessio_conti", "sofia.esposito", "simone_bruno",
        "valentina.leone", "gabriele_russo", "elena_moretti", "federico_costa", "sara_galli",
        "andrea_marini", "giorgia.serra", "luca_ferrari88", "francesca_deluca", "filippo_fontana",
        // Community & Sub style tags
        "SubFedele_01", "ChatLurker_PRO", "DonatorKing", "ZyroVIP_Max", "Mod_InIncognito",
        "Caffettino_Caldo", "NotteFonda_Gamer", "TopTifoso_Replay", "TwitchFlex_Legend", "PizzaLover_99"
    )

    private val realisticItalianNames get() = vastNicknamePool

    private val usedSubscriberNames = mutableSetOf<String>()

    fun getNextUniqueNickname(): String {
        val available = vastNicknamePool.filterNot { usedSubscriberNames.contains(it) }
        val chosen = if (available.isNotEmpty()) available.random() else {
            "${vastNicknamePool.random()}_${Random.nextInt(10, 99)}"
        }
        usedSubscriberNames.add(chosen)
        return chosen
    }

    // Developer Secret Settings (PIN: 200908)
    private val _isDeveloperUnlocked = MutableStateFlow(false)
    val isDeveloperUnlocked: StateFlow<Boolean> = _isDeveloperUnlocked.asStateFlow()

    private val _devPayoutMode = MutableStateFlow(DevPayoutMode.DEFAULT)
    val devPayoutMode: StateFlow<DevPayoutMode> = _devPayoutMode.asStateFlow()

    private val _devCustomSubRate = MutableStateFlow(3.20)
    val devCustomSubRate: StateFlow<Double> = _devCustomSubRate.asStateFlow()

    private val _devDonationRate = MutableStateFlow(DevDonationRate.NATURAL)
    val devDonationRate: StateFlow<DevDonationRate> = _devDonationRate.asStateFlow()

    fun unlockDeveloperMenu(pin: String): Boolean {
        if (pin == "200908") {
            _isDeveloperUnlocked.value = true
            return true
        }
        return false
    }

    fun setDevPayoutMode(mode: DevPayoutMode) {
        _devPayoutMode.value = mode
    }

    fun setDevCustomSubRate(rate: Double) {
        _devCustomSubRate.value = rate.coerceAtLeast(0.50)
    }

    fun setDevDonationRate(rate: DevDonationRate) {
        _devDonationRate.value = rate
    }

    init {
        viewModelScope.launch { preferences.userProfileFlow.collect { _userProfile.value = it } }
        viewModelScope.launch { preferences.themeAccentFlow.collect { _themeAccent.value = it } }
        viewModelScope.launch { preferences.monetizationUnlockedFlow.collect { _monetizationUnlocked.value = it } }
        viewModelScope.launch { preferences.totalSubsFlow.collect { _totalSubsAllTime.value = it } }
        viewModelScope.launch { preferences.totalStreamSecondsFlow.collect { _totalStreamSecondsAllTime.value = it } }
        viewModelScope.launch { preferences.totalEarningsFlow.collect { _totalEarningsAllTime.value = it } }
        viewModelScope.launch { preferences.availableWalletBalanceFlow.collect { _availableWalletBalance.value = it } }
        viewModelScope.launch { preferences.withdrawnTotalFlow.collect { _withdrawnTotal.value = it } }
        viewModelScope.launch { preferences.walletTransactionsFlow.collect { _walletTransactions.value = it } }
        viewModelScope.launch { preferences.customCommandsFlow.collect { _customCommands.value = it } }
        viewModelScope.launch { preferences.savedClipsFlow.collect { _savedClips.value = it } }
        viewModelScope.launch { preferences.gachaUnlockedFlow.collect { _unlockedGachaItemIds.value = it } }
        viewModelScope.launch { preferences.geminiApiKeyFlow.collect { _geminiApiKey.value = it } }
        viewModelScope.launch { preferences.geminiKeyVerifiedFlow.collect { _geminiKeyVerified.value = it } }
        viewModelScope.launch { preferences.geminiAiEnabledFlow.collect { _geminiAiEnabled.value = it } }
        viewModelScope.launch { preferences.firstLiveDoneFlow.collect { _isFirstLiveDone.value = it } }
        viewModelScope.launch { preferences.autoModSeverityFlow.collect { _autoModSeverity.value = it } }
        viewModelScope.launch { preferences.customBlacklistWordsFlow.collect { _customBlacklistWords.value = it } }
        viewModelScope.launch { preferences.platformContractFlow.collect { _currentPlatform.value = it } }
        viewModelScope.launch { preferences.brandReputationFlow.collect { _brandReputation.value = it } }
        viewModelScope.launch {
            preferences.hiredModeratorsFlow.collect { hiredIds ->
                _virtualModerators.value = VirtualModerator.ALL.map { mod ->
                    mod.copy(isHired = hiredIds.contains(mod.id))
                }
            }
        }
        viewModelScope.launch { preferences.subGoalsFlow.collect { _subGoals.value = it } }
        viewModelScope.launch { preferences.channelUsersFlow.collect { _channelUsers.value = it } }
        viewModelScope.launch { preferences.onlyCamModeFlow.collect { _onlyCamMode.value = it } }

        initSocialTweets()
        initVirtualNews()
        initLurkerChannels()
        initFans()

        // Gratitude voice detection
        viewModelScope.launch {
            voiceDetector.gratitudeDetectedEvent.collect { detected ->
                if (detected && _liveState.value.gratitudeBonusActive) {
                    onGratitudeExpressedByStreamer()
                    voiceDetector.consumeGratitudeEvent()
                }
            }
        }
    }

    private fun initSocialTweets() {
        _socialTweets.value = listOf(
            SocialTweet(
                authorName = "TwitchFlex Community IT",
                handle = "@twitchflex_it",
                avatarEmoji = "👑",
                content = "La nuova era dello streaming indipendente è qui! Scoprite i creator in ascesa di oggi 🔥",
                likesCount = 1420,
                retweetsCount = 312,
                timeAgo = "10m fa"
            ),
            SocialTweet(
                authorName = "MemeStreamer_99",
                handle = "@memes_stream",
                avatarEmoji = "😂",
                content = "Quando lo streamer promette vittoria reale e viene laserato nei primi 30 secondi: 💀💀",
                likesCount = 890,
                retweetsCount = 145,
                timeAgo = "25m fa"
            ),
            SocialTweet(
                authorName = "ClipMaster Pro",
                handle = "@clip_master",
                avatarEmoji = "🎬",
                content = "Ho appena clippato la giocata più assurda della serata su TwitchFlex! Date una medaglia a questo creator 🏆",
                likesCount = 650,
                retweetsCount = 89,
                timeAgo = "1h fa"
            )
        )
    }

    private fun initVirtualNews() {
        _virtualNews.value = listOf(
            VirtualNewsArticle(
                newspaperName = "The Daily Streamer",
                headline = "TwitchFlex vs KickFlex: È Guerra Aperta sui Contratti dei Creator!",
                snippet = "KickFlex sferra l'attacco con split al 95% e stipendi fissi. I creator stanno valutando il grande salto.",
                dateAgo = "2 ore fa",
                reputationImpact = +5,
                donationMultiplierBoost = 1.1f,
                tagEmoji = "🔥"
            ),
            VirtualNewsArticle(
                newspaperName = "GamerZone Italia",
                headline = "Il Fenomeno delle Maratone Subathon: Record di Ore Senza Dormire",
                snippet = "Con l'aggiunta di 30 secondi per sub e 10s per ogni euro donato, le maratone continuano a macinare ore e incassi record.",
                dateAgo = "Ieri",
                reputationImpact = +3,
                donationMultiplierBoost = 1.05f,
                tagEmoji = "⏱️"
            ),
            VirtualNewsArticle(
                newspaperName = "StreamLeaks",
                headline = "Brand Deals: GigaFuel e RazeK Aumentano i Budget per i Micro-Streamer",
                snippet = "Le missioni flash in diretta (Pitch di 60s e Codici Sconto) si confermano le più redditizie per chi ha alta reputazione.",
                dateAgo = "2 giorni fa",
                reputationImpact = +8,
                donationMultiplierBoost = 1.15f,
                tagEmoji = "💼"
            )
        )
    }

    private fun initLurkerChannels() {
        _lurkerChannels.value = listOf(
            VirtualLurkerChannel(
                id = "pokemaster_99",
                streamerName = "PokéMaster99",
                avatarEmoji = "⚡",
                category = "Giochi (Pokémon)",
                title = "Shiny Hunting No-Stop! 1000 Pack Opening 🔥",
                viewersCount = 340,
                bio = "Collezionista incallito e maestro Pokémon dal 1999.",
                recentChatMessages = listOf(
                    ChatMessage(username = "AshKetchum", message = "Trovato lo shiny?! 😱", badge = UserBadge.VIEWER),
                    ChatMessage(username = "PikaFan", message = "POG CHAMP! Che fortuna!", badge = UserBadge.SUBSCRIBER),
                    ChatMessage(username = "Red_Trainer", message = "GG per l'apertura!", badge = UserBadge.VIP)
                )
            ),
            VirtualLurkerChannel(
                id = "chefinlive",
                streamerName = "ChefInLive",
                avatarEmoji = "🍳",
                category = "IRL (Cucina)",
                title = "Carbonara Perfetta secondo la tradizione romana! 🍝",
                viewersCount = 185,
                bio = "Cuoco romano in diretta ogni sera tra padelle e segreti culinari.",
                recentChatMessages = listOf(
                    ChatMessage(username = "PastaLover", message = "Niente panna mi raccomando!! 😂", badge = UserBadge.VIEWER),
                    ChatMessage(username = "GuancialeKing", message = "Che profumo da qua!", badge = UserBadge.VIP),
                    ChatMessage(username = "RomeGuy", message = "Giga W per il guanciale croccante!", badge = UserBadge.SUBSCRIBER)
                )
            ),
            VirtualLurkerChannel(
                id = "progamer_x",
                streamerName = "ProGamer_X",
                avatarEmoji = "🎯",
                category = "Giochi (Fortnite / Valorant)",
                title = "Torneo Ranked Mondiale! Road to Top 1 Global 🏆",
                viewersCount = 720,
                bio = "Player competitivo Esports, 240Hz, zero pietà per i nemici.",
                recentChatMessages = listOf(
                    ChatMessage(username = "SniperKing", message = "Che mira assurda ha questo!", badge = UserBadge.VIEWER),
                    ChatMessage(username = "NoScope", message = "Clutch 1v3 pazzesco 💀💀", badge = UserBadge.SUBSCRIBER),
                    ChatMessage(username = "EsportsFan", message = "GG streamer sei il migliore!", badge = UserBadge.VIP)
                )
            ),
            VirtualLurkerChannel(
                id = "chilllofi",
                streamerName = "ChillLoFi_Beats",
                avatarEmoji = "☕",
                category = "Musica & Relax",
                title = "Lo-Fi Beats to Relax / Study / Stream to 24/7 🌙",
                viewersCount = 95,
                bio = "Musica senza copyright per rilassarsi, studiare e fare live.",
                recentChatMessages = listOf(
                    ChatMessage(username = "StudyBuddy", message = "Questa traccia è stupenda... ☕", badge = UserBadge.VIEWER),
                    ChatMessage(username = "NightOwl", message = "Perfetto per rilassarsi stanotte.", badge = UserBadge.VIEWER)
                )
            )
        )
    }

    private fun initFans() {
        _fanUsers.value = emptyMap()
    }

    fun registerOrLogin(channelName: String, pin: String, birthDate: String, avatarEmoji: String, bio: String) {
        viewModelScope.launch {
            preferences.saveRegistration(channelName, pin, birthDate, avatarEmoji, bio)
        }
    }

    fun setThemeAccent(accent: ThemeAccent) {
        viewModelScope.launch { preferences.saveThemeAccent(accent) }
    }

    fun updateStreamTitle(title: String) {
        _liveState.value = _liveState.value.copy(streamTitle = title)
    }

    fun updateOfflineBanner(banner: String) {
        viewModelScope.launch { preferences.updateOfflineBanner(banner) }
    }

    // ==========================================
    // PANNELLO GESTIONE CANALE & HUB CREATOR
    // ==========================================

    fun updateCreatorProfile(
        name: String,
        handle: String,
        bio: String,
        avatarEmoji: String,
        customBadgeEmoji: String,
        profileImageUri: String?
    ) {
        viewModelScope.launch {
            preferences.updateUserProfile(
                channelName = name,
                handle = handle,
                bio = bio,
                avatarEmoji = avatarEmoji,
                customBadgeEmoji = customBadgeEmoji,
                profileImageUri = profileImageUri
            )
        }
    }

    fun addGoal(title: String, target: Int, initialAmount: Int = 0, type: SubGoalType = SubGoalType.NEW_SUBS) {
        val current = _subGoals.value.toMutableList()
        val newGoal = SubGoal(
            id = System.currentTimeMillis().toString(),
            title = title,
            currentAmount = initialAmount,
            targetAmount = target,
            isActive = current.none { it.isActive },
            type = type
        )
        current.add(newGoal)
        _subGoals.value = current
        viewModelScope.launch { preferences.saveSubGoals(current) }
    }

    fun removeGoal(goalId: String) {
        val current = _subGoals.value.filter { it.id != goalId }
        _subGoals.value = current
        viewModelScope.launch { preferences.saveSubGoals(current) }
    }

    fun toggleGoalActive(goalId: String) {
        val current = _subGoals.value.map { g ->
            if (g.id == goalId) g.copy(isActive = !g.isActive)
            else g.copy(isActive = false)
        }
        _subGoals.value = current
        viewModelScope.launch { preferences.saveSubGoals(current) }
    }

    fun updateGoalProgress(goalId: String, delta: Int) {
        val current = _subGoals.value.map { g ->
            if (g.id == goalId) g.copy(currentAmount = (g.currentAmount + delta).coerceAtLeast(0))
            else g
        }
        _subGoals.value = current
        viewModelScope.launch { preferences.saveSubGoals(current) }
    }

    fun toggleUserVip(userId: String) {
        val current = _channelUsers.value.map { u ->
            if (u.id == userId) u.copy(isVip = !u.isVip) else u
        }
        _channelUsers.value = current
        viewModelScope.launch { preferences.saveChannelUsers(current) }
    }

    fun toggleUserMod(userId: String) {
        val current = _channelUsers.value.map { u ->
            if (u.id == userId) u.copy(isMod = !u.isMod) else u
        }
        _channelUsers.value = current
        viewModelScope.launch { preferences.saveChannelUsers(current) }
    }

    fun deleteChannelUser(userId: String) {
        val current = _channelUsers.value.filterNot { it.id == userId }
        _channelUsers.value = current
        viewModelScope.launch { preferences.saveChannelUsers(current) }
    }

    fun deleteDaySubscriber(subscriberName: String) {
        _daySubscribers.value = _daySubscribers.value.filterNot { it.fullName.equals(subscriberName, ignoreCase = true) }
    }

    fun simulateSubMessage(user: ChannelUser) {
        val tier = when (user.subTier) {
            2 -> SubTier.TIER_2
            3 -> SubTier.TIER_3
            else -> SubTier.TIER_1
        }
        registerNewSub(user.username, tier, "Sub rinnovata per il ${user.monthsSubbed + 1}° mese consecutivo! 🎉")
    }

    // ==========================================
    // 1. CONTRATTI DI ESCLUSIVA & PIATTAFORME RIVALI
    // ==========================================

    fun signPlatformContract(targetPlatform: PlatformId, onResult: (Boolean, String) -> Unit) {
        if (_currentPlatform.value == targetPlatform) {
            onResult(false, "Hai già un contratto attivo con ${targetPlatform.platformName}!")
            return
        }

        viewModelScope.launch {
            val oldPlatform = _currentPlatform.value
            val penalty = oldPlatform.cancellationPenalty

            if (penalty > 0.0 && _availableWalletBalance.value < penalty) {
                onResult(false, "Saldo insufficiente per la penale di rescissione (${penalty} € da versare a ${oldPlatform.platformName})!")
                return@launch
            }

            if (penalty > 0.0) {
                preferences.addTransactionLog(
                    type = TransactionType.PLATFORM_STIPEND,
                    amount = -penalty,
                    desc = "Penale rescissione anticipata con ${oldPlatform.platformName}",
                    dest = "${oldPlatform.platformName} Headquarters"
                )
            }

            preferences.setPlatformContract(targetPlatform)
            _currentPlatform.value = targetPlatform

            // Add breaking news article
            val breakingNews = VirtualNewsArticle(
                newspaperName = "StreamLeaks Breaking",
                headline = "BOMBA: Lo Streamer Firma per ${targetPlatform.platformName}!",
                snippet = "Passaggio epocale da ${oldPlatform.platformName} a ${targetPlatform.platformName} con split al ${targetPlatform.subSplitPercent}% e nuove regole!",
                dateAgo = "Poco fa",
                reputationImpact = +15,
                donationMultiplierBoost = 1.25f,
                tagEmoji = targetPlatform.logoEmoji
            )
            _virtualNews.value = listOf(breakingNews) + _virtualNews.value

            onResult(true, "Contratto firmato con successo con ${targetPlatform.platformName}! Split sub al ${targetPlatform.subSplitPercent}%.")
        }
    }

    // ==========================================
    // 2. BRAND DEALS & MISSIONI SPONSOR FLASH
    // ==========================================

    fun triggerRandomSponsorMission() {
        if (!_liveState.value.isLive || _liveState.value.activeSponsorMission != null) return

        val brands = BrandId.values()
        val randomBrand = brands.random()
        val missionTypes = SponsorMissionType.values()
        val randomType = missionTypes.random()

        val reward = when (randomType) {
            SponsorMissionType.PITCH_60S -> 45.00
            SponsorMissionType.SHOW_PRODUCT_30S -> 35.00
            SponsorMissionType.DISCOUNT_CODE -> 50.00
            SponsorMissionType.BRAND_SAFE_SILENCE -> 65.00
        }

        val totalSec = when (randomType) {
            SponsorMissionType.PITCH_60S -> 60
            SponsorMissionType.SHOW_PRODUCT_30S -> 30
            SponsorMissionType.DISCOUNT_CODE -> 120
            SponsorMissionType.BRAND_SAFE_SILENCE -> 300
        }

        val mission = ActiveSponsorMission(
            brand = randomBrand,
            type = randomType,
            title = randomType.title,
            description = "${randomBrand.brandName}: ${randomType.shortDesc}",
            rewardEuros = reward,
            durationSecondsTotal = totalSec,
            secondsRemaining = totalSec
        )

        _liveState.value = _liveState.value.copy(activeSponsorMission = mission)

        addRawChatMessage(
            ChatMessage(
                username = "Contratto Flash Sponsor",
                message = "💼 Nuovo accordo con ${randomBrand.brandName} (${randomBrand.emoji}): ${mission.title}! Ricompensa: +${"%.2f".format(reward)} €",
                badge = UserBadge.BROADCASTER,
                colorHex = randomBrand.colorHex,
                isSystem = true
            )
        )
    }

    fun completeSponsorMission() {
        val mission = _liveState.value.activeSponsorMission ?: return
        val reward = mission.rewardEuros

        _liveState.value = _liveState.value.copy(
            activeSponsorMission = null,
            sessionSponsorEarnings = _liveState.value.sessionSponsorEarnings + reward
        )

        viewModelScope.launch {
            preferences.updateBrandReputation(+10)
            preferences.addTransactionLog(
                type = TransactionType.SPONSOR_REWARD,
                amount = reward,
                desc = "Compenso completamento missione sponsor ${mission.brand.brandName}",
                dest = "Saldo Canale"
            )
        }

        soundboardManager.play(SoundEffect.AIRHORN)

        // Celebratory chat hype
        val brandName = mission.brand.brandName
        addRawChatMessage(
            ChatMessage(
                username = "Sistema Brand",
                message = "🎉 MISSIONE SPONSOR COMPLETATA! ${mission.brand.brandName} ti ringrazia con +${"%.2f".format(reward)} € sul saldo!",
                badge = UserBadge.BROADCASTER,
                colorHex = mission.brand.colorHex,
                isSystem = true
            )
        )

        listOf(
            "$brandName W 🔥",
            "MIGLIOR SPONSOR DI SEMPRE ⚡",
            "VADO SUBITO A COMPRARE IL PRODOTTO!!",
            "HYPE PER LO SPONSOR 👏"
        ).forEach { chatText ->
            addRawChatMessage(
                ChatMessage(
                    username = realisticItalianNames.random(),
                    message = chatText,
                    badge = UserBadge.REGULAR
                )
            )
        }
    }

    fun failSponsorMission() {
        val mission = _liveState.value.activeSponsorMission ?: return
        _liveState.value = _liveState.value.copy(activeSponsorMission = null)

        viewModelScope.launch { preferences.updateBrandReputation(-12) }

        addRawChatMessage(
            ChatMessage(
                username = "Sistema Brand",
                message = "❌ MISSIONE SPONSOR FALLITA! ${mission.brand.brandName} ha annullato il contratto. Reputazione aziendale calata.",
                badge = UserBadge.BROADCASTER,
                colorHex = "#EB0400",
                isSystem = true
            )
        )
    }

    fun setHoldingProduct(isHolding: Boolean) {
        val cur = _liveState.value.activeSponsorMission ?: return
        if (cur.type == SponsorMissionType.SHOW_PRODUCT_30S) {
            _liveState.value = _liveState.value.copy(
                activeSponsorMission = cur.copy(isHoldingProduct = isHolding)
            )
        }
    }

    // ==========================================
    // 3. SCHERMATA AFK & MINI-GIOCHI IN CHAT
    // ==========================================

    fun toggleAfkMode() {
        val isNowAfk = !_liveState.value.afkState.isAfk
        _liveState.value = _liveState.value.copy(
            afkState = _liveState.value.afkState.copy(
                isAfk = isNowAfk,
                afkSecondsElapsed = 0,
                quizWinner = null,
                lotteryWinner = null
            )
        )

        if (isNowAfk) {
            addRawChatMessage(
                ChatMessage(
                    username = "Sistema",
                    message = "☕ Lo streamer è andato in pausa AFK! Attivato mini-gioco Quiz Trivia e Lotteria in chat!",
                    badge = UserBadge.BROADCASTER,
                    isSystem = true
                )
            )
            startAfkChatLoop()
        } else {
            afkLoopJob?.cancel()
            addRawChatMessage(
                ChatMessage(
                    username = "Sistema",
                    message = "🎉 LO STREAMER È TORNATO! Ripresa la diretta!",
                    badge = UserBadge.BROADCASTER,
                    isSystem = true
                )
            )
        }
    }

    private fun startAfkChatLoop() {
        afkLoopJob?.cancel()
        afkLoopJob = viewModelScope.launch {
            val questions = listOf(
                Pair("Qual è il gioco più trasmesso su TwitchFlex?", "Fortnite"),
                Pair("A quante Sub si sblocca la monetizzazione?", "10"),
                Pair("Qual è l'energy drink sponsor ufficiale?", "GigaFuel"),
                Pair("Quanto dura il timeout dei bot troll?", "30s")
            )
            var qIndex = 0

            while (isActive && _liveState.value.afkState.isAfk) {
                delay(6000)
                val (q, ans) = questions[qIndex % questions.size]
                qIndex++

                addRawChatMessage(
                    ChatMessage(
                        username = "TriviaBot 🤖",
                        message = "QUIZ AFK: $q Chi risponde per primo vince punti fedeltà!",
                        badge = UserBadge.MODERATOR,
                        colorHex = "#00F5D4"
                    )
                )

                delay(3500)
                val winner = realisticItalianNames.random()
                addRawChatMessage(
                    ChatMessage(
                        username = winner,
                        message = "$ans! Risposta esatta! 🚀",
                        badge = UserBadge.REGULAR
                    )
                )

                _liveState.value = _liveState.value.copy(
                    afkState = _liveState.value.afkState.copy(quizWinner = winner)
                )

                delay(4000)
                // Lottery wheel
                val lottoWinner = realisticItalianNames.random()
                addRawChatMessage(
                    ChatMessage(
                        username = "LotteriaBot 🎡",
                        message = "La ruota della lotteria ha estratto: $lottoWinner! Vincitore VIP!",
                        badge = UserBadge.MODERATOR,
                        colorHex = "#FFD700"
                    )
                )
                _liveState.value = _liveState.value.copy(
                    afkState = _liveState.value.afkState.copy(lotteryWinner = lottoWinner)
                )
            }
        }
    }

    // ==========================================
    // 4. SFIDE IN DIRETTA SU DONAZIONE (BOUNTIES)
    // ==========================================

    fun triggerViewerBounty() {
        if (!_liveState.value.isLive || _liveState.value.activeViewerBounty != null) return

        val donor = realisticItalianNames.random()
        val challenges = listOf(
            Pair(5.00, "Fai la voce da robot per 30 secondi 🤖"),
            Pair(8.00, "Rispondi sinceramente alla domanda più imbarazzante della chat ❓"),
            Pair(10.00, "Fai 10 flessioni in live davanti alla cam 💪"),
            Pair(6.50, "Canta il ritornello della tua canzone preferita 🎵")
        )
        val (amount, text) = challenges.random()

        val bounty = ViewerBounty(
            donorName = donor,
            bountyEuros = amount,
            challengeDescription = text,
            durationSeconds = 60,
            secondsLeft = 60
        )

        _liveState.value = _liveState.value.copy(activeViewerBounty = bounty)

        addRawChatMessage(
            ChatMessage(
                username = donor,
                message = "🎁 SFIDA BOUNTY (${amount} €): $text!",
                badge = UserBadge.VIP,
                colorHex = "#FFD700"
            )
        )
        soundboardManager.play(SoundEffect.AIRHORN)
    }

    fun completeViewerBounty() {
        val bounty = _liveState.value.activeViewerBounty ?: return
        val amount = bounty.bountyEuros

        _liveState.value = _liveState.value.copy(
            activeViewerBounty = null,
            sessionBountyEarnings = _liveState.value.sessionBountyEarnings + amount
        )

        viewModelScope.launch {
            preferences.addTransactionLog(
                type = TransactionType.BOUNTY_REWARD,
                amount = amount,
                desc = "Completamento sfida donazione da ${bounty.donorName}: ${bounty.challengeDescription}",
                dest = "Saldo Canale"
            )
        }

        soundboardManager.play(SoundEffect.APPLAUSE)

        addRawChatMessage(
            ChatMessage(
                username = "Sistema",
                message = "✅ SFIDA COMPLETATA! Hai incassato +${"%.2f".format(amount)} € da ${bounty.donorName}!",
                badge = UserBadge.BROADCASTER,
                colorHex = "#00F5D4",
                isSystem = true
            )
        )
    }

    fun failViewerBounty() {
        _liveState.value = _liveState.value.copy(activeViewerBounty = null)
        addRawChatMessage(
            ChatMessage(
                username = "Sistema",
                message = "⏰ Tempo per la sfida scaduto! La donazione non è stata convalidata.",
                badge = UserBadge.BROADCASTER,
                isSystem = true
            )
        )
    }

    fun openGachaCrate(): GachaItem {
        val available = allGachaItems.filter { !_unlockedGachaItemIds.value.contains(it.id) }
        val item = if (available.isNotEmpty()) available.random() else allGachaItems.random()
        viewModelScope.launch {
            preferences.unlockGachaItem(item.id)
        }
        return item
    }

    // ==========================================
    // 5. MODALITÀ MARATONA (SUBATHON SYSTEM)
    // ==========================================

    fun toggleSleepStream() {
        val subState = _liveState.value.subathonState
        val isNowSleep = !subState.isSleepStreamMode
        _liveState.value = _liveState.value.copy(
            subathonState = subState.copy(isSleepStreamMode = isNowSleep)
        )
        addRawChatMessage(
            ChatMessage(
                username = "Sistema Maratona",
                message = if (isNowSleep) "🌙 Sleep Stream attivato! Chat rallentata e musica rilassante. La maratona prosegue!" else "☀️ Sveglia! Lo streamer ha ripreso la maratona a pieno ritmo!",
                badge = UserBadge.BROADCASTER,
                isSystem = true
            )
        )
    }

    fun setSubathonCapHours(hours: Int) {
        val capSec = (hours * 3600L).coerceAtLeast(3600L)
        _liveState.value = _liveState.value.copy(
            subathonState = _liveState.value.subathonState.copy(timerCapSeconds = capSec)
        )
        viewModelScope.launch { preferences.setSubathonCapSeconds(capSec) }
    }

    // ==========================================
    // 6. HARDWARE GLITCHES & TECHNICAL FAULTS
    // ==========================================

    fun triggerHardwareGlitch() {
        if (!_liveState.value.isLive || _liveState.value.activeHardwareGlitch != null) return

        val glitchTypes = GlitchType.values()
        val randomType = glitchTypes.random()

        val glitch = ActiveHardwareGlitch(
            type = randomType,
            tapsCompleted = 0,
            secondsLeft = 15
        )
        _liveState.value = _liveState.value.copy(activeHardwareGlitch = glitch)

        soundboardManager.play(SoundEffect.SAD_HORN)

        addRawChatMessage(
            ChatMessage(
                username = "ALLARME TECNICO",
                message = "⚠️ ${randomType.emoji} ${randomType.title} Risolvi il guasto prima che scada il tempo!",
                badge = UserBadge.BROADCASTER,
                colorHex = "#EB0400",
                isSystem = true
            )
        )
    }

    fun tapFixHardwareGlitch() {
        val glitch = _liveState.value.activeHardwareGlitch ?: return
        val currentTaps = glitch.tapsCompleted + 1
        if (currentTaps >= glitch.type.tapsNeeded) {
            _liveState.value = _liveState.value.copy(activeHardwareGlitch = null)
            soundboardManager.play(SoundEffect.GG)
            addRawChatMessage(
                ChatMessage(
                    username = "Sistema",
                    message = "🛠️ Guasto tecnico ${glitch.type.title} risolto con successo! Spettatori al sicuro!",
                    badge = UserBadge.BROADCASTER,
                    colorHex = "#00F5D4",
                    isSystem = true
                )
            )
        } else {
            _liveState.value = _liveState.value.copy(
                activeHardwareGlitch = glitch.copy(tapsCompleted = currentTaps)
            )
        }
    }

    // ==========================================
    // 7. MERCATO DEI MODERATORI VIRTUALI
    // ==========================================

    fun hireOrFireModerator(modId: String) {
        val mod = _virtualModerators.value.find { it.id == modId } ?: return
        val newHiredState = !mod.isHired

        viewModelScope.launch {
            preferences.toggleHiredModerator(modId, newHiredState)
            _virtualModerators.value = _virtualModerators.value.map {
                if (it.id == modId) it.copy(isHired = newHiredState) else it
            }
        }
    }

    // ==========================================
    // 8. MODALITÀ LURKER & AUTOPROMOZIONE
    // ==========================================

    fun lurkerSendChat(channelId: String, messageText: String) {
        val myName = _userProfile.value.channelName.ifBlank { "Streamer" }
        val ch = _lurkerChannels.value.find { it.id == channelId } ?: return
        val newMsg = ChatMessage(
            username = myName,
            message = messageText,
            badge = UserBadge.VIP,
            colorHex = "#FFD700"
        )
        _lurkerChannels.value = _lurkerChannels.value.map {
            if (it.id == channelId) it.copy(recentChatMessages = it.recentChatMessages + newMsg) else it
        }
    }

    fun lurkerSendDonationOrSub(channelId: String, amountEuros: Double, isGiftSub: Boolean, onResult: (Boolean, String) -> Unit) {
        if (_availableWalletBalance.value < amountEuros) {
            onResult(false, "Saldo insufficiente per donare ${amountEuros} €!")
            return
        }

        viewModelScope.launch {
            val myName = _userProfile.value.channelName.ifBlank { "Streamer" }
            val ch = _lurkerChannels.value.find { it.id == channelId } ?: return@launch

            preferences.withdrawFunds(amountEuros, "Auto-promozione canale ${ch.streamerName}")

            // Feedback and simulated audience boost!
            val gainedFollowers = (amountEuros * Random.nextInt(3, 8)).toInt()
            val newSubs = if (isGiftSub) (amountEuros / 4.0).toInt().coerceAtLeast(1) else 0

            if (newSubs > 0) {
                preferences.addSubsAndEarnings(newSubs, 0.0, 0.0, 0.0)
            }

            val shoutoutMsg = ChatMessage(
                username = ch.streamerName,
                message = "👑 WOOOW!! Grazie mille allo streamer $myName per ${if (isGiftSub) "le $newSubs sub regalo!" else "la super donazione di ${amountEuros}€!"} Andate tutti a seguirlo!",
                badge = UserBadge.BROADCASTER,
                colorHex = "#00F5D4"
            )

            _lurkerChannels.value = _lurkerChannels.value.map {
                if (it.id == channelId) it.copy(recentChatMessages = it.recentChatMessages + shoutoutMsg) else it
            }

            onResult(true, "Auto-promozione riuscita! Hai guadagnato +$gainedFollowers follower e +$newSubs sub per il tuo canale!")
        }
    }

    // ==========================================
    // GEMINI KEY VERIFICATION & TOGGLE
    // ==========================================

    fun verifyAndSaveGeminiKey(key: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val (success, message) = geminiService.verifyApiKey(key)
            if (success) {
                preferences.saveGeminiApiKey(key, true)
                preferences.setGeminiAiEnabled(true)
            } else {
                preferences.saveGeminiApiKey(key, false)
            }
            onResult(success, message)
        }
    }

    fun toggleGeminiAiEnabled(enabled: Boolean) {
        if (_geminiKeyVerified.value) {
            viewModelScope.launch { preferences.setGeminiAiEnabled(enabled) }
        }
    }

    // ==========================================
    // AUTOMOD CONFIGURATION
    // ==========================================

    fun setAutoModSeverity(severity: AutoModSeverity) {
        viewModelScope.launch { preferences.setAutoModSeverity(severity) }
    }

    fun addCustomBlacklistWord(word: String) {
        viewModelScope.launch { preferences.addCustomBlacklistWord(word) }
    }

    fun removeCustomBlacklistWord(word: String) {
        viewModelScope.launch { preferences.removeCustomBlacklistWord(word) }
    }

    // ==========================================
    // WALLET & FUNDS WITHDRAWAL
    // ==========================================

    fun withdrawFunds(amount: Double, destination: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = preferences.withdrawFunds(amount, destination)
            onResult(ok)
        }
    }

    // ==========================================
    // STREAM LIFECYCLE
    // ==========================================

    fun startStream() {
        if (_liveState.value.isLive) return

        val myIp = LocalStreamServer.getLocalIpAddress()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = currentHour >= 0 && currentHour < 6
        val isFirst = !_isFirstLiveDone.value

        _postStreamSummary.value = null

        _liveState.value = _liveState.value.copy(
            isLive = true,
            durationSeconds = 0L,
            viewersCount = if (isFirst) 3 else Random.nextInt(2, 6),
            peakViewers = if (isFirst) 4 else 5,
            isNightStream = isNight,
            sessionSubEarnings = 0.0,
            sessionDonationEarnings = 0.0,
            sessionHourlyEarnings = 0.0,
            sessionSponsorEarnings = 0.0,
            sessionBountyEarnings = 0.0,
            sessionSubsCount = 0,
            subGoalCurrent = 0,
            localIpAddress = myIp,
            localServerRunning = true,
            chatMessages = listOf(
                ChatMessage(
                    username = "Sistema Zyro",
                    message = "🔴 Diretta avviata! ${if (isFirst) "🌱 Benvenuto alla tua prima live!" else if (isNight) "🌙 STREAM NOTTURNO ATTIVO!" else "Buona live!"}",
                    badge = UserBadge.BROADCASTER,
                    isSystem = true
                )
            ),
            subathonState = SubathonState(isEnabled = _liveState.value.currentCategory.type == CategoryType.MARATONA)
        )

        _viewerHistoryPoints.value = listOf(0f)

        voiceDetector.startListening(viewModelScope)
        startLocalServer()

        streamDiscovery.startAnnouncing(viewModelScope) {
            LocalStreamServer.StreamInfoData(
                channelName = _userProfile.value.channelName.ifBlank { "Streamer" },
                category = _liveState.value.currentCategory.name,
                viewers = _liveState.value.viewersCount,
                totalSubs = _totalSubsAllTime.value + _liveState.value.sessionSubsCount,
                durationFormatted = formatDuration(_liveState.value.durationSeconds),
                isLive = true
            )
        }

        startStreamTimer()
        startViewersSimulation()
        startAiChatLoop()
        startRandomEventsScheduler()
    }

    fun endStream() {
        if (!_liveState.value.isLive) return

        val seconds = _liveState.value.durationSeconds
        val grossSub = _liveState.value.sessionSubEarnings
        val grossHourly = 0.0 // Penny-per-second eliminato al 100%
        val grossDonation = _liveState.value.sessionDonationEarnings
        val grossSponsor = _liveState.value.sessionSponsorEarnings + _liveState.value.sessionBountyEarnings
        val grossTotal = grossSub + grossHourly + grossDonation + grossSponsor
        val fixedTax = 5.50
        val subsAdded = _liveState.value.sessionSubsCount
        // Twitch/Platform Cut: For every 10 Subs, the platform takes a €10.00 cut (mostrato solo nel post-stream)
        val platformCut = (subsAdded / 10) * 10.0
        val netEarnings = max(0.0, grossTotal - fixedTax - platformCut)
        val peak = _liveState.value.peakViewers
        val catName = _liveState.value.currentCategory.name

        // Build Post-Live Session Summary
        val summary = StreamSessionSummary(
            durationSeconds = seconds,
            peakViewers = peak,
            totalSubs = subsAdded,
            grossSubEarnings = grossSub,
            grossHourlyEarnings = grossHourly,
            grossDonationEarnings = grossDonation,
            grossSponsorEarnings = grossSponsor,
            zyroTax = fixedTax,
            platformCut = platformCut,
            netEarnings = netEarnings,
            categoryName = catName
        )
        _postStreamSummary.value = summary

        // Generate AI coaching feedback via Gemini in background
        viewModelScope.launch {
            val feedback = geminiService.generatePostStreamCoaching(summary, _geminiApiKey.value)
            _postStreamSummary.value = summary.copy(aiCoachFeedback = feedback)
        }

        // Add auto recap tweet
        val channel = _userProfile.value.channelName.ifBlank { "Lo Streamer" }
        val newTweet = SocialTweet(
            authorName = "FanClub $channel",
            handle = "@fan_${channel.lowercase().replace(" ", "_")}",
            avatarEmoji = "⭐",
            content = "Live conclusa da $channel! ${formatDuration(seconds)} di trasmissione con picco di $peak spettatori! Guadagno netto: ${"%.2f".format(netEarnings)} € 🏆",
            likesCount = Random.nextInt(150, 800),
            retweetsCount = Random.nextInt(20, 120),
            timeAgo = "1m fa"
        )
        _socialTweets.value = listOf(newTweet) + _socialTweets.value

        // Update Hype stock index
        val stockBoost = if (peak > 50) +12.4 else +4.1
        val newPoints = _hypeStock.value.points + (_hypeStock.value.currentPrice + stockBoost)
        _hypeStock.value = _hypeStock.value.copy(
            currentPrice = _hypeStock.value.currentPrice + stockBoost,
            changePercent = stockBoost,
            points = newPoints.takeLast(10)
        )

        viewModelScope.launch {
            preferences.addStreamDuration(seconds)
            preferences.addSubsAndEarnings(subsAdded, grossTotal, netEarnings, fixedTax)
            preferences.markFirstLiveDone()
        }

        streamTimerJob?.cancel()
        viewersSimulationJob?.cancel()
        aiChatLoopJob?.cancel()
        voiceComprehensionJob?.cancel()
        localChatFillJob?.cancel()
        randomEventJob?.cancel()
        afkLoopJob?.cancel()
        voiceDetector.stopListening()
        stopLocalServer()
        streamDiscovery.stopAnnouncing()

        _liveState.value = _liveState.value.copy(
            isLive = false,
            localServerRunning = false,
            activeRandomEvent = null,
            activeSponsorMission = null,
            activeViewerBounty = null,
            activeHardwareGlitch = null
        )
    }

    fun dismissPostStreamSummary() {
        _postStreamSummary.value = null
    }

    fun toggleMute() { _liveState.value = _liveState.value.copy(isMuted = !_liveState.value.isMuted) }
    fun toggleCamera() { _liveState.value = _liveState.value.copy(isFrontCamera = !_liveState.value.isFrontCamera) }

    fun setCategory(category: StreamCategory) {
        val oldCat = _liveState.value.currentCategory
        val isMaratona = category.type == CategoryType.MARATONA
        _liveState.value = _liveState.value.copy(
            currentCategory = category,
            subathonState = _liveState.value.subathonState.copy(isEnabled = isMaratona)
        )
        addRawChatMessage(
            ChatMessage(
                username = "Sistema",
                message = "🏷️ Categoria cambiata da ${oldCat.name} a ${category.name} ${if (isMaratona) "⏱️ MODALITÀ MARATONA (SUBATHON) ATTIVATA!" else ""}",
                badge = UserBadge.BROADCASTER,
                isSystem = true
            )
        )
    }

    fun cutAndSaveClip(title: String) {
        val clip = StreamClip(
            title = title,
            categoryName = _liveState.value.currentCategory.name,
            peakViewers = _liveState.value.viewersCount,
            durationFormatted = formatDuration(_liveState.value.durationSeconds)
        )
        viewModelScope.launch {
            preferences.addClip(clip)
            notificationHelper.showNotification("🎬 Clip Salvata!", title)
        }
        addRawChatMessage(
            ChatMessage(
                username = "Sistema Clip",
                message = "🎬 Clip creata: \"$title\" (${clip.durationFormatted})",
                badge = UserBadge.BROADCASTER,
                isSystem = true
            )
        )
    }

    fun sendStreamerChat(text: String) {
        val streamerName = _userProfile.value.channelName.ifBlank { "Streamer" }

        // Check if fulfilling discount code mission
        val mission = _liveState.value.activeSponsorMission
        if (mission != null && mission.type == SponsorMissionType.DISCOUNT_CODE) {
            if (text.contains(mission.discountCode, ignoreCase = true)) {
                completeSponsorMission()
            }
        }

        addChatMessage(
            ChatMessage(
                username = streamerName,
                message = text,
                badge = UserBadge.BROADCASTER,
                colorHex = _themeAccent.value.hex
            )
        )

        // Check custom commands
        _customCommands.value.firstOrNull { it.command.equals(text.trim(), ignoreCase = true) }?.let { cmd ->
            viewModelScope.launch {
                delay(300)
                addChatMessage(
                    ChatMessage(
                        username = "Bot Canale",
                        message = cmd.response,
                        badge = UserBadge.MODERATOR,
                        colorHex = "#00F5D4"
                    )
                )
            }
        }
    }

    fun banHater(messageId: String, username: String) {
        val list = _liveState.value.chatMessages.map {
            if (it.id == messageId) it.copy(isBanned = true, message = "<Messaggio eliminato dal moderatore>") else it
        }
        _liveState.value = _liveState.value.copy(chatMessages = list)

        addRawChatMessage(
            ChatMessage(
                username = "Moderazione",
                message = "🔨 $username è stato BANNATO definitivamente dal canale!",
                badge = UserBadge.MODERATOR,
                colorHex = "#EB0400",
                isSystem = true
            )
        )

        soundboardManager.play(SoundEffect.GG)

        viewModelScope.launch {
            delay(500)
            listOf("W MODS 🛡️", "Giga Chad ban!", "Pulizia chat effettuata!", "L hater 💀").forEach {
                addRawChatMessage(
                    ChatMessage(
                        username = realisticItalianNames.random(),
                        message = it,
                        badge = UserBadge.VIEWER
                    )
                )
            }
        }
    }

    fun timeoutUser(messageId: String, username: String) {
        val list = _liveState.value.chatMessages.map {
            if (it.id == messageId) it.copy(isBanned = true, message = "<Utente silenziato per 30 secondi>") else it
        }
        _liveState.value = _liveState.value.copy(chatMessages = list)

        addRawChatMessage(
            ChatMessage(
                username = "Moderazione",
                message = "⏱️ $username è stato silenziato per 30 secondi per violazione della chat.",
                badge = UserBadge.MODERATOR,
                colorHex = "#FFB100",
                isSystem = true
            )
        )

        viewModelScope.launch {
            delay(400)
            listOf("Raffreddati un attimo bro 😂", "Giusto così mod!", "30 secondi di meditazione 🧘").forEach {
                addRawChatMessage(
                    ChatMessage(
                        username = realisticItalianNames.random(),
                        message = it,
                        badge = UserBadge.VIEWER
                    )
                )
            }
        }
    }

    fun playSoundboardEffect(effect: SoundEffect) {
        soundboardManager.play(effect)
        addRawChatMessage(
            ChatMessage(
                username = "Soundboard",
                message = "${effect.emoji} Ha suonato: ${effect.displayName}!",
                badge = UserBadge.BROADCASTER,
                isSystem = true
            )
        )
    }

    fun resolveActiveRandomEvent() {
        val cur = _liveState.value.activeRandomEvent ?: return
        _liveState.value = _liveState.value.copy(activeRandomEvent = null)
        soundboardManager.play(SoundEffect.GG)
        addRawChatMessage(
            ChatMessage(
                username = "Sistema Eventi",
                message = "✅ Imprevisto superato: ${cur.type.title}! Chat rassicurata!",
                badge = UserBadge.BROADCASTER,
                isSystem = true
            )
        )
    }

    fun updateCameraFrame(jpegBytes: ByteArray) {
        localServer?.broadcastJpegFrame(jpegBytes)
    }

    // ==========================================
    // SIMULATION LOOPS
    // ==========================================

    private fun startStreamTimer() {
        streamTimerJob?.cancel()
        streamTimerJob = viewModelScope.launch {
            while (isActive && _liveState.value.isLive) {
                delay(1000)
                val newSec = _liveState.value.durationSeconds + 1
                val hourlyEarn = MonetizationCalculator.calculateHourlyEarnings(newSec, _liveState.value.viewersCount, _currentPlatform.value)

                var subathonState = _liveState.value.subathonState
                if (subathonState.isEnabled && subathonState.secondsRemaining > 0) {
                    val newSubSec = max(0L, subathonState.secondsRemaining - 1)
                    subathonState = subathonState.copy(secondsRemaining = newSubSec)
                }

                // Check sponsor mission tick
                var mission = _liveState.value.activeSponsorMission
                if (mission != null && !mission.isCompleted && !mission.isFailed) {
                    val remaining = mission.secondsRemaining - 1
                    var progress = mission.progressPercent

                    if (mission.type == SponsorMissionType.PITCH_60S) {
                        if (voiceDetector.isSpeaking.value) {
                            progress = ((mission.durationSecondsTotal - remaining).toFloat() / mission.durationSecondsTotal).coerceIn(0f, 1f)
                        }
                    } else if (mission.type == SponsorMissionType.SHOW_PRODUCT_30S && mission.isHoldingProduct) {
                        progress = ((mission.durationSecondsTotal - remaining).toFloat() / mission.durationSecondsTotal).coerceIn(0f, 1f)
                    }

                    if (remaining <= 0) {
                        if (mission.type == SponsorMissionType.BRAND_SAFE_SILENCE || progress >= 0.8f) {
                            completeSponsorMission()
                            mission = null
                        } else {
                            failSponsorMission()
                            mission = null
                        }
                    } else {
                        mission = mission.copy(secondsRemaining = remaining, progressPercent = progress)
                    }
                }

                // Check viewer bounty tick
                var bounty = _liveState.value.activeViewerBounty
                if (bounty != null && !bounty.isCompleted) {
                    val left = bounty.secondsLeft - 1
                    if (left <= 0) {
                        failViewerBounty()
                        bounty = null
                    } else {
                        bounty = bounty.copy(secondsLeft = left)
                    }
                }

                // Check hardware glitch tick
                var glitch = _liveState.value.activeHardwareGlitch
                if (glitch != null && !glitch.isFixed) {
                    val left = glitch.secondsLeft - 1
                    if (left <= 0) {
                        // Glitch penalty: drop viewers
                        _liveState.value = _liveState.value.copy(
                            viewersCount = max(2, (_liveState.value.viewersCount * 0.7).toInt()),
                            activeHardwareGlitch = null
                        )
                        glitch = null
                        addRawChatMessage(
                            ChatMessage(username = "Sistema", message = "💥 Il guasto tecnico ha causato un calo di spettatori!", badge = UserBadge.BROADCASTER, isSystem = true)
                        )
                    } else {
                        glitch = glitch.copy(secondsLeft = left)
                    }
                }

                _liveState.value = _liveState.value.copy(
                    durationSeconds = newSec,
                    sessionHourlyEarnings = hourlyEarn,
                    subathonState = subathonState,
                    activeSponsorMission = mission,
                    activeViewerBounty = bounty,
                    activeHardwareGlitch = glitch
                )
            }
        }
    }

    private fun startViewersSimulation() {
        viewersSimulationJob?.cancel()
        viewersSimulationJob = viewModelScope.launch {
            while (isActive && _liveState.value.isLive) {
                delay(2000)

                val duration = _liveState.value.durationSeconds
                val current = _liveState.value.viewersCount
                val cat = _liveState.value.currentCategory
                val silenceSec = voiceDetector.silenceDurationSeconds.value
                val isSpeaking = voiceDetector.isSpeaking.value
                val totalSubs = _totalSubsAllTime.value + _liveState.value.sessionSubsCount

                var targetViewers = current

                if (duration <= 300) {
                    // Startup phase: 0 to 5 minutes
                    val progressRatio = duration.toFloat() / 300f
                    val baseRamp = if (totalSubs > 500) {
                        // Ramp-up 500+ subs: starts low and gradually scales to 50-60 max in first 5 min
                        5 + (50 * progressRatio).toInt()
                    } else {
                        2 + (20 * progressRatio).toInt()
                    }
                    val jitter = Random.nextInt(-2, 3)
                    targetViewers = max(2, baseRamp + jitter)
                } else {
                    // Dynamic phase after 5 minutes: Uncapped max viewers (can scale past 50,000+)
                    var base = when (cat.type) {
                        CategoryType.IRL -> {
                            val spike = if (isSpeaking) Random.nextInt(15, 60) else 0
                            30 + (totalSubs / 6) + spike
                        }
                        CategoryType.GIOCHI, CategoryType.TRENDING -> {
                            val subBoost = (totalSubs * 1.6).toInt() // Uncapped!
                            40 + subBoost
                        }
                        CategoryType.EDINATR -> {
                            val subBoost = (totalSubs * 1.8).toInt() // Uncapped special show!
                            50 + subBoost
                        }
                        CategoryType.MECCANICO, CategoryType.LAVORO -> {
                            25 + (totalSubs / 10)
                        }
                        CategoryType.MARATONA -> {
                            45 + (totalSubs / 5)
                        }
                        else -> 25 + (totalSubs / 10)
                    }

                    if (silenceSec > 15) {
                        val penalty = min(base - 5, (silenceSec - 15) * 2)
                        base = max(5, base - penalty)
                    } else if (isSpeaking) {
                        base += Random.nextInt(2, 8)
                    }

                    if (_liveState.value.isNightStream) {
                        base = (base * 1.25).toInt()
                    }

                    if (_liveState.value.gratitudeBonusActive) {
                        base = (base * 1.30).toInt()
                    }

                    val delta = ((base - current) * 0.25).toInt().coerceIn(-25, 25)
                    targetViewers = max(4, current + delta + Random.nextInt(-3, 4))
                }

                val peak = max(_liveState.value.peakViewers, targetViewers)
                _liveState.value = _liveState.value.copy(
                    viewersCount = targetViewers,
                    peakViewers = peak
                )
                val pts = _viewerHistoryPoints.value
                _viewerHistoryPoints.value = (pts + targetViewers.toFloat()).takeLast(40)

                maybeTriggerRandomSub()
            }
        }
    }

    private fun maybeTriggerRandomSub() {
        val cat = _liveState.value.currentCategory
        val viewers = _liveState.value.viewersCount
        val duration = _liveState.value.durationSeconds

        var chance = (viewers * cat.subConversionRate * 0.8).toInt()
        if (_liveState.value.gratitudeBonusActive) chance *= 3

        if (Random.nextInt(1000) < chance) {
            val tier = if (Random.nextInt(100) < 40) SubTier.PRIME else SubTier.TIER_1
            val randomRealName = getNextUniqueNickname()
            registerNewSub(randomRealName, tier, "Sub hype per il canale!")
        }

        // Rebalanced Donation Mechanics:
        // 1. Donations can ONLY start appearing after 4 minutes (240s) of live streaming
        if (duration < 240L) return
        if (_devDonationRate.value == DevDonationRate.DISABLED) return

        val rateThreshold = when (_devDonationRate.value) {
            DevDonationRate.MEDIUM -> max(10, (viewers * 0.25).toInt())
            else -> max(5, (viewers * 0.12).toInt()) // Natural/low frequency
        }

        if (Random.nextInt(1800) < rateThreshold) {
            val donor = getNextUniqueNickname()
            val donationMsgs = listOf(
                "Grande live continua così! 🔥",
                "Un caffè per lo streamer ☕",
                "Sei il numero uno, content fantastico!",
                "Daje tutta per il traguardo!",
                "Hype meritato per la community 💎"
            )
            // Rare Big Donation: Allow a maximum of ONE €50.00 donation per live stream, from distinct user
            if (!_liveState.value.hasRareBigDonationOccurred && Random.nextInt(100) < 6) {
                triggerDonationAlert(donor, 50.0, "Super Donazione Rara! Complimenti per il grande spettacolo! 🚀")
            } else {
                // Standard donation capped at maximum €25.00
                val euroAmount = listOf(2.0, 3.50, 5.0, 10.0, 15.0, 20.0, 25.0).random()
                triggerDonationAlert(donor, euroAmount, donationMsgs.random())
            }
        }
    }

    /**
     * Triggers a real-time donation alert with 5-second in-app visual alert (NO audio TTS).
     */
    fun triggerDonationAlert(donorName: String, eurosAmount: Double, message: String) {
        val duration = _liveState.value.durationSeconds
        // If received via P2P before 4 minutes, ignore
        if (duration < 240L && _liveState.value.isLive) return

        // Standard donation capped at €25.00, rare one-time €50.00 allowed
        val isRareBig = eurosAmount >= 49.0 && !_liveState.value.hasRareBigDonationOccurred
        val euros = if (isRareBig) {
            _liveState.value = _liveState.value.copy(hasRareBigDonationOccurred = true)
            50.0
        } else {
            eurosAmount.coerceAtMost(25.0).coerceAtLeast(1.0)
        }

        val event = DonationEvent(
            donorName = donorName,
            bits = (euros / 0.014).toInt(),
            euros = euros,
            message = message
        )
        val newDonationEarnings = _liveState.value.sessionDonationEarnings + euros
        val newWalletBalance = _availableWalletBalance.value + euros
        _availableWalletBalance.value = newWalletBalance

        val subathon = _liveState.value.subathonState
        val updatedSubathon = if (subathon.isEnabled) {
            val increment = (euros * subathon.euroDonationIncrementSeconds).toLong()
            val boosted = min(subathon.timerCapSeconds, subathon.secondsRemaining + increment)
            subathon.copy(secondsRemaining = boosted)
        } else subathon

        _liveState.value = _liveState.value.copy(
            latestDonationAlert = event,
            sessionDonationEarnings = newDonationEarnings,
            subathonState = updatedSubathon
        )

        // Add donation message in chat (No TTS audio readout as requested)
        addRawChatMessage(
            ChatMessage(
                username = donorName,
                message = "💎 Ha donato +${"%.2f".format(euros)} €: $message",
                badge = UserBadge.VIP,
                colorHex = "#00F5D4"
            )
        )

        // Update active Donation SubGoal if present
        val currentGoals = _subGoals.value
        val hasDonationGoal = currentGoals.any { it.isActive && it.type == SubGoalType.DONATIONS_EURO }
        if (hasDonationGoal) {
            val eurosInt = max(1, euros.toInt())
            val updatedGoals = currentGoals.map { goal ->
                if (goal.isActive && goal.type == SubGoalType.DONATIONS_EURO) {
                    goal.copy(currentAmount = goal.currentAmount + eurosInt)
                } else goal
            }
            _subGoals.value = updatedGoals
            viewModelScope.launch { preferences.saveSubGoals(updatedGoals) }
        }

        // Add donor to Channel Users database
        val currentUsers = _channelUsers.value.toMutableList()
        val existingDonorIdx = currentUsers.indexOfFirst { it.username.equals(donorName, ignoreCase = true) }
        if (existingDonorIdx >= 0) {
            val u = currentUsers[existingDonorIdx]
            currentUsers[existingDonorIdx] = u.copy(isVip = true, chatActivityScore = min(100, u.chatActivityScore + 10))
        } else {
            currentUsers.add(
                0,
                ChannelUser(
                    id = "donor_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
                    username = donorName,
                    isSub = false,
                    subTier = 0,
                    monthsSubbed = 0,
                    isVip = true,
                    isMod = false,
                    renewalDate = "-",
                    chatActivityScore = 95,
                    avatarEmoji = "💎",
                    followedDaysAgo = 0
                )
            )
        }
        _channelUsers.value = currentUsers
        viewModelScope.launch { preferences.saveChannelUsers(currentUsers) }

        // Clear in-app visual alert after 5 seconds
        viewModelScope.launch {
            delay(5000)
            if (_liveState.value.latestDonationAlert?.id == event.id) {
                _liveState.value = _liveState.value.copy(latestDonationAlert = null)
            }
        }
    }

    fun triggerDonationAlert(donorName: String, bits: Int, message: String) {
        val euros = bits * 0.014
        triggerDonationAlert(donorName, euros, message)
    }

    fun registerNewSub(subscriberName: String, tier: SubTier, customMessage: String = "") {
        val totalSubsBefore = _totalSubsAllTime.value + _liveState.value.sessionSubsCount
        val totalSubsAfter = totalSubsBefore + 1

        val payout = when (_devPayoutMode.value) {
            DevPayoutMode.DEFAULT -> MonetizationCalculator.getPayoutPerSub(totalSubsAfter, _currentPlatform.value)
            DevPayoutMode.CUSTOM_RATE -> _devCustomSubRate.value
            DevPayoutMode.PERFORMANCE -> 3.20 + (maxOf(1, _liveState.value.peakViewers) / 100.0)
        }
        val event = SubEvent(
            subscriberName = subscriberName,
            tier = tier,
            message = customMessage,
            payoutAmount = payout
        )

        val newSessionSubs = _liveState.value.sessionSubsCount + 1
        val newSubEarnings = _liveState.value.sessionSubEarnings + payout

        // Subathon timer boost: +30s per sub!
        val subathon = _liveState.value.subathonState
        val updatedSubathon = if (subathon.isEnabled) {
            val boosted = min(subathon.timerCapSeconds, subathon.secondsRemaining + subathon.subIncrementSeconds)
            subathon.copy(secondsRemaining = boosted)
        } else subathon

        // Add to daily subscribers list with realistic names
        val daySub = DaySubscriber(
            fullName = subscriberName,
            tier = tier,
            timeFormatted = "Poco fa",
            payoutAmount = payout
        )
        _daySubscribers.value = listOf(daySub) + _daySubscribers.value

        if (totalSubsAfter >= MonetizationCalculator.UNLOCK_THRESHOLD_SUBS && !_monetizationUnlocked.value) {
            _monetizationUnlocked.value = true
            viewModelScope.launch { preferences.updateMonetizationUnlocked(true) }
            addRawChatMessage(
                ChatMessage(
                    username = "SISTEMA",
                    message = "🎉 COMPLIMENTI! Hai raggiunto 10 Sub! La MONETIZZAZIONE è SBLOCCATA a vita!",
                    badge = UserBadge.BROADCASTER,
                    isSystem = true
                )
            )
        }

        // Update active SubGoal if present
        val currentGoals = _subGoals.value
        val hasSubGoal = currentGoals.any { it.isActive && it.type == SubGoalType.NEW_SUBS }
        if (hasSubGoal) {
            val updatedGoals = currentGoals.map { goal ->
                if (goal.isActive && goal.type == SubGoalType.NEW_SUBS) {
                    goal.copy(currentAmount = goal.currentAmount + 1)
                } else goal
            }
            _subGoals.value = updatedGoals
            viewModelScope.launch { preferences.saveSubGoals(updatedGoals) }
        }

        // Add or update subscriber in Channel Users database
        val currentUsers = _channelUsers.value.toMutableList()
        val existingSubIdx = currentUsers.indexOfFirst { it.username.equals(subscriberName, ignoreCase = true) }
        if (existingSubIdx >= 0) {
            val u = currentUsers[existingSubIdx]
            currentUsers[existingSubIdx] = u.copy(
                isSub = true,
                subTier = tier.ordinal + 1,
                monthsSubbed = u.monthsSubbed + 1,
                renewalDate = "Tra 30 giorni",
                chatActivityScore = min(100, u.chatActivityScore + 15)
            )
        } else {
            currentUsers.add(
                0,
                ChannelUser(
                    id = "sub_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
                    username = subscriberName,
                    isSub = true,
                    subTier = tier.ordinal + 1,
                    monthsSubbed = 1,
                    isVip = false,
                    isMod = false,
                    renewalDate = "Tra 30 giorni",
                    chatActivityScore = Random.nextInt(75, 99),
                    avatarEmoji = "⭐",
                    followedDaysAgo = 0
                )
            )
        }
        _channelUsers.value = currentUsers
        viewModelScope.launch { preferences.saveChannelUsers(currentUsers) }

        _liveState.value = _liveState.value.copy(
            latestSubAlert = event,
            sessionSubsCount = newSessionSubs,
            sessionSubEarnings = newSubEarnings,
            gratitudeBonusActive = true,
            gratitudeSecondsLeft = 600,
            subGoalCurrent = _liveState.value.subGoalCurrent + 1,
            subathonState = updatedSubathon
        )

        addRawChatMessage(
            ChatMessage(
                username = subscriberName,
                message = "⭐ Si è abbonato con ${tier.displayName}! ${customMessage.ifBlank { "Hype per la live!" }}",
                badge = if (tier == SubTier.PRIME) UserBadge.PRIME else UserBadge.SUBSCRIBER,
                colorHex = "#FFD700"
            )
        )

        // Visual in-app alert only (system notification removed per strict requirements)
        viewModelScope.launch { _subAlertFlow.emit(event) }
    }

    fun registerNewFollower(followerName: String) {
        val currentGoals = _subGoals.value
        val hasFollowGoal = currentGoals.any { it.isActive && it.type == SubGoalType.NEW_FOLLOWERS }
        if (hasFollowGoal) {
            val updatedGoals = currentGoals.map { goal ->
                if (goal.isActive && goal.type == SubGoalType.NEW_FOLLOWERS) {
                    goal.copy(currentAmount = goal.currentAmount + 1)
                } else goal
            }
            _subGoals.value = updatedGoals
            viewModelScope.launch { preferences.saveSubGoals(updatedGoals) }
        }

        val currentUsers = _channelUsers.value.toMutableList()
        if (!currentUsers.any { it.username.equals(followerName, ignoreCase = true) }) {
            currentUsers.add(
                0,
                ChannelUser(
                    id = "follow_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
                    username = followerName,
                    isSub = false,
                    subTier = 0,
                    monthsSubbed = 0,
                    isVip = false,
                    isMod = false,
                    renewalDate = "-",
                    chatActivityScore = Random.nextInt(60, 85),
                    avatarEmoji = "👋",
                    followedDaysAgo = 0
                )
            )
            _channelUsers.value = currentUsers
            viewModelScope.launch { preferences.saveChannelUsers(currentUsers) }
        }

        addRawChatMessage(
            ChatMessage(
                username = followerName,
                message = "💜 Ha iniziato a seguire il canale!",
                badge = UserBadge.VIEWER,
                colorHex = "#9146FF"
            )
        )
    }

    private fun onGratitudeExpressedByStreamer() {
        val bonusSubs = Random.nextInt(1, 3)
        addRawChatMessage(
            ChatMessage(
                username = "Sistema",
                message = "✨ Bonus Gratitudine Attivato! I tuoi ringraziamenti hanno convinto +$bonusSubs spettatori a fare Sub!",
                badge = UserBadge.BROADCASTER,
                isSystem = true
            )
        )
        for (i in 0 until bonusSubs) {
            val name = realisticItalianNames.random()
            registerNewSub(name, SubTier.TIER_1, "Grazie a te per l'intrattenimento!")
        }
    }

    private fun startAiChatLoop() {
        aiChatLoopJob?.cancel()
        aiChatLoopJob = viewModelScope.launch {
            while (isActive && _liveState.value.isLive) {
                _liveState.value = _liveState.value.copy(aiCooldownSeconds = 20)
                val cat = _liveState.value.currentCategory.name
                val speech = voiceDetector.recentTranscribedSpeech.value
                val streamer = _userProfile.value.channelName.ifBlank { "Streamer" }
                val viewers = _liveState.value.viewersCount
                val isNight = _liveState.value.isNightStream
                val isFirst = !_isFirstLiveDone.value

                val generatedMessages = geminiService.generateChatReactions(
                    categoryName = cat,
                    transcribedSpeech = speech,
                    streamerName = streamer,
                    viewersCount = viewers,
                    isNightStream = isNight,
                    userApiKey = _geminiApiKey.value,
                    isAiEnabled = _geminiAiEnabled.value,
                    isFirstLive = isFirst,
                    durationSeconds = _liveState.value.durationSeconds
                )

                launch {
                    for (msg in generatedMessages) {
                        delay(Random.nextLong(600, 2500))
                        addChatMessage(msg)
                    }
                }
                delay(if (isFirst) 30000 else 20000)
            }
        }

        // Voice Comprehension & Context Loop:
        // Captures continuous audio transcription from the streamer's voice and passes it to Gemini (gemini-1.5-flash)
        voiceComprehensionJob?.cancel()
        voiceComprehensionJob = viewModelScope.launch {
            var lastProcessed = ""
            voiceDetector.recentTranscribedSpeech.collect { rawSpeech ->
                if (!_liveState.value.isLive) return@collect
                val speech = rawSpeech.trim()
                if (speech.isNotBlank() && speech != lastProcessed && speech.length >= 3) {
                    lastProcessed = speech
                    val cat = _liveState.value.currentCategory.name
                    val streamer = _userProfile.value.channelName.ifBlank { "Streamer" }
                    val viewers = _liveState.value.viewersCount
                    val isNight = _liveState.value.isNightStream
                    val duration = _liveState.value.durationSeconds

                    val reactions = geminiService.generateChatReactions(
                        categoryName = cat,
                        transcribedSpeech = speech,
                        streamerName = streamer,
                        viewersCount = viewers,
                        isNightStream = isNight,
                        userApiKey = _geminiApiKey.value,
                        isAiEnabled = _geminiAiEnabled.value,
                        isFirstLive = !_isFirstLiveDone.value,
                        durationSeconds = duration
                    )
                    for (msg in reactions.take(3)) {
                        delay(Random.nextLong(400, 1600))
                        addChatMessage(msg)
                    }
                }
            }
        }

        localChatFillJob?.cancel()
        localChatFillJob = viewModelScope.launch {
            while (isActive && _liveState.value.isLive) {
                val delayTime = if (!_isFirstLiveDone.value) Random.nextLong(15000, 25000) else Random.nextLong(3000, 7000)
                delay(delayTime)
                val cat = _liveState.value.currentCategory.name
                val fallback = geminiService.generateLocalChat(
                    categoryName = cat,
                    speech = voiceDetector.recentTranscribedSpeech.value,
                    viewersCount = _liveState.value.viewersCount,
                    isNightStream = _liveState.value.isNightStream,
                    durationSeconds = _liveState.value.durationSeconds,
                    streamerName = _userProfile.value.channelName.ifBlank { "Streamer" }
                )
                fallback.firstOrNull()?.let { addChatMessage(it) }
            }
        }
    }

    private fun startRandomEventsScheduler() {
        randomEventJob?.cancel()
        randomEventJob = viewModelScope.launch {
            while (isActive && _liveState.value.isLive) {
                delay(Random.nextLong(45000, 90000))
                if (_liveState.value.activeRandomEvent == null &&
                    _liveState.value.activeSponsorMission == null &&
                    _liveState.value.activeViewerBounty == null &&
                    _liveState.value.activeHardwareGlitch == null
                ) {
                    val roll = Random.nextInt(100)
                    when {
                        roll < 35 -> triggerRandomSponsorMission()
                        roll < 60 -> triggerViewerBounty()
                        roll < 80 -> triggerHardwareGlitch()
                        else -> {
                            val types = RandomEventType.values()
                            val t = types.random()
                            _liveState.value = _liveState.value.copy(
                                activeRandomEvent = ActiveRandomEvent(type = t, secondsRemaining = t.timeLimitSeconds)
                            )
                        }
                    }
                }
            }
        }
    }

    fun addChatMessage(msg: ChatMessage) {
        // 1. VIP / Trusted Mod & Virtual Mod Auto-Defense against haters
        if (msg.isHater) {
            // Check if hired virtual moderators or trusted fans intercept
            val hasActiveVirtualMod = _virtualModerators.value.any { it.isHired && Random.nextInt(100) < it.efficiencyPercent }
            val trustedFan = _fanUsers.value.values.firstOrNull { it.rank == FanRank.VIP || it.rank == FanRank.TRUSTED_MOD }

            if (hasActiveVirtualMod || trustedFan != null) {
                val defenderName = if (hasActiveVirtualMod) {
                    _virtualModerators.value.first { it.isHired }.name
                } else {
                    trustedFan!!.username
                }

                val censoredMsg = msg.copy(
                    message = "<Messaggio eliminato dal moderatore $defenderName>",
                    isBanned = true,
                    isVipModerated = true
                )
                addRawChatMessage(censoredMsg)
                addRawChatMessage(
                    ChatMessage(
                        username = "Moderazione Automatica",
                        message = "🛡️ $defenderName ha neutralizzato un commento hater da ${msg.username}!",
                        badge = UserBadge.MODERATOR,
                        colorHex = "#00F5D4",
                        isSystem = true
                    )
                )
                return
            }
        }

        // 2. Track Fan Loyalty
        val currentFans = _fanUsers.value.toMutableMap()
        val fan = currentFans[msg.username]
        val assignedBadge = if (fan != null) {
            val newScore = fan.loyaltyPoints + 2
            val newRank = when {
                newScore >= 100 -> FanRank.TRUSTED_MOD
                newScore >= 45 -> FanRank.VIP
                newScore >= 15 -> FanRank.REGULAR
                else -> FanRank.VIEWER
            }
            currentFans[msg.username] = fan.copy(loyaltyPoints = newScore, rank = newRank, messagesCount = fan.messagesCount + 1)
            newRank.badge
        } else {
            msg.badge
        }
        _fanUsers.value = currentFans

        // 3. AutoMod Filtering
        val (filteredText, wasCensored) = AutoModFilter.filterMessage(
            text = msg.message,
            severity = _autoModSeverity.value,
            customWords = _customBlacklistWords.value
        )

        val finalMsg = msg.copy(
            message = filteredText,
            isFilteredByAutoMod = wasCensored,
            badge = if (assignedBadge != UserBadge.VIEWER) assignedBadge else msg.badge
        )

        val current = _liveState.value.chatMessages.takeLast(60).toMutableList()
        current.add(finalMsg)
        _liveState.value = _liveState.value.copy(chatMessages = current)
    }

    private fun addRawChatMessage(msg: ChatMessage) {
        val current = _liveState.value.chatMessages.takeLast(60).toMutableList()
        current.add(msg)
        _liveState.value = _liveState.value.copy(chatMessages = current)
    }

    // ==========================================
    // P2P LOCAL SERVER
    // ==========================================

    private fun startLocalServer() {
        stopLocalServer()
        localServer = LocalStreamServer(
            port = 8080,
            onChatReceived = { sender, message ->
                viewModelScope.launch {
                    addChatMessage(
                        ChatMessage(
                            username = sender,
                            message = message,
                            badge = UserBadge.REGULAR,
                            colorHex = "#00F5D4",
                            isLocalP2P = true
                        )
                    )
                    notificationHelper.showNotification("💬 Messaggio P2P", "$sender: $message")
                }
            },
            onSubReceived = { subscriber, tierName ->
                viewModelScope.launch {
                    val tier = if (tierName.contains("Prime", ignoreCase = true)) SubTier.PRIME else SubTier.TIER_1
                    registerNewSub(subscriber, tier, "Sub inviata dalla rete locale!")
                }
            },
            onDonationReceived = { donor, amount, message ->
                viewModelScope.launch {
                    triggerDonationAlert(donor, amount, message)
                }
            }
        ).apply {
            streamDurationSecondsProvider = { _liveState.value.durationSeconds }
            streamerInfoProvider = {
                LocalStreamServer.StreamInfoData(
                    channelName = _userProfile.value.channelName.ifBlank { "Streamer" },
                    category = _liveState.value.currentCategory.name,
                    viewers = _liveState.value.viewersCount,
                    totalSubs = _totalSubsAllTime.value + _liveState.value.sessionSubsCount,
                    durationFormatted = formatDuration(_liveState.value.durationSeconds),
                    isLive = _liveState.value.isLive
                )
            }
            startServer(viewModelScope)
        }
    }

    private fun stopLocalServer() {
        localServer?.stopServer()
        localServer = null
    }

    fun launchRaiseEmailIntent(context: Context) {
        val channel = _userProfile.value.channelName.ifBlank { "Nome Canale" }
        val subs = _totalSubsAllTime.value + _liveState.value.sessionSubsCount
        val totalSec = _totalStreamSecondsAllTime.value + _liveState.value.durationSeconds
        val totalHours = String.format(Locale.US, "%.1f", totalSec / 3600.0)

        val recipient = "b3yeclaude@gmail.com"
        val subject = "Richiesta Aumento Tariffa Oraria Streamer - $channel"
        val body = """
            Ciao Team Zyro / TwitchFlex,
            
            Richiedo una revisione del mio contratto orario per il canale $channel sulla piattaforma ${_currentPlatform.value.platformName}.
            Statistiche attuali: $subs Iscritti, $totalHours Ore Totali Trasmesse.
            
            Attendo vostro riscontro.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try { context.startActivity(Intent.createChooser(intent, "Invia Richiesta Aumento")) } catch (_: Exception) {}
    }

    fun logout() {
        endStream()
        viewModelScope.launch { preferences.logout() }
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
    }

    fun addCustomCommand(command: String, response: String) {
        viewModelScope.launch {
            preferences.addCustomCommand(command, response)
        }
    }

    fun editCustomCommand(oldCommand: String, newCommand: String, response: String) {
        viewModelScope.launch {
            preferences.editCustomCommand(oldCommand, newCommand, response)
        }
    }

    fun deleteCustomCommand(command: String) {
        viewModelScope.launch {
            preferences.deleteCustomCommand(command)
        }
    }

    suspend fun exportSaveJson(): String {
        return preferences.exportSaveJson()
    }

    suspend fun importSaveJson(json: String): Boolean {
        return preferences.importSaveJson(json)
    }

    override fun onCleared() {
        super.onCleared()
        soundboardManager.shutdown()
        endStream()
    }
}
