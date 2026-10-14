package com.jarvis.ai.ai

import com.jarvis.ai.tools.ToolCall

interface AiProvider {
    suspend fun chat(history: List<Pair<String, String>>): AiReply
}

data class AiReply(
    val text: String,
    val action: ToolCall? = null
)