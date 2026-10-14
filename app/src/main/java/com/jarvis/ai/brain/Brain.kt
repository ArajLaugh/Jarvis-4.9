package com.jarvis.ai.brain
import com.jarvis.ai.ai.AiProvider
import com.jarvis.ai.tools.ToolCall

enum class Stage(val label: String) {
    IDLE("READY FOR COMMAND"), LISTENING("LISTENING..."), UNDERSTANDING("UNDERSTANDING..."), PLANNING("PLANNING..."),
    CHECKING("CHECKING PERMISSION..."), EXECUTING("EXECUTING..."), VERIFYING("VERIFYING..."),
    DONE("TASK COMPLETED"), FAILED("ACTION FAILED")
}
data class Plan(val reply: String, val call: ToolCall?)

class Brain(private val provider: AiProvider?) {
    /** Cloud plan when a provider exists, otherwise simple local commands (offline mode). */
    suspend fun plan(history: List<Pair<String, String>>): Plan {
        if (provider == null) return localPlan(history.last().second)
        val r = provider.chat(history); return Plan(r.text, r.action)
    }
    fun localPlan(t: String): Plan {
        Regex("(?:open|kholo|khol)\\s+(.+)", RegexOption.IGNORE_CASE).find(t)?.let {
            return Plan("", ToolCall("open_app", mapOf("name" to it.groupValues[1].trim())))
        }
        Regex("timer.*?(\\d+)\\s*(min|sec|minute|second)?", RegexOption.IGNORE_CASE).find(t)?.let {
            val n = it.groupValues[1].toInt(); val s = if (it.groupValues[2].startsWith("s", true)) n else n * 60
            return Plan("", ToolCall("timer", mapOf("seconds" to s.toString())))
        }
        return Plan("JARVIS AI OFFLINE: only 'open <app>' and 'timer <n> min' work without the cloud AI.", null)
    }
}
