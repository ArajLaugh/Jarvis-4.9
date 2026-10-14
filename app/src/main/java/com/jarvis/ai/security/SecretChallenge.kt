package com.jarvis.ai.security
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
/** App-level lock only. Never replaces the Android device lock. */
class SecretChallenge(private val s: SecureStore) {
    sealed class Result { object Ok : Result(); object Fail : Result(); data class Locked(val secs: Long) : Result() }
    val isConfigured get() = s.get("sc_hash").isNotEmpty()
    val question get() = s.get("sc_q")
    private fun hash(a: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(a.trim().lowercase().toCharArray(), salt, 150_000, 256)).encoded
    fun setup(q: String, answer: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        s.put("sc_q", q); s.put("sc_salt", Base64.encodeToString(salt, 0))
        s.put("sc_hash", Base64.encodeToString(hash(answer, salt), 0)); s.putLong("sc_fails", 0)
    }
    fun verify(answer: String): Result {
        val now = System.currentTimeMillis(); val until = s.getLong("sc_until")
        if (now < until) return Result.Locked((until - now) / 1000 + 1)
        val salt = Base64.decode(s.get("sc_salt"), 0)
        val ok = MessageDigest.isEqual(hash(answer, salt), Base64.decode(s.get("sc_hash"), 0))
        if (ok) { s.putLong("sc_fails", 0); return Result.Ok }
        val f = s.getLong("sc_fails") + 1; s.putLong("sc_fails", f)
        if (f >= 5) { s.putLong("sc_until", now + 30_000L * (f - 4)); return Result.Locked(30L * (f - 4)) }
        return Result.Fail
    }
}
