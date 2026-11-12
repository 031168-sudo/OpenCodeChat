package ru.alfanomy.opencodechat

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class ChatSession(val id: String, val title: String, val directory: String)

sealed interface Block
data class TextBlock(val text: String) : Block
data class ReasoningBlock(val text: String) : Block
data class ToolBlock(
    val name: String, val title: String, val detail: String,
    val additions: Int, val deletions: Int, val status: String
) : Block
data class ErrorBlock(val text: String) : Block

data class ChatMessage(
    val id: String,
    val role: String,
    val blocks: List<Block>,
    val attachments: List<Attachment> = emptyList(),
    val created: Long = 0L
)

data class Project(val id: String, val name: String, val worktree: String)
data class ModelInfo(
    val providerId: String,
    val providerName: String,
    val modelId: String,
    val name: String,
    val variants: List<String> = emptyList(),
    val free: Boolean = false,
    val contextLimit: Long = 0
)
data class CommandInfo(val name: String, val description: String)
data class Attachment(val name: String, val mime: String, val data: String)

data class FormFieldInfo(
    val key: String,
    val title: String,
    val type: String,
    val required: Boolean,
    val options: List<Pair<String, String>> = emptyList() // value to label
)
data class PendingForm(
    val id: String, val sessionId: String, val title: String, val fields: List<FormFieldInfo>
)
data class PendingPermission(
    val id: String, val sessionId: String, val permission: String, val patterns: List<String>
)

data class SessionStats(
    val title: String,
    val cost: Double,
    val input: Long,
    val output: Long,
    val reasoning: Long,
    val cacheRead: Long,
    val cacheWrite: Long,
    val model: String,
    val agent: String,
    val directory: String,
    val created: Long,
    val updated: Long
)

data class FileChange(val file: String, val additions: Int, val deletions: Int, val patch: String)

data class ProjectUsage(
    val sessions: Int,
    val cost: Double,
    val input: Long,
    val output: Long,
    val reasoning: Long,
    val cacheRead: Long,
    val cacheWrite: Long,
    val created: Long,
    val updated: Long
)

data class FsEntry(val path: String, val type: String)
data class FsRead(val text: String?, val bytes: ByteArray?, val mime: String, val size: Long)
data class MentionFile(
    val uri: String, val name: String,
    val start: Int, val end: Int, val text: String
)

class EventStream(conn: HttpURLConnection) : AutoCloseable {
    val reader: BufferedReader = conn.inputStream.bufferedReader(Charsets.UTF_8)
    private val connection = conn
    override fun close() {
        runCatching { reader.close() }
        runCatching { connection.disconnect() }
    }
}

fun structuredErrorText(err: JSONObject?): String {
    if (err == null) return ""
    val msg = err.optString("message").trim()
    if (msg.isNotBlank()) return msg
    val type = err.optString("type").trim()
    val status = err.optInt("status", 0)
    return when {
        type.isNotBlank() && status > 0 -> "$type ($status)"
        type.isNotBlank() -> type
        status > 0 -> "Ошибка $status"
        else -> ""
    }
}

fun parseToolBlock(p: JSONObject): ToolBlock {
    val name = p.optString("name")
    val st = p.optJSONObject("state") ?: JSONObject()
    val status = st.optString("status")
    val input = st.optJSONObject("input") ?: JSONObject()
    val detail = when (name) {
        "bash", "shell" -> input.optString("command")
        "edit", "write", "read", "list" -> input.optString("filePath").ifBlank { input.optString("path") }
        "grep", "glob", "find" -> input.optString("pattern")
        else -> input.optString("description").ifBlank {
            input.optString("query").ifBlank {
                val keys = input.keys()
                if (keys.hasNext()) input.optString(keys.next()) else ""
            }
        }
    }.replace('\n', ' ').trim().take(140)
    var add = 0
    var del = 0
    val diff = st.optJSONObject("metadata")?.optString("diff")
    if (!diff.isNullOrBlank()) {
        for (line in diff.lineSequence()) {
            if (line.startsWith("+++") || line.startsWith("---")) continue
            if (line.startsWith("+")) add++ else if (line.startsWith("-")) del++
        }
    }
    return ToolBlock(name, "", detail, add, del, status)
}

