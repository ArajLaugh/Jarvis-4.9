package com.jarvis.ai.security
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
/** Keystore-backed storage for API token, endpoint and secret-challenge hash. */
class SecureStore(ctx: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        ctx, "jarvis_secure",
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    fun get(k: String, d: String = "") = prefs.getString(k, d) ?: d
    fun put(k: String, v: String) = prefs.edit().putString(k, v).apply()
    fun getLong(k: String) = prefs.getLong(k, 0L)
    fun putLong(k: String, v: Long) = prefs.edit().putLong(k, v).apply()
}
