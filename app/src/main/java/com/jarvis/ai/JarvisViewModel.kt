package com.jarvis.ai
import com.jarvis.ai.AiConfig
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.ai.ai.*
import com.jarvis.ai.brain.*
import com.jarvis.ai.guard.*
import com.jarvis.ai.security.*
import com.jarvis.ai.tools.*
import com.jarvis.ai.voice.Voice
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
data class Msg(val who: String, val text: String)
data class UiState(val stage: Stage = Stage.IDLE, val log: List<Msg> = emptyList(), val pending: ToolCall? = null,
                   val pendingPrompt: String = "", val locked: Boolean = false, val online: Boolean = false,
                   val aiConfigured: Boolean = false, val level: Float = 0f, val detail: String = "")

class JarvisViewModel(app: Application) : AndroidViewModel(app) {
    val store = SecureStore(app); val challenge = SecretChallenge(store)
    private val tools = Tools(app)
    private val _s = MutableStateFlow(UiState(locked = challenge.isConfigured)); val state = _s.asStateFlow()
    private val guard = ActionGuard { _s.value.locked }
    private val history = mutableListOf<Pair<String, String>>()
    val voice = Voice(app, { send(it) }, { e -> _s.update { it.copy(stage = Stage.FAILED, detail = e) } }, { l -> _s.update { it.copy(level = l) } })
    init { refresh() }

    fun refresh() {
        val cfg = AiConfig.load(store)
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val online = cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _s.update { it.copy(online = online, aiConfigured = cfg.url.isNotEmpty()) }
    }
    fun startListening() { _s.update { it.copy(stage = Stage.LISTENING) }; voice.listen() }
    private fun say(t: String) { if (t.isNotBlank()) { _s.update { it.copy(log = it.log + Msg("JARVIS", t)) }; voice.speak(t) } }
    private fun stage(s: Stage, d: String = "") = _s.update { it.copy(stage = s, detail = d) }

    fun send(text: String) {
        if (text.isBlank() || _s.value.locked) return
        refresh(); _s.update { it.copy(log = it.log + Msg("YOU", text)) }; history += "user" to text
        viewModelScope.launch {
            try {
                stage(Stage.UNDERSTANDING)
                val cfg = AiConfig.load(store)
                val provider = if (_s.value.online && cfg.url.isNotEmpty()) HttpAiProvider(cfg) else null
                stage(Stage.PLANNING)
                val plan = Brain(provider).plan(history.takeLast(20))
                if (plan.reply.isNotBlank()) { history += "assistant" to plan.reply; say(plan.reply) }
                val call = plan.call
                if (call == null) { stage(if (plan.reply.contains("OFFLINE")) Stage.FAILED else Stage.DONE); return@launch }
                stage(Stage.CHECKING)
                when (val v = guard.check(call)) {
                    is Verdict.Allow -> execute(call)
                    is Verdict.Confirm -> { _s.update { it.copy(pending = call, pendingPrompt = v.prompt) }; stage(Stage.CHECKING, "Waiting for confirmation") }
                    is Verdict.Deny -> { stage(Stage.FAILED, v.reason); say(v.reason) }
                }
            } catch (e: Exception) { stage(Stage.FAILED, e.message ?: "Unknown error"); say("Action failed: ${e.message}") }
        }
    }
    private fun execute(c: ToolCall) {
        stage(Stage.EXECUTING); val r = tools.run(c); stage(Stage.VERIFYING)
        if (r.ok) { stage(Stage.DONE, r.message); say(r.message) } else { stage(Stage.FAILED, r.message); say("Action failed: ${r.message}") }
    }
    fun confirm(yes: Boolean) {
        val c = _s.value.pending ?: return
        _s.update { it.copy(pending = null, pendingPrompt = "") }
        if (yes) execute(c) else stage(Stage.IDLE, "Cancelled")
    }
    fun unlock(a: String): String = when (val r = challenge.verify(a)) {
        is SecretChallenge.Result.Ok -> { _s.update { it.copy(locked = false) }; say("Identity verified. Welcome back."); "" }
        is SecretChallenge.Result.Fail -> "Authentication failed."
        is SecretChallenge.Result.Locked -> "Too many attempts. Try again in ${r.secs}s."
    }
    fun lockdown() {
        if (challenge.isConfigured) _s.update { it.copy(locked = true, pending = null, log = emptyList()) }
        history.clear()
    }
    override fun onCleared() { voice.release() }
}
