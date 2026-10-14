package com.jarvis.ai

import com.jarvis.ai.security.SecureStore

data class AiConfig(
    val url: String,
    val token: String,
    val model: String,
    val system: String,
    val temp: Double
) {
    companion object {
        fun load(s: SecureStore): AiConfig {
            return AiConfig(
                url = s.get("ai_url"),
                token = s.get("ai_token"),
                model = s.get("ai_model", "default"),
                system = s.get(
                    "ai_system",
                    "You are JARVIS, a concise personal assistant. Reply in the user's language (Hindi/English/Hinglish)."
                ),
                temp = s.get("ai_temp", "0.5").toDoubleOrNull() ?: 0.5
            )
        }
    }
}