package com.jarvis.ai.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.ai.JarvisViewModel
import com.jarvis.ai.UiState
import com.jarvis.ai.brain.Stage

private val Bg = Color(0xFF020807)
private val Panel = Color(0xFF071411)
private val Green = Color(0xFF00FF9D)
private val Cyan = Color(0xFF00E5FF)
private val Purple = Color(0xFFB56BFF)
private val Pink = Color(0xFFFF4DB8)
private val Amber = Color(0xFFFFC400)
private val Muted = Color(0xFF8CA9A4)

@Composable
fun JarvisScreen(vm: JarvisViewModel) {
    val s by vm.state.collectAsState()
    MaterialTheme(colorScheme = darkColorScheme(primary = Green, secondary = Cyan, background = Bg, surface = Panel)) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            if (s.locked) LockView(vm) else Hud(vm, s)
        }
    }
}

@Composable
private fun NeonCard(modifier: Modifier = Modifier, color: Color = Green, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.border(1.dp, color.copy(alpha = .82f), RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp)).background(Brush.verticalGradient(listOf(Panel, Color(0xFF020807))))
            .padding(12.dp), content = content
    )
}

@Composable
fun Orb(level: Float, active: Boolean, alert: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "orb")
    val spin by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "spin")
    val pulse by transition.animateFloat(.92f, 1.06f, infiniteRepeatable(tween(1300), RepeatMode.Reverse), label = "pulse")
    val main = if (alert) Pink else Green
    Canvas(modifier.size(220.dp)) {
        val cx = size.width / 2f; val cy = size.height / 2f
        val radius = size.minDimension * .29f * pulse * (1f + level.coerceIn(0f, 1f) * .12f)
        drawCircle(Brush.radialGradient(listOf(main.copy(alpha = .35f), Cyan.copy(alpha = .12f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(cx, cy), radius = radius * 2.0f), radius * 2f)
        drawCircle(main.copy(alpha = .12f), radius * 1.45f, style = Stroke(2.dp.toPx()))
        drawCircle(Cyan.copy(alpha = .72f), radius * 1.18f, style = Stroke(1.5.dp.toPx()))
        drawCircle(Brush.radialGradient(listOf(Cyan.copy(alpha = .9f), Green.copy(alpha = .28f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(cx, cy), radius = radius), radius)
        drawCircle(main.copy(alpha = if (active) .95f else .55f), radius * .42f)
        for (i in 0..5) {
            val angle = Math.toRadians((spin + i * 60f).toDouble())
            val x = cx + kotlin.math.cos(angle).toFloat() * radius * 1.42f
            val y = cy + kotlin.math.sin(angle).toFloat() * radius * 1.42f
            drawCircle(if (i % 2 == 0) Green else Cyan, 3.dp.toPx(), androidx.compose.ui.geometry.Offset(x, y))
        }
        drawCircle(Cyan.copy(alpha = .7f), radius * 1.6f, style = Stroke(1.dp.toPx()))
    }
}

@Composable
fun Hud(vm: JarvisViewModel, s: UiState) {
    var input by remember { mutableStateOf("") }
    var settings by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("HOME") }
    val mic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) vm.startListening() }
    val latestAssistant = s.log.lastOrNull { it.who == "JARVIS" }?.text ?: "नमस्ते! मैं JARVIS हूँ। आपकी AI सहायक। मैं आपकी क्या मदद कर सकता हूँ?"
    val latestUser = s.log.lastOrNull { it.who == "YOU" }?.text ?: "आज का मौसम कैसा है?"

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF06261B)).border(1.dp, Green, CircleShape), contentAlignment = Alignment.Center) {
                    Text("◉", color = Cyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("JARVIS", color = Green, fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("YOUR AI COMPANION", color = Cyan, fontSize = 10.sp, letterSpacing = 1.4.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("● ${if (s.online) "ONLINE" else "OFFLINE"}", color = if (s.online) Green else Pink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("AI ${if (s.aiConfigured) "READY" else "SETUP"}", color = Muted, fontSize = 9.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            NeonCard(Modifier.fillMaxWidth(), Cyan) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("≋", color = Cyan, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp)); Text("TRANSCRIPT", color = Cyan, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.weight(1f)); Text(s.stage.label.uppercase(), color = Green, fontSize = 9.sp, maxLines = 1)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("JARVIS", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(latestAssistant, color = Color.White, fontSize = 12.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                    }
                    Box(Modifier.width(1.dp).height(62.dp).background(Cyan.copy(alpha = .35f)))
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("YOU", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(latestUser, color = Color.White, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(if (s.stage == Stage.LISTENING) Green else Muted))
                    Spacer(Modifier.width(6.dp)); Text(if (s.stage == Stage.LISTENING) "Listening..." else s.detail.ifBlank { "Ready when you are" }, color = Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(.92f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NavChip("⌂", "HOME", Green, selected == "HOME") { selected = "HOME" }
                    NavChip("●", "CHAT", Cyan, selected == "CHAT") { selected = "CHAT" }
                    NavChip("◎", "MEMORY", Purple, selected == "MEMORY") { selected = "MEMORY" }
                    NavChip("♨", "SOUL", Pink, selected == "SOUL") { selected = "SOUL" }
                    NavChip("✓", "TASKS", Amber, selected == "TASKS") { selected = "TASKS" }
                    NavChip("⚙", "SETTINGS", Cyan, selected == "SETTINGS") { selected = "SETTINGS"; settings = true }
                }
                Column(Modifier.weight(1.08f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Orb(s.level, s.stage == Stage.LISTENING || s.stage == Stage.EXECUTING, s.stage == Stage.FAILED, Modifier.fillMaxWidth())
                    Text("• JARVIS •", color = Cyan, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("AI ASSISTANT", color = Muted, fontSize = 9.sp, letterSpacing = 2.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = { mic.launch(Manifest.permission.RECORD_AUDIO) }, modifier = Modifier.fillMaxWidth().height(58.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06382B), contentColor = Green), shape = RoundedCornerShape(30.dp)) {
                Text("🎙   TAP TO SPEAK     •     HEY JARVIS", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Type a command in Hindi or English...") }, singleLine = true,
                trailingIcon = { TextButton(onClick = { vm.send(input); input = "" }) { Text("SEND", color = Green, fontWeight = FontWeight.Bold) } },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green, unfocusedBorderColor = Cyan.copy(alpha = .45f), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            if (s.pending != null) {
                Spacer(Modifier.height(8.dp))
                NeonCard(Modifier.fillMaxWidth(), Amber) {
                    Text(s.pendingPrompt, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button({ vm.confirm(true) }) { Text("CONFIRM") }
                        OutlinedButton({ vm.confirm(false) }) { Text("CANCEL") }
                    }
                }
            }
            if (settings) {
                Spacer(Modifier.height(8.dp)); SettingsPanel(vm) { settings = false; if (selected == "SETTINGS") selected = "HOME" }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                NeonCard(Modifier.weight(1f), Cyan) {
                    Text("▤  TODAY HEADLINES", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Headline("🌐", "भारत और दुनिया की ताज़ा खबरें")
                    Headline("📈", "बिज़नेस और टेक अपडेट")
                    Headline("☁", "आज का मौसम")
                    Headline("▶", "ट्रेंडिंग वीडियो")
                }
                NeonCard(Modifier.weight(1f), Green) {
                    Text("☑  TODAY TASKS", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    TaskLine("Desktop version", "75%", true)
                    TaskLine("वीडियो अपलोड करें", "", false)
                    TaskLine("मीटिंग नोट्स बनाएँ", "", false)
                    TaskLine("YouTube पर खोजें", "", false)
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        Row(Modifier.fillMaxWidth().background(Color(0xFF030C09)).border(width = 1.dp, color = Green.copy(alpha = .35f)).padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            BottomItem("⌂", "Home", selected == "HOME") { selected = "HOME" }
            BottomItem("☏", "Chat", selected == "CHAT") { selected = "CHAT" }
            BottomItem("◎", "Memory", selected == "MEMORY") { selected = "MEMORY" }
            BottomItem("♨", "Soul", selected == "SOUL") { selected = "SOUL" }
            BottomItem("✓", "Tasks", selected == "TASKS") { selected = "TASKS" }
            BottomItem("⚙", "Settings", selected == "SETTINGS") { selected = "SETTINGS"; settings = true }
        }
    }
}

@Composable
private fun NavChip(icon: String, label: String, tint: Color, active: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(43.dp).clip(RoundedCornerShape(24.dp)).background(if (active) tint.copy(alpha = .13f) else Color.Transparent).border(1.dp, tint.copy(alpha = if (active) 1f else .65f), RoundedCornerShape(24.dp)).clickable(onClick = onClick).padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, color = tint, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp)); Text(label, color = if (active) tint else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun Headline(icon: String, text: String) {
    Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
        Text(icon, fontSize = 14.sp); Spacer(Modifier.width(6.dp))
        Text(text, color = Color.White, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TaskLine(title: String, progress: String, done: Boolean) {
    Row(Modifier.padding(bottom = 9.dp), verticalAlignment = Alignment.Top) {
        Text(if (done) "●" else "○", color = if (done) Green else Cyan, fontSize = 13.sp)
        Spacer(Modifier.width(6.dp)); Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 10.sp, lineHeight = 13.sp)
            if (progress.isNotBlank()) {
                Spacer(Modifier.height(4.dp)); Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color(0xFF173A31))) { Box(Modifier.fillMaxWidth(.75f).fillMaxHeight().clip(CircleShape).background(Green)) }
                Text(progress, color = Cyan, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun BottomItem(icon: String, label: String, active: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick).padding(horizontal = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, color = if (active) Green else Cyan, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(label, color = if (active) Green else Muted, fontSize = 9.sp)
    }
}

@Composable
fun SettingsPanel(vm: JarvisViewModel, close: () -> Unit) {
    val st = vm.store
    var url by remember { mutableStateOf(st.get("ai_url")) }; var tok by remember { mutableStateOf("") }
    var model by remember { mutableStateOf(st.get("ai_model", "default")) }
    var q by remember { mutableStateOf("") }; var a by remember { mutableStateOf("") }
    NeonCard(Modifier.fillMaxWidth(), Green) {
        Text("AI SETTINGS", color = Green, fontWeight = FontWeight.Bold)
        OutlinedTextField(url, { url = it }, label = { Text("AI endpoint (https)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(tok, { tok = it }, label = { Text("API token (blank = keep)") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(model, { model = it }, label = { Text("Model") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(q, { q = it }, label = { Text("Secret question (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(a, { a = it }, label = { Text("Secret answer") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button({ st.put("ai_url", url.trim()); st.put("ai_model", model.trim()); if (tok.isNotBlank()) st.put("ai_token", tok.trim()); if (q.isNotBlank() && a.isNotBlank()) vm.challenge.setup(q.trim(), a); vm.refresh(); close() }) { Text("SAVE") }
            OutlinedButton(onClick = close) { Text("CLOSE") }
        }
    }
}

@Composable
fun LockView(vm: JarvisViewModel) {
    var answer by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(Bg).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("JARVIS SECURITY", color = Green, fontSize = 24.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp)); Text("SECURITY LOCKDOWN", color = Pink, fontSize = 15.sp)
        Spacer(Modifier.height(12.dp)); Text(vm.challenge.question, color = Color.White)
        OutlinedTextField(answer, { answer = it }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(12.dp)); Button({ error = vm.unlock(answer); answer = "" }) { Text("VERIFY") }
        if (error.isNotEmpty()) Text(error, color = Pink)
    }
}
