package com.jarvis.ai.guard
import com.jarvis.ai.tools.ToolCall
enum class Risk { LOW, MEDIUM, HIGH, BLOCKED }
sealed class Verdict {
    object Allow : Verdict()
    data class Confirm(val prompt: String) : Verdict()
    data class Deny(val reason: String) : Verdict()
}
/** Single choke point: no tool runs without a verdict from here. Default-deny. */
class ActionGuard(private val locked: () -> Boolean) {
    private val risk = mapOf("open_app" to Risk.LOW, "timer" to Risk.LOW, "alarm" to Risk.LOW, "dial" to Risk.MEDIUM)
    private val financial = Regex("pay|upi|bank|transfer|wallet|otp|cvv|pin|password|card", RegexOption.IGNORE_CASE)
    private val protectedApp = Regex(
        "\b(bank|banking|upi|phonepe|google\s?pay|gpay|paytm|bhim|amazon\s?pay|cred|mobikwik|payzapp|yono|imobile|pockets|axis\s?mobile|hdfc|icici|sbi\s?yono|kotak|indusind|canara|bob\s?world|federal\s?bank|wallet)\b",
        RegexOption.IGNORE_CASE
    )
    fun classify(c: ToolCall): Risk {
        if (financial.containsMatchIn(c.tool)) return Risk.BLOCKED
        // Prevent opening known banking/UPI apps through the generic open_app tool.
        if (c.tool.equals("open_app", true) && protectedApp.containsMatchIn(c.args["name"].orEmpty())) return Risk.BLOCKED
        return risk[c.tool] ?: Risk.BLOCKED
    }
    fun check(c: ToolCall): Verdict {
        if (locked()) return Verdict.Deny("JARVIS is locked. Please authenticate first.")
        return when (classify(c)) {
            Risk.LOW -> Verdict.Allow
            Risk.MEDIUM -> Verdict.Confirm("Confirm: ${c.tool} ${c.args.values.joinToString()}?")
            Risk.HIGH -> Verdict.Confirm("High-risk action. Confirm: ${c.tool}?")
            Risk.BLOCKED -> Verdict.Deny("Blocked by Action Guard (unknown or financial action).")
        }
    }
}
