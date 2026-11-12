package ru.alfanomy.opencodechat

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("opencode_chat", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = sp.getString("baseUrl", "https://gmpony.alfanomy.ru") ?: ""
        set(v) = sp.edit().putString("baseUrl", v).apply()

    var user: String
        get() = sp.getString("user", "opencode") ?: ""
        set(v) = sp.edit().putString("user", v).apply()

    var password: String
        get() = sp.getString("password", "") ?: ""
        set(v) = sp.edit().putString("password", v).apply()

    var providerId: String
        get() = sp.getString("providerId", "opencode") ?: "opencode"
        set(v) = sp.edit().putString("providerId", v).apply()

    var modelId: String
        get() = sp.getString("modelId", "deepseek-v4.1-flash") ?: "deepseek-v4.1-flash"
        set(v) = sp.edit().putString("modelId", v).apply()

    var agent: String
        get() = sp.getString("agent", "build") ?: "build"
        set(v) = sp.edit().putString("agent", v).apply()

    var variant: String
        get() = sp.getString("variant", "") ?: ""
        set(v) = sp.edit().putString("variant", v).apply()

    var lastSessionId: String
        get() = sp.getString("lastSessionId", "") ?: ""
        set(v) = sp.edit().putString("lastSessionId", v).apply()

    var lastSessionTitle: String
        get() = sp.getString("lastSessionTitle", "") ?: ""
        set(v) = sp.edit().putString("lastSessionTitle", v).apply()

    var lastScreen: String
        get() = sp.getString("lastScreen", "") ?: ""
        set(v) = sp.edit().putString("lastScreen", v).apply()

    var contextSessionId: String
        get() = sp.getString("contextSessionId", "") ?: ""
        set(v) = sp.edit().putString("contextSessionId", v).apply()

    var contextUsed: Long
        get() = sp.getLong("contextUsed", 0L)
        set(v) = sp.edit().putLong("contextUsed", v).apply()

    var hiddenModels: Set<String>
        get() = sp.getStringSet("hiddenModels", emptySet()) ?: emptySet()
        set(v) = sp.edit().putStringSet("hiddenModels", v).apply()

    var themeMode: String
        get() = sp.getString("themeMode", "system") ?: "system"
        set(v) = sp.edit().putString("themeMode", v).apply()

    var fontFamily: String
        get() = sp.getString("fontFamily", "system") ?: "system"
        set(v) = sp.edit().putString("fontFamily", v).apply()

    var fontScale: Float
        get() = sp.getFloat("fontScale", 1f)
        set(v) = sp.edit().putFloat("fontScale", v).apply()

    var directory: String
        get() = sp.getString("directory", "") ?: ""
        set(v) = sp.edit().putString("directory", v).apply()

    var parentDir: String
        get() = sp.getString("parentDir", "/home/gmpony") ?: "/home/gmpony"
        set(v) = sp.edit().putString("parentDir", v).apply()

    fun isConfigured(): Boolean = baseUrl.isNotBlank() && user.isNotBlank() && password.isNotBlank()
}
