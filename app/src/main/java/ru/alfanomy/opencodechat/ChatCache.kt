package ru.alfanomy.opencodechat

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Локальный снимок последней открытой сессии. Нужен, чтобы после перезапуска
 * процесса (Android может убить приложение в фоне) сразу показать то же
 * содержимое, а не пустой экран и повторную загрузку.
 */
object ChatCache {
    private const val FILE_NAME = "last_chat.json"

    private fun file(context: Context) = File(context.filesDir, FILE_NAME)

    @Synchronized
    fun save(context: Context, sessionId: String, messages: List<ChatMessage>) {
        runCatching {
            val root = JSONObject()
            root.put("sessionId", sessionId)
            val arr = JSONArray()
            for (m in messages) {
                val o = JSONObject()
                o.put("id", m.id)
                o.put("role", m.role)
                o.put("created", m.created)
                val blocks = JSONArray()
                for (b in m.blocks) blocks.put(blockToJson(b))
                o.put("blocks", blocks)
                // Вложения (base64-картинки) в кэш не пишем: они тяжёлые,
                // а после обновления с сервера всё равно подгрузятся.
                arr.put(o)
            }
            root.put("messages", arr)

            val f = file(context)
            val tmp = File(context.filesDir, "$FILE_NAME.tmp")
            tmp.writeText(root.toString())
            if (!tmp.renameTo(f)) {
                f.delete()
                tmp.renameTo(f)
            }
        }
    }

    fun load(context: Context, sessionId: String): List<ChatMessage>? {
        val f = file(context)
        if (sessionId.isBlank() || !f.exists()) return null
        // Старые кэши могли содержать base64-картинки и быть очень большими.
        // Не рискуем памятью: такой файл просто удаляем.
        if (f.length() > 1_500_000L) {
            runCatching { f.delete() }
            return null
        }
        return runCatching {
            val root = JSONObject(f.readText())
            if (root.optString("sessionId") != sessionId) return null
            val arr = root.optJSONArray("messages") ?: return null
            val out = ArrayList<ChatMessage>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val blocks = ArrayList<Block>()
                val ba = o.optJSONArray("blocks")
                if (ba != null) {
                    for (j in 0 until ba.length()) {
                        blockFromJson(ba.getJSONObject(j))?.let { blocks.add(it) }
                    }
                }
                val atts = ArrayList<Attachment>()
                val aa = o.optJSONArray("attachments")
                if (aa != null) {
                    for (j in 0 until aa.length()) {
                        val a = aa.getJSONObject(j)
                        atts.add(
                            Attachment(
                                a.optString("name").ifBlank { "файл" },
                                a.optString("mime").ifBlank { "application/octet-stream" },
                                a.optString("data")
                            )
                        )
                    }
                }
                out.add(ChatMessage(o.optString("id"), o.optString("role"), blocks, atts, o.optLong("created")))
            }
            out
        }.getOrNull()
    }

    fun clear(context: Context) {
        runCatching { file(context).delete() }
    }

    private fun blockToJson(b: Block): JSONObject = JSONObject().apply {
        when (b) {
            is TextBlock -> {
                put("t", "text")
                put("text", b.text)
            }
            is ErrorBlock -> {
                put("t", "error")
                put("text", b.text)
            }
            is ReasoningBlock -> {
                put("t", "reasoning")
                put("text", b.text)
            }
            is ToolBlock -> {
                put("t", "tool")
                put("name", b.name)
                put("detail", b.detail)
                put("additions", b.additions)
                put("deletions", b.deletions)
                put("status", b.status)
            }
        }
    }

    private fun blockFromJson(o: JSONObject): Block? = when (o.optString("t")) {
        "text" -> TextBlock(o.optString("text"))
        "error" -> ErrorBlock(o.optString("text"))
        "reasoning" -> ReasoningBlock(o.optString("text"))
        "tool" -> ToolBlock(
            o.optString("name"), "", o.optString("detail"),
            o.optInt("additions"), o.optInt("deletions"), o.optString("status")
        )
        else -> null
    }
}