class ApiClient(
    private val baseUrl: String,
    private val user: String,
    private val password: String
) {
    private fun authHeader(): String =
        "Basic " + Base64.encodeToString("$user:$password".toByteArray(), Base64.NO_WRAP)

    private fun request(path: String, method: String, body: JSONObject? = null, directory: String? = null): String {
        var full = baseUrl.trimEnd('/') + path
        if (!directory.isNullOrBlank()) {
            val enc = URLEncoder.encode(directory, "UTF-8").replace("+", "%20")
            full += if (path.contains('?')) "&directory=$enc" else "?directory=$enc"
        }
        val url = URL(full)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 15000
        conn.readTimeout = 600000
        conn.setRequestProperty("Authorization", authHeader())
        conn.setRequestProperty("Accept", "application/json")
        if (body != null) {
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
        if (code !in 200..299) throw RuntimeException("HTTP $code: ${text.take(300)}")
        return text
    }

    fun health(): String {
        val o = JSONObject(request("/api/info", "GET"))
        return o.optString("version").ifBlank { "ok" }
    }

    fun listProjects(): List<Project> {
        val arr = runCatching { JSONArray(request("/api/project", "GET")) }.getOrNull() ?: return emptyList()
        val out = ArrayList<Project>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val canonical = o.optString("canonical")
            if (canonical.isBlank()) continue
            val name = o.optString("name").ifBlank {
                canonical.trimEnd('/').substringAfterLast('/').ifBlank { canonical }
            }
            out.add(Project(o.optString("id"), name, canonical))
        }
        return out
    }

    fun listSessionDirectories(): List<String> {
        val dirs = LinkedHashSet<String>()
        val o = runCatching { JSONObject(request("/api/session?limit=200", "GET")) }.getOrNull() ?: return emptyList()
        val data = o.optJSONArray("data") ?: return emptyList()
        for (i in 0 until data.length()) {
            val d = data.getJSONObject(i).optJSONObject("location")?.optString("directory").orEmpty()
            if (d.isNotBlank()) dirs.add(d)
        }
        return dirs.toList()
    }

    fun listSessions(directory: String? = null): List<ChatSession> {
        val o = JSONObject(request("/api/session?limit=200&order=desc", "GET", directory = directory))
        val arr = o.optJSONArray("data") ?: return emptyList()
        val out = ArrayList<ChatSession>()
        for (i in 0 until arr.length()) {
            val s = arr.getJSONObject(i)
            out.add(
                ChatSession(
                    id = s.optString("id"),
                    title = s.optString("title").ifBlank { "(без названия)" },
                    directory = s.optJSONObject("location")?.optString("directory").orEmpty()
                )
            )
        }
        return out
    }

    fun activeSessions(): Set<String> {
        val o = JSONObject(request("/api/session/active", "GET"))
        val data = o.optJSONObject("data") ?: return emptySet()
        val out = HashSet<String>()
        val keys = data.keys()
        while (keys.hasNext()) out.add(keys.next())
        return out
    }

    private fun encQuery(s: String): String =
        URLEncoder.encode(s, "UTF-8").replace("+", "%20")

    private fun locationQuery(directory: String): String =
        "location%5Bdirectory%5D=" + encQuery(directory)

    private fun encFsPath(path: String): String =
        encQuery(path).replace("%2F", "/")

    fun fsList(directory: String, path: String = ""): List<FsEntry> {
        var url = "/api/fs/list?" + locationQuery(directory)
        if (path.isNotBlank()) url += "&path=" + encQuery(path)
        val o = JSONObject(request(url, "GET"))
        val arr = o.optJSONArray("data") ?: return emptyList()
        val out = ArrayList<FsEntry>()
        for (i in 0 until arr.length()) {
            val e = arr.getJSONObject(i)
            out.add(FsEntry(e.optString("path"), e.optString("type")))
        }
        return out
    }

    fun fsFind(directory: String, query: String, type: String? = "file", limit: Int = 50): List<FsEntry> {
        var url = "/api/fs/find?" + locationQuery(directory) +
            "&query=" + encQuery(query) + "&limit=$limit"
        if (!type.isNullOrBlank()) url += "&type=$type"
        val o = JSONObject(request(url, "GET"))
        val arr = o.optJSONArray("data") ?: return emptyList()
        val out = ArrayList<FsEntry>()
        for (i in 0 until arr.length()) {
            val e = arr.getJSONObject(i)
            out.add(FsEntry(e.optString("path"), e.optString("type")))
        }
        return out
    }

    private fun isTextLike(mime: String, path: String): Boolean {
        val m = mime.lowercase()
        if (m.startsWith("text/")) return true
        if (m.contains("json") || m.contains("xml") || m.contains("javascript") ||
            m.contains("x-sh") || m.contains("yaml") || m.contains("toml")
        ) return true
        val ext = path.substringAfterLast('.', "").lowercase()
        return ext in setOf(
            "kt", "kts", "java", "md", "txt", "json", "xml", "yaml", "yml",
            "html", "htm", "css", "js", "ts", "tsx", "jsx", "py", "go", "rs",
            "c", "cpp", "h", "hpp", "sh", "properties", "gradle", "toml", "ini",
            "conf", "sql", "bat", "cmd", "ps1", "dockerfile", "gitignore", "env",
            "csv", "log", "lock", "pro", "mk", "cmake", "swift", "dart", "rb", "php"
        )
    }

    fun fsRead(directory: String, path: String): FsRead {
        val url = URL(
            baseUrl.trimEnd('/') + "/api/fs/read/" + encFsPath(path) +
                "?" + locationQuery(directory)
        )
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 15000
        conn.readTimeout = 60000
        conn.setRequestProperty("Authorization", authHeader())
        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            conn.disconnect()
            throw RuntimeException("HTTP $code: ${err.take(300)}")
        }
        val mime = conn.contentType ?: "application/octet-stream"
        val size = conn.contentLengthLong
        val textLike = isTextLike(mime, path)
        val imageLike = mime.lowercase().startsWith("image/")
        var bytes: ByteArray? = null
        var text: String? = null
        if (textLike || imageLike) {
            val max = if (textLike) 1_000_000 else 8_000_000
            val out = java.io.ByteArrayOutputStream()
            conn.inputStream.use { input ->
                val buf = ByteArray(8192)
                var total = 0
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    if (total + n > max) {
                        out.write(buf, 0, max - total)
                        break
                    }
                    out.write(buf, 0, n)
                    total += n
                }
            }
            bytes = out.toByteArray()
            if (textLike) text = String(bytes, Charsets.UTF_8)
            if (imageLike) text = null
        }
        conn.disconnect()
        return FsRead(text, bytes, mime, if (size >= 0) size else (bytes?.size?.toLong() ?: 0L))
    }

    fun fsWrite(directory: String, path: String, input: java.io.InputStream, length: Long) {
        val url = URL(
            baseUrl.trimEnd('/') + "/api/experimental/fs/write?" +
                locationQuery(directory) + "&path=" + encQuery(path)
        )
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 15000
        conn.readTimeout = 300000
        conn.doOutput = true
        conn.setRequestProperty("Authorization", authHeader())
        conn.setRequestProperty("Content-Type", "application/octet-stream")
        if (length > 0) conn.setFixedLengthStreamingMode(length)
        else conn.setChunkedStreamingMode(0)
        conn.outputStream.use { out -> input.copyTo(out) }
        val code = conn.responseCode
        val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
        conn.disconnect()
        if (code !in 200..299) throw RuntimeException("HTTP $code: ${resp.take(300)}")
    }

    private fun ptyRun(directory: String, script: String, title: String) {
        val body = JSONObject()
            .put("command", "sh")
            .put("args", JSONArray().put("-c").put(script))
            .put("cwd", directory)
            .put("title", title)
        val resp = runCatching { JSONObject(request("/api/pty", "POST", body)) }.getOrNull()
        val ptyId = resp?.optJSONObject("data")?.optString("id").orEmpty()
        var finished = ptyId.isBlank()
        if (ptyId.isNotBlank()) {
            val deadline = System.currentTimeMillis() + 15000
            while (System.currentTimeMillis() < deadline) {
                Thread.sleep(200)
                val o = runCatching { JSONObject(request("/api/pty/$ptyId", "GET")) }.getOrNull()
                val st = o?.optJSONObject("data")?.optString("status").orEmpty()
                if (st == "exited" || st.isBlank()) {
                    finished = true
                    break
                }
            }
        }
        if (finished && ptyId.isNotBlank()) {
            runCatching { request("/api/pty/$ptyId", "DELETE") }
        }
    }

    fun fsDelete(directory: String, path: String, recursive: Boolean = false) {
        val cmd = if (recursive) "rm -rf -- " else "rm -f -- "
        ptyRun(directory, cmd + shellQuote(path), "delete " + path)
    }

    fun fsMkdir(directory: String, path: String) {
        ptyRun(directory, "mkdir -p -- " + shellQuote(path), "mkdir " + path)
    }

    fun fsRename(directory: String, from: String, to: String) {
        ptyRun(directory, "mv -- " + shellQuote(from) + " " + shellQuote(to), "mv " + from)
    }

    fun createSession(title: String, directory: String? = null): ChatSession {
        val body = JSONObject().put("title", title)
        if (!directory.isNullOrBlank()) body.put("location", JSONObject().put("directory", directory))
        val o = JSONObject(request("/api/session", "POST", body))
        val s = o.optJSONObject("data") ?: JSONObject()
        return ChatSession(
            s.optString("id"),
            s.optString("title").ifBlank { title },
            s.optJSONObject("location")?.optString("directory").orEmpty().ifBlank { directory.orEmpty() }
        )
    }

    fun messages(sessionId: String): List<ChatMessage> {
        // Длинные сессии содержат огромные вложения/вывод инструментов.
        // Берём только последний вопрос и последние ответы — это быстро
        // и не приводит к OOM на телефоне.
        val out = ArrayList<ChatMessage>()
        val user = JSONObject(
            request("/api/session/$sessionId/message?limit=1&order=desc&type=user", "GET")
        )
        user.optJSONArray("data")?.let { parseMessagesInto(it, out) }
        val assistant = JSONObject(
            request("/api/session/$sessionId/message?limit=20&order=desc&type=assistant", "GET")
        )
        assistant.optJSONArray("data")?.let { parseMessagesInto(it, out) }
        return out.sortedBy { it.created }.distinctBy { it.id }
    }

    private fun parseMessagesInto(arr: JSONArray, out: MutableList<ChatMessage>) {
        for (i in 0 until arr.length()) {
            val m = arr.getJSONObject(i)
            val id = m.optString("id")
            val created = m.optJSONObject("time")?.optLong("created") ?: 0L
            when (m.optString("type")) {
                "user" -> {
                    val files = ArrayList<Attachment>()
                    val farr = m.optJSONArray("files")
                    if (farr != null) {
                        for (j in 0 until farr.length()) {
                            val f = farr.getJSONObject(j)
                            // Файл-упоминание (@path) — это не вложение:
                            // он уже виден в тексте сообщения, отдельно не рисуем.
                            if (f.optJSONObject("mention") != null) continue
                            val data = f.optString("data")
                            if (data.isNotBlank()) {
                                files.add(Attachment(f.optString("name").ifBlank { "файл" }, f.optString("mime").ifBlank { "application/octet-stream" }, data))
                            }
                        }
                    }
                    val text = m.optString("text")
                    if (text.isNotBlank() || files.isNotEmpty()) {
                        out.add(ChatMessage(id, "user", listOf(TextBlock(text)), files, created))
                    }
                }
                "assistant" -> {
                    val blocks = ArrayList<Block>()
                    val content = m.optJSONArray("content")
                    if (content != null) {
                        for (j in 0 until content.length()) {
                            val c = content.getJSONObject(j)
                            when (c.optString("type")) {
                                "text" -> {
                                    val t = c.optString("text")
                                    if (t.isNotBlank()) blocks.add(TextBlock(t))
                                }
                                "reasoning" -> {
                                    // Промежуточные размышления в историю не попадают —
                                    // показываем их только вживую во время ответа.
                                }
                                "tool" -> blocks.add(parseToolBlock(c))
                            }
                        }
                    }
                    val errText = structuredErrorText(m.optJSONObject("error"))
                    if (errText.isNotBlank()) {
                        blocks.add(ErrorBlock(errText))
                    } else if (m.optString("finish") == "content-filter") {
                        blocks.add(ErrorBlock("Ответ заблокирован фильтром провайдера"))
                    }
                    if (blocks.isNotEmpty()) out.add(ChatMessage(id, "assistant", blocks, emptyList(), created))
                }
            }
        }
    }

    fun sendAsync(
        sessionId: String, text: String, files: List<Attachment>,
        providerId: String, modelId: String, variant: String?, agent: String?,
        mentions: List<MentionFile> = emptyList()
    ) {
        val modelRef = JSONObject().put("id", modelId).put("providerID", providerId)
        if (!variant.isNullOrBlank() && variant != "default") modelRef.put("variant", variant)
        runCatching { request("/api/session/$sessionId/model", "POST", JSONObject().put("model", modelRef)) }
        if (!agent.isNullOrBlank()) runCatching { request("/api/session/$sessionId/agent", "POST", JSONObject().put("agent", agent)) }

        val body = JSONObject().put("text", text)
        if (files.isNotEmpty() || mentions.isNotEmpty()) {
            val arr = JSONArray()
            for (f in files) {
                arr.put(
                    JSONObject()
                        .put("uri", "data:${f.mime};base64,${f.data}")
                        .put("name", f.name)
                )
            }
            for (m in mentions) {
                arr.put(
                    JSONObject()
                        .put("uri", m.uri)
                        .put("name", m.name)
                        .put(
                            "mention", JSONObject()
                                .put("start", m.start)
                                .put("end", m.end)
                                .put("text", m.text)
                        )
                )
            }
            body.put("files", arr)
        }
        request("/api/session/$sessionId/prompt", "POST", body)
    }

    fun sendCommand(sessionId: String, name: String, text: String, providerId: String, modelId: String, variant: String?, agent: String?) {
        val modelRef = JSONObject().put("id", modelId).put("providerID", providerId)
        if (!variant.isNullOrBlank() && variant != "default") modelRef.put("variant", variant)
        runCatching { request("/api/session/$sessionId/model", "POST", JSONObject().put("model", modelRef)) }
        if (!agent.isNullOrBlank()) runCatching { request("/api/session/$sessionId/agent", "POST", JSONObject().put("agent", agent)) }
        request("/api/session/$sessionId/command", "POST", JSONObject().put("name", name).put("text", text))
    }

    fun respondPermission(sessionId: String, permissionId: String, response: String) {
        request(
            "/api/session/$sessionId/permission/$permissionId/reply", "POST",
            JSONObject().put("decision", response)
        )
    }

    fun pendingPermissions(sessionId: String): List<PendingPermission> {
        val out = ArrayList<PendingPermission>()
        val o = runCatching { JSONObject(request("/api/session/$sessionId/permission", "GET")) }.getOrNull() ?: return out
        val arr = o.optJSONArray("data") ?: return out
        for (i in 0 until arr.length()) {
            val r = arr.getJSONObject(i)
            val res = r.optJSONArray("resources")
            val list = if (res != null) (0 until res.length()).map { res.optString(it) } else emptyList()
            out.add(PendingPermission(r.optString("id"), sessionId, r.optString("action"), list))
        }
        return out
    }

    fun pendingForms(sessionId: String): List<PendingForm> {
        val out = ArrayList<PendingForm>()
        val o = runCatching { JSONObject(request("/api/session/$sessionId/form", "GET")) }.getOrNull() ?: return out
        val arr = o.optJSONArray("data") ?: return out
        for (i in 0 until arr.length()) {
            val f = arr.getJSONObject(i)
            val fields = ArrayList<FormFieldInfo>()
            val farr = f.optJSONArray("fields")
            if (farr != null) {
                for (j in 0 until farr.length()) {
                    val fd = farr.getJSONObject(j)
                    val opts = ArrayList<Pair<String, String>>()
                    val oarr = fd.optJSONArray("options")
                    if (oarr != null) {
                        for (k in 0 until oarr.length()) {
                            val op = oarr.getJSONObject(k)
                            opts.add(op.optString("value") to op.optString("label"))
                        }
                    }
                    fields.add(
                        FormFieldInfo(
                            key = fd.optString("key"),
                            title = fd.optString("title").ifBlank { fd.optString("key") },
                            type = fd.optString("type"),
                            required = fd.optBoolean("required"),
                            options = opts
                        )
                    )
                }
            }
            out.add(PendingForm(f.optString("id"), sessionId, f.optString("title").ifBlank { "Вопрос" }, fields))
        }
        return out
    }

    fun replyForm(sessionId: String, formId: String, answer: JSONObject) {
        request("/api/session/$sessionId/form/$formId/reply", "POST", JSONObject().put("answer", answer))
    }

    fun cancelForm(sessionId: String, formId: String) {
        request("/api/session/$sessionId/form/$formId", "DELETE")
    }

    fun renameSession(sessionId: String, title: String) {
        request("/api/session/$sessionId", "PATCH", JSONObject().put("title", title))
    }

    fun deleteSession(sessionId: String) {
        request("/api/session/$sessionId", "DELETE")
    }

    fun forkSession(sessionId: String, before: String?): ChatSession {
        val body = JSONObject()
        if (!before.isNullOrBlank()) body.put("before", before)
        val o = JSONObject(request("/api/session/$sessionId/fork", "POST", body))
        val s = o.optJSONObject("data") ?: JSONObject()
        return ChatSession(s.optString("id"), s.optString("title").ifBlank { "Форк" }, s.optJSONObject("location")?.optString("directory").orEmpty())
    }

    fun compactSession(sessionId: String) {
        request("/api/session/$sessionId/compact", "POST", JSONObject())
    }

    fun interrupt(sessionId: String) {
        runCatching { request("/api/session/$sessionId/interrupt", "POST", JSONObject()) }
    }

    fun revertStage(sessionId: String, messageId: String) {
        request("/api/session/$sessionId/revert/stage", "POST", JSONObject().put("messageID", messageId))
    }

    fun revertCommit(sessionId: String) {
        request("/api/session/$sessionId/revert/commit", "POST", JSONObject())
    }

    fun background(sessionId: String) {
        runCatching { request("/api/session/$sessionId/background", "POST", JSONObject()) }
    }

    /**
     * Суммарное использование по всем сессиям каталога (проекта):
     * стоимость и токены. Проходит все страницы списка сессий.
     */
    fun projectUsage(directory: String): ProjectUsage {
        var cursor: String? = null
        var pages = 0
        var sessions = 0
        var cost = 0.0
        var input = 0L
        var output = 0L
        var reasoning = 0L
        var cacheRead = 0L
        var cacheWrite = 0L
        var created = Long.MAX_VALUE
        var updated = 0L
        while (pages < 50) {
            var path = "/api/session?limit=200&order=desc"
            if (!cursor.isNullOrBlank()) path += "&cursor=" + URLEncoder.encode(cursor, "UTF-8")
            val o = JSONObject(request(path, "GET", directory = directory))
            val arr = o.optJSONArray("data") ?: break
            for (i in 0 until arr.length()) {
                val s = arr.getJSONObject(i)
                sessions++
                cost += s.optDouble("cost", 0.0)
                val t = s.optJSONObject("tokens") ?: JSONObject()
                input += t.optLong("input")
                output += t.optLong("output")
                reasoning += t.optLong("reasoning")
                val cache = t.optJSONObject("cache")
                cacheRead += cache?.optLong("read") ?: 0L
                cacheWrite += cache?.optLong("write") ?: 0L
                val time = s.optJSONObject("time")
                val cr = time?.optLong("created") ?: 0L
                val up = time?.optLong("updated") ?: 0L
                if (cr > 0 && cr < created) created = cr
                if (up > updated) updated = up
            }
            cursor = o.optJSONObject("cursor")?.optString("next")
                ?.let { if (it.isBlank() || it == "null") null else it }
            pages++
            if (arr.length() == 0 || cursor.isNullOrBlank()) break
        }
        return ProjectUsage(
            sessions = sessions,
            cost = cost,
            input = input,
            output = output,
            reasoning = reasoning,
            cacheRead = cacheRead,
            cacheWrite = cacheWrite,
            created = if (created == Long.MAX_VALUE) 0L else created,
            updated = updated
        )
    }

    fun sessionStats(sessionId: String): SessionStats? {
        val o = runCatching { JSONObject(request("/api/session/$sessionId", "GET")) }.getOrNull() ?: return null
        val d = o.optJSONObject("data") ?: return null
        val t = d.optJSONObject("tokens") ?: JSONObject()
        val c = t.optJSONObject("cache") ?: JSONObject()
        val model = d.optJSONObject("model")
        val time = d.optJSONObject("time")
        return SessionStats(
            title = d.optString("title"),
            cost = d.optDouble("cost", 0.0),
            input = t.optLong("input"),
            output = t.optLong("output"),
            reasoning = t.optLong("reasoning"),
            cacheRead = c.optLong("read"),
            cacheWrite = c.optLong("write"),
            model = (model?.optString("providerID").orEmpty()) + "/" + (model?.optString("id").orEmpty()),
            agent = d.optString("agent"),
            directory = d.optJSONObject("location")?.optString("directory").orEmpty(),
            created = time?.optLong("created") ?: 0L,
            updated = time?.optLong("updated") ?: 0L
        )
    }

    fun contextTokens(sessionId: String): Long {
        val o = runCatching { JSONObject(request("/api/session/$sessionId/context", "GET")) }.getOrNull() ?: return 0L
        val arr = o.optJSONArray("data") ?: return 0L
        for (i in arr.length() - 1 downTo 0) {
            val m = arr.getJSONObject(i)
            if (m.optString("type") != "assistant") continue
            val t = m.optJSONObject("tokens") ?: continue
            val c = t.optJSONObject("cache")
            val used = t.optLong("input") + (c?.optLong("read") ?: 0L) + (c?.optLong("write") ?: 0L)
            if (used > 0) return used
        }
        return 0L
    }

    fun sessionDiff(sessionId: String): List<FileChange> {
        val o = runCatching { JSONObject(request("/api/session/$sessionId/diff", "GET")) }.getOrNull() ?: return emptyList()
        val arr = o.optJSONArray("data") ?: return emptyList()
        val out = ArrayList<FileChange>()
        for (i in 0 until arr.length()) {
            val f = arr.getJSONObject(i)
            val patch = f.optString("patch")
            var add = 0
            var del = 0
            for (line in patch.lineSequence()) {
                if (line.startsWith("+++") || line.startsWith("---")) continue
                if (line.startsWith("+")) add++ else if (line.startsWith("-")) del++
            }
            out.add(FileChange(f.optString("file"), add, del, patch))
        }
        return out
    }

    fun listCommands(): List<CommandInfo> {
        val o = JSONObject(request("/api/command", "GET"))
        val arr = o.optJSONArray("data") ?: return emptyList()
        val out = ArrayList<CommandInfo>()
        for (i in 0 until arr.length()) {
            val c = arr.getJSONObject(i)
            val n = c.optString("name")
            if (n.isNotBlank()) out.add(CommandInfo(n, c.optString("description")))
        }
        return out
    }

    fun listAgents(): List<String> {
        val o = JSONObject(request("/api/agent", "GET"))
        val arr = o.optJSONArray("data") ?: return emptyList()
        val out = ArrayList<String>()
        for (i in 0 until arr.length()) {
            val a = arr.getJSONObject(i)
            val id = a.optString("id")
            if (id.isNotBlank() && !a.optBoolean("hidden") && a.optString("mode") != "subagent") out.add(id)
        }
        return out
    }

    fun listModels(): List<ModelInfo> {
        val providers = runCatching {
            val o = JSONObject(request("/api/provider", "GET"))
            val arr = o.optJSONArray("data") ?: JSONArray()
            val map = HashMap<String, String>()
            for (i in 0 until arr.length()) {
                val p = arr.getJSONObject(i)
                map[p.optString("id")] = p.optString("name").ifBlank { p.optString("id") }
            }
            map
        }.getOrDefault(emptyMap())

        val o = JSONObject(request("/api/model", "GET"))
        val arr = o.optJSONArray("data") ?: return emptyList()
        val out = ArrayList<ModelInfo>()
        for (i in 0 until arr.length()) {
            val m = arr.getJSONObject(i)
            if (!m.optBoolean("enabled", true)) continue
            val pid = m.optString("providerID")
            val mid = m.optString("modelID").ifBlank { m.optString("id") }
            val variants = ArrayList<String>()
            val varr = m.optJSONArray("variants")
            if (varr != null) {
                for (j in 0 until varr.length()) variants.add(varr.getJSONObject(j).optString("id"))
            }
            val ctx = m.optJSONObject("limit")?.optLong("context") ?: 0L
            out.add(
                ModelInfo(
                    providerId = pid,
                    providerName = providers[pid] ?: pid,
                    modelId = mid,
                    name = m.optString("name").ifBlank { mid },
                    variants = variants.filter { it.isNotBlank() && it != "none" },
                    free = mid.contains("free", ignoreCase = true) || m.optString("name").contains("free", ignoreCase = true),
                    contextLimit = ctx
                )
            )
        }
        return out.sortedBy { it.name.lowercase() }
    }

    fun defaultModel(): Pair<String, String>? {
        val o = runCatching { JSONObject(request("/api/model/default", "GET")) }.getOrNull() ?: return null
        val d = o.optJSONObject("data") ?: return null
        val pid = d.optString("providerID")
        val mid = d.optString("modelID")
        return if (pid.isBlank() || mid.isBlank()) null else pid to mid
    }

    fun openEventStream(): EventStream {
        val conn = URL(baseUrl.trimEnd('/') + "/api/event").openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 15000
        conn.readTimeout = 0
        conn.setRequestProperty("Authorization", authHeader())
        conn.setRequestProperty("Accept", "text/event-stream")
        val code = conn.responseCode
        if (code !in 200..299) {
            conn.disconnect()
            throw RuntimeException("HTTP $code")
        }
        return EventStream(conn)
    }

    private fun shellQuote(s: String): String = "'" + s.replace("'", "'\\''") + "'"

    fun createProject(parent: String, name: String): String {
        val parentPath = parent.trim().trimEnd('/').ifBlank { "/" }
        val path = parentPath.trimEnd('/') + "/" + name
        val script = "mkdir -p " + shellQuote(path) + " && git -C " + shellQuote(path) + " init -q"
        val body = JSONObject()
            .put("command", "sh")
            .put("args", JSONArray().put("-c").put(script))
            .put("cwd", "/")
            .put("title", name)
        runCatching { request("/api/pty", "POST", body) }
        Thread.sleep(600)
        // Создаём сессию в каталоге — это регистрирует проект.
        runCatching { createSession("Новый чат", path) }
        return path
    }
}
