package com.example.data

import com.example.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

/**
 * Repository powered by Google's Generative AI SDK (gemini-1.5-flash)
 * to generate dynamic, context-aware live chat interactions based on the streamer's voice.
 */
class GeminiChatRepository {

    suspend fun verifyApiKey(apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Pair(false, "La chiave API non può essere vuota.")
        try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )
            val response = model.generateContent("Rispondi solo OK")
            if (!response.text.isNullOrBlank()) {
                Pair(true, "Chiave API Gemini verificata con successo!")
            } else {
                Pair(false, "Risposta vuota dall'API Gemini.")
            }
        } catch (e: Exception) {
            Pair(false, "Errore di verifica: ${e.localizedMessage ?: "Verifica la tua connessione o chiave"}")
        }
    }

    suspend fun generateChatReactions(
        categoryName: String,
        transcribedSpeech: String,
        streamerName: String,
        viewersCount: Int,
        durationSeconds: Long,
        isNightStream: Boolean = false,
        userApiKey: String? = null
    ): List<ChatMessage> = withContext(Dispatchers.IO) {
        val effectiveApiKey = when {
            !userApiKey.isNullOrBlank() -> userApiKey.trim()
            try { BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" } catch (_: Exception) { false } -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (effectiveApiKey.isBlank()) {
            return@withContext emptyList()
        }

        try {
            val isFirstTwoMinutes = durationSeconds <= 120L

            val prompt = """
                Sei la chat dal vivo in tempo reale di TwitchFlex per lo streamer '$streamerName'.
                Categoria corrente: '$categoryName'.
                Secondi di trasmissione: $durationSeconds sec.
                Fascia oraria: ${if (isNightStream) "STREAM NOTTURNO" else "STREAM DIURNO"}.
                Cosa sta dicendo lo streamer a voce al microfono in questo momento:
                "${transcribedSpeech.ifBlank { "(sta trasmettendo concentrato in diretta)" }}"

                REGOLE DI VOCABOLARIO E FLUSSO (Rigorose):
                ${if (isFirstTwoMinutes) """
                - FASE INIZIALE (Primi 2 minuti): La chat fa saluti calorosi e moderni usando slang e domande tipiche:
                  "Yo $streamerName", "My G", "Goat", "Bro", "come stai", "cosa fai oggi?", "ma oggi che giorno è", "bella bro", "daje tutta!".
                """ else """
                - FASE AVANZATA (Dopo 2 minuti): Interazione contestuale intelligente. La chat risponde direttamente a ciò che lo streamer ha detto ("$transcribedSpeech"), commenta le sue battute, fa domande sul gameplay/argomento o reagisce con "KEKW", "POG", "AHAHAH", "W streamer".
                """}
                - ZERO commenti offensivi, hater o tossici. La chat deve essere coinvolgente, vivace, supportiva e autentica.
                - Varia i nickname degli spettatori in modo realistico.

                Rispondi ESCLUSIVAMENTE con un JSON array valido nel seguente formato:
                [
                  {"username": "NicknameUtente", "message": "Testo del messaggio"}
                ]
            """.trimIndent()

            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = effectiveApiKey,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    temperature = if (isNightStream) 0.95f else 0.85f
                }
            )

            val response = model.generateContent(prompt)
            val responseText = response.text.orEmpty()
            parseChatJson(responseText)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun generatePostStreamCoaching(
        summary: StreamSessionSummary,
        userApiKey: String? = null
    ): String = withContext(Dispatchers.IO) {
        val effectiveApiKey = when {
            !userApiKey.isNullOrBlank() -> userApiKey.trim()
            try { BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" } catch (_: Exception) { false } -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (effectiveApiKey.isBlank()) return@withContext ""

        try {
            val prompt = """
                Sei il Coach IA ufficiale di TwitchFlex per creator e streamer.
                Analizza questa sessione appena conclusa:
                - Categoria: ${summary.categoryName}
                - Durata: ${summary.durationSeconds / 60} minuti
                - Picco Spettatori: ${summary.peakViewers}
                - Nuovi Sub: ${summary.totalSubs}
                - Guadagno Netto: ${"%.2f".format(summary.netEarnings)} €

                Fornisci un'analisi strutturata in 3 sezioni con emoji:
                1. 🎯 Punti di Forza
                2. 📉 Aree di Miglioramento
                3. 🚀 3 Consigli Pratici per la Prossima Live
            """.trimIndent()

            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = effectiveApiKey
            )
            val response = model.generateContent(prompt)
            response.text?.trim().orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    private fun parseChatJson(jsonText: String): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        try {
            val clean = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val array = JSONArray(clean)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val user = obj.optString("username", "Spettatore")
                val msg = obj.optString("message", "")
                if (msg.isNotBlank()) {
                    messages.add(
                        ChatMessage(
                            username = user,
                            message = msg,
                            badge = randomBadge(),
                            colorHex = randomTwitchColor()
                        )
                    )
                }
            }
        } catch (_: Exception) {}
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
}
