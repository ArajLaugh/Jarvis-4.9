package com.jarvis.ai.tools
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
data class ToolCall(val tool: String, val args: Map<String, String>)
data class ToolResult(val ok: Boolean, val message: String)

/** Tools only use system intents. ok = intent resolved and started (not that the target app finished work). */
class Tools(private val ctx: Context) {
    fun run(c: ToolCall): ToolResult = try {
        when (c.tool) {
            "open_app" -> openApp(c.args["name"].orEmpty())
            "timer" -> launch(Intent(AlarmClock.ACTION_SET_TIMER).putExtra(AlarmClock.EXTRA_LENGTH, c.args["seconds"]?.toInt() ?: 0)
                .putExtra(AlarmClock.EXTRA_SKIP_UI, true), "Timer request sent to the clock app")
            "alarm" -> launch(Intent(AlarmClock.ACTION_SET_ALARM).putExtra(AlarmClock.EXTRA_HOUR, c.args["hour"]?.toInt() ?: 0)
                .putExtra(AlarmClock.EXTRA_MINUTES, c.args["minute"]?.toInt() ?: 0).putExtra(AlarmClock.EXTRA_SKIP_UI, true), "Alarm request sent to the clock app")
            "dial" -> launch(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + c.args["number"].orEmpty())), "Dialer opened (call not placed)")
            else -> ToolResult(false, "Unknown tool: ${c.tool}")
        }
    } catch (e: Exception) { ToolResult(false, e.message ?: "Failed") }

    private fun launch(i: Intent, ok: String): ToolResult {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (i.resolveActivity(ctx.packageManager) == null) return ToolResult(false, "No app on this device can handle this action")
        ctx.startActivity(i); return ToolResult(true, ok)
    }
    private fun openApp(name: String): ToolResult {
        val pm = ctx.packageManager
        val q = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val all = pm.queryIntentActivities(q, 0)
        val hit = all.firstOrNull { it.loadLabel(pm).toString().equals(name, true) }
            ?: all.firstOrNull { it.loadLabel(pm).toString().contains(name, true) }
            ?: return ToolResult(false, "App '$name' not found")
        val i = pm.getLaunchIntentForPackage(hit.activityInfo.packageName) ?: return ToolResult(false, "Cannot launch $name")
        ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return ToolResult(true, "Opened ${hit.loadLabel(pm)}")
    }
}
