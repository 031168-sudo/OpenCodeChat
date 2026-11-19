package ru.alfanomy.opencodechat

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Profile(val id: String, val name: String, val root: String)

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("opencode_chat", Context.MODE_PRIVATE)

    private fun scopedFor(id: String, base: String): String =
        if (id.isBlank()) base else "${base}_$id"

    private fun scoped(base: String): String = scopedFor(activeProfileId, base)

    fun providerIdFor(id: String): String =
        sp.getString(scopedFor(id, "providerId"), "opencode") ?: "opencode"

    fun modelIdFor(id: String): String =
        sp.getString(scopedFor(id, "modelId"), "deepseek-v4.1-flash") ?: "deepseek-v4.1-flash"

    fun agentFor(id: String): String =
        sp.getString(scopedFor(id, "agent"), "build") ?: "build"

    fun variantFor(id: String): String =
        sp.getString(scopedFor(id, "variant"), "") ?: ""

    fun hiddenModelsFor(id: String): Set<String> =
        sp.getStringSet(scopedFor(id, "hiddenModels"), emptySet()) ?: emptySet()

    var activeProfileId: String
        get() = sp.getString("activeProfileId", "") ?: ""
        set(v) = sp.edit().putString("activeProfileId", v).apply()

    var profilesJson: String
        get() = sp.getString("profiles", "[]") ?: "[]"
        set(v) = sp.edit().putString("profiles", v).apply()

    // Первый синк с /profiles/api: локальные профили (если были) один раз
    // добавляются на сервер. Дальше сервер — источник истины.
    var profilesSynced: Boolean
        get() = sp.getBoolean("profilesSynced", false)
        set(v) = sp.edit().putBoolean("profilesSynced", v).apply()

    val profiles: List<Profile>
        get() = runCatching {
            val arr = JSONArray(profilesJson)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = o.optString("id")
                val name = o.optString("name")
                if (id.isBlank() || name.isBlank()) return@mapNotNull null
                Profile(id, name, o.optString("root"))
            }
        }.getOrDefault(emptyList())

    val activeProfile: Profile?
        get() = profiles.firstOrNull { it.id == activeProfileId }

    fun saveProfiles(list: List<Profile>) {
        val arr = JSONArray()
        for (p in list) {
            val root = p.root.trim().trimEnd('/')
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("root", if (root == "/") "" else root)
            )
        }
        profilesJson = arr.toString()
    }

    var baseUrl: String
        get() = sp.getString("baseUrl", "") ?: ""
        set(v) = sp.edit().putString("baseUrl", v).apply()

    var tailscaleUrl: String
        get() = sp.getString("tailscaleUrl", "") ?: ""
        set(v) = sp.edit().putString("tailscaleUrl", v).apply()

    /** Корень для новых проектов, если у профиля не задана своя папка. */
    var defaultRoot: String
        get() = sp.getString("defaultRoot", "") ?: ""
        set(v) = sp.edit().putString("defaultRoot", v.trim().trimEnd('/')).apply()

    var connectionMode: String
        get() = sp.getString("connectionMode", "auto") ?: "auto"
        set(v) = sp.edit().putString("connectionMode", v).apply()

    var speechUrl: String
        get() = sp.getString("speechUrl", "") ?: ""
        set(v) = sp.edit().putString("speechUrl", v).apply()

    var activeBaseUrl: String
        get() = sp.getString("activeBaseUrl", "") ?: ""
        set(v) = sp.edit().putString("activeBaseUrl", v).apply()

    var activeSpeechUrl: String
        get() = sp.getString("activeSpeechUrl", "") ?: ""
        set(v) = sp.edit().putString("activeSpeechUrl", v).apply()

    var activeKind: String
        get() = sp.getString("activeKind", "") ?: ""
        set(v) = sp.edit().putString("activeKind", v).apply()

    var user: String
        get() = sp.getString("user", "opencode") ?: ""
        set(v) = sp.edit().putString("user", v).apply()

    var password: String
        get() = sp.getString("password", "") ?: ""
        set(v) = sp.edit().putString("password", v).apply()

    var providerId: String
        get() = sp.getString(scoped("providerId"), "opencode") ?: "opencode"
        set(v) = sp.edit().putString(scoped("providerId"), v).apply()

    var modelId: String
        get() = sp.getString(scoped("modelId"), "deepseek-v4.1-flash") ?: "deepseek-v4.1-flash"
        set(v) = sp.edit().putString(scoped("modelId"), v).apply()

    var agent: String
        get() = sp.getString(scoped("agent"), "build") ?: "build"
        set(v) = sp.edit().putString(scoped("agent"), v).apply()

    var variant: String
        get() = sp.getString(scoped("variant"), "") ?: ""
        set(v) = sp.edit().putString(scoped("variant"), v).apply()

    var lastSessionId: String
        get() = sp.getString(scoped("lastSessionId"), "") ?: ""
        set(v) = sp.edit().putString(scoped("lastSessionId"), v).apply()

    var lastSessionTitle: String
        get() = sp.getString(scoped("lastSessionTitle"), "") ?: ""
        set(v) = sp.edit().putString(scoped("lastSessionTitle"), v).apply()

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
        get() = sp.getStringSet(scoped("hiddenModels"), emptySet()) ?: emptySet()
        set(v) = sp.edit().putStringSet(scoped("hiddenModels"), v).apply()

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
        get() = sp.getString(scoped("directory"), "") ?: ""
        set(v) = sp.edit().putString(scoped("directory"), v).apply()

    var parentDir: String
        get() = sp.getString(
            scoped("parentDir"),
            ""
        ) ?: ""
        set(v) = sp.edit().putString(scoped("parentDir"), v).apply()

    fun isConfigured(): Boolean =
        (baseUrl.isNotBlank() || tailscaleUrl.isNotBlank()) && user.isNotBlank()
}
