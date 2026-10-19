package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Service orchestrating Gemini AI chat generation and offline fallback simulations
 * powered by GeminiChatRepository with Google's Generative AI SDK (gemini-1.5-flash).
 */
class GeminiChatService(
    private val repository: GeminiChatRepository = GeminiChatRepository()
) {

    suspend fun verifyApiKey(key: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        repository.verifyApiKey(key)
    }

    suspend fun generateChatReactions(
        categoryName: String,
        transcribedSpeech: String,
        streamerName: String,
        viewersCount: Int,
        isNightStream: Boolean = false,
        userApiKey: String? = null,
        isAiEnabled: Boolean = true,
        isFirstLive: Boolean = false,
        durationSeconds: Long = 0L
    ): List<ChatMessage> = withContext(Dispatchers.IO) {
        // If it's the very first live ever, chat is gentle and quiet
        if (isFirstLive) {
            return@withContext generateFirstLiveChat()
        }

        if (!isAiEnabled) {
            return@withContext generateLocalChat(categoryName, transcribedSpeech, viewersCount, isNightStream, durationSeconds, streamerName)
        }

        val sdkMessages = repository.generateChatReactions(
            categoryName = categoryName,
            transcribedSpeech = transcribedSpeech,
            streamerName = streamerName,
            viewersCount = viewersCount,
            durationSeconds = durationSeconds,
            isNightStream = isNightStream,
            userApiKey = userApiKey
        )

        if (sdkMessages.isNotEmpty()) {
            sdkMessages
        } else {
            // Intelligent local fallback when offline or no API key provided
            generateLocalChat(categoryName, transcribedSpeech, viewersCount, isNightStream, durationSeconds, streamerName)
        }
    }

    suspend fun generatePostStreamCoaching(
        summary: StreamSessionSummary,
        userApiKey: String? = null
    ): String = withContext(Dispatchers.IO) {
        val coachFeedback = repository.generatePostStreamCoaching(summary, userApiKey)
        if (coachFeedback.isNotBlank()) {
            return@withContext coachFeedback
        }

        // Fallback rich AI coaching analysis
        val minutes = summary.durationSeconds / 60
        return@withContext """
            🎯 Punti di Forza:
            Ottima costanza nella categoria ${summary.categoryName}! Hai mantenuto una sessione di $minutes minuti con un picco di ${summary.peakViewers} spettatori simultanei e convertito ${summary.totalSubs} nuovi sub.

            📉 Aree di Miglioramento:
            I dati sulla voce indicano alcune pause prolungate di silenzio: l'algoritmo premia chi commenta costantemente le proprie azioni. Ricorda che la tassa fissa di 5,50€ di Zyro viene assorbita meglio con live di durata superiore a 20-30 minuti.

            🚀 3 Consigli per la Prossima Live:
            1. Poni domande aperte alla chat ogni 3-4 minuti per stimolare l'algoritmo di interazione.
            2. Utilizza la soundboard nei momenti di climax per riattivare l'attenzione degli spettatori silenziosi.
            3. Ringrazia immediatamente per nome ogni nuovo abbonato per attivare il bonus moltiplicatore gratitudine!
        """.trimIndent()
    }

    private fun generateFirstLiveChat(): List<ChatMessage> {
        val calmComments = listOf(
            "Ciao streamer! Prima live?",
            "Audio si sente bene 👍",
            "GG per l'inizio",
            "Buona fortuna con il canale!",
            "Seguo volentieri",
            "Ciao a tutti"
        )
        val user = firstLiveUsernames.random()
        return listOf(
            ChatMessage(
                username = user,
                message = calmComments.random(),
                badge = UserBadge.VIEWER,
                colorHex = randomTwitchColor()
            )
        )
    }

    fun generateLocalChat(
        categoryName: String,
        speech: String,
        viewersCount: Int,
        isNightStream: Boolean = false,
        durationSeconds: Long = 0L,
        streamerName: String = "Streamer"
    ): List<ChatMessage> {
        val lowerSpeech = speech.lowercase()
        val messages = mutableListOf<ChatMessage>()

        val isFirstTwoMinutes = durationSeconds in 0L..120L

        // Modern opening greetings for the first 2 minutes
        val openingGreetings = listOf(
            "Yo $streamerName",
            "My G come stai?",
            "Goat mi saluti? Sei il migliore!",
            "Bro finalmente sei in live!",
            "come stai",
            "cosa fai oggi?",
            "ma oggi che giorno è",
            "Yo bomber!",
            "My G sempre presente 🔥",
            "Goat ti ricordi di me?",
            "Bro carica questa live!",
            "daje grande live!",
            "eccomiii!"
        )

        // Contextual dynamic interactions for after 2 minutes
        val postTwoMinutesInteractions = listOf(
            "ieri non c'ero che cosa avete fatto",
            "cosa facciamo oggi",
            "come stai",
            "cosa dici 🤣",
            "ma oggi che giorno è",
            "Sono appena entrato mi spieghi?",
            "di cosa stai parlando?",
            "ahahah non ci credo 💀",
            "W streamer continua così 🔥",
            "My G hai ragione al 100%",
            "Goat che giocata!"
        )

        val isQuestion = lowerSpeech.contains("come state") ||
                lowerSpeech.contains("tutto bene") ||
                lowerSpeech.contains("che dite") ||
                lowerSpeech.contains("mi sentite") ||
                lowerSpeech.contains("cosa ne pensate") ||
                lowerSpeech.contains("che fate")

        val isGratitude = lowerSpeech.contains("grazie") ||
                lowerSpeech.contains("benvenuto") ||
                lowerSpeech.contains("ciao a tutti") ||
                lowerSpeech.contains("saluto")

        val count = Random.nextInt(2, 4)
        for (i in 0 until count) {
            val user = sampleUsernames.random()
            val msg = when {
                isFirstTwoMinutes -> openingGreetings.random()
                isNightStream && Random.nextBoolean() -> nightStreamReactions.random()
                isQuestion && Random.nextBoolean() -> questionAnswers.random()
                isGratitude && Random.nextBoolean() -> gratitudeReactions.random()
                lowerSpeech.isNotBlank() && Random.nextInt(100) < 55 -> postTwoMinutesInteractions.random()
                categoryName.contains("IRL", ignoreCase = true) -> irlReactions.random()
                categoryName.contains("Edinatr", ignoreCase = true) -> listOf("Che format assurdo!", "Edinatr top show!", "Spettacolo stasera!").random()
                categoryName.contains("Meccanico", ignoreCase = true) -> mechanicalReactions.random()
                categoryName.contains("Lavoro", ignoreCase = true) -> workReactions.random()
                categoryName.contains("Maratona", ignoreCase = true) -> marathonReactions.random()
                categoryName.contains("Giochi", ignoreCase = true) || categoryName.contains("Fortnite", ignoreCase = true) -> gameReactions.random()
                else -> generalReactions.random()
            }
            messages.add(
                ChatMessage(
                    username = user,
                    message = msg,
                    badge = randomBadge(),
                    colorHex = randomTwitchColor()
                )
            )
        }
        return messages
    }

    private fun randomBadge(): UserBadge {
        val r = Random.nextInt(100)
        return when {
            r < 8 -> UserBadge.MOD
            r < 18 -> UserBadge.VIP
            r < 40 -> UserBadge.SUB
            r < 55 -> UserBadge.PRIME
            else -> UserBadge.VIEWER
        }
    }

    private fun randomTwitchColor(): String {
        val colors = listOf("#9146FF", "#00F5D4", "#FFB100", "#FF2A6D", "#00B5D8", "#772CE8", "#ADADB8", "#FFFFFF")
        return colors.random()
    }

    private val firstLiveUsernames = listOf("PrimiPassi_01", "Luca_Explorer", "SpettatoreTranquillo", "Gigi_Prime", "Elena_99")

    private val sampleUsernames = listOf(
        "KekMaster_99", "ProGamer_IT", "Matteo_Prime", "Giulia_Sub", "Vincenzo_98",
        "Apex_Predator", "Simo_Fire", "Zio_Gamer", "DarkKnight_77", "Claudio_Boss",
        "StreamLover", "MemeLord_X", "SuperMario_Fan", "Cyber_Ghost", "Ale_Stream",
        "ShadowNinja", "PixelSniper", "VortexGamer", "GhostRider", "Frosty_Boy"
    )

    private val mechanicalReactions = listOf(
        "Che motore stai smontando oggi? 🚗",
        "Occhio a quella guarnizione di testa! 🔧",
        "Bel lavoro con la chiave dinamometrica!",
        "La passione per i motori non batte nessuno 🔥",
        "Quanti cavalli fa dopo la mappa? 🏎️"
    )

    private val workReactions = listOf(
        "Compagnia perfetta durante la giornata lavorativa 💼",
        "Anche io sto lavorando dal pc in smart working!",
        "Che orari fai oggi in live? ☕",
        "Forza che il turno finisce presto 💪",
        "Audio perfetto in sottofondo mentre fatturo!"
    )

    private val marathonReactions = listOf(
        "RESISTI STREAMER! Quante ore mancano alla fine? ⏰",
        "Qualcuno gli faccia un sub per aggiungere 5 minuti! ⭐",
        "Dormi prima o poi o vai avanti 24 ore? 😱",
        "MARATONA LEGGENDA! GG per la resistenza 🔥",
        "Idratati e bevi acqua mi raccomando!"
    )

    private val gameReactions = listOf(
        "GG per la giocata! POG 🔥",
        "Occhio a destra nel mirino! 😱",
        "Che clutch assurda KEKW",
        "Costruisci e prendi l'high ground!",
        "Vittoria reale in arrivo me lo sento 🏆"
    )

    private val irlReactions = listOf(
        "Dove sei di bello? Che panorama!",
        "Salutami il barista/passante! 😂",
        "Attento alla connessione in movimento 📶",
        "La vita reale è il content migliore 🔥"
    )

    private val generalReactions = listOf(
        "GG streamer! 🔥",
        "KEKW 😂",
        "POG CHAMPION 🏆",
        "Hype per questa live!",
        "La chat è prontissima!",
        "W streamer sempre al top 👑"
    )

    private val questionAnswers = listOf(
        "Tutto benissimo capo, tu come stai? 🔥",
        "Alla grande, pronti per la live!",
        "Ti sentiamo forte e chiaro dal mic 🎙️",
        "Presenti e carichi!"
    )

    private val gratitudeReactions = listOf(
        "Meritatissimo streamer! W te ❤️",
        "Grazie a te per l'intrattenimento 🔥",
        "Sub rinnovata con orgoglio! ⭐",
        "Il canale sta volando!"
    )

    private val nightStreamReactions = listOf(
        "Chi è sveglio alle 3 di notte spamma 💀💀",
        "Notturna leggendaria, domani si dorme al lavoro 😂",
        "I veri campioni guardano la live di notte 🌙",
        "Vibe notturne rilassanti al massimo 🔥"
    )
}
