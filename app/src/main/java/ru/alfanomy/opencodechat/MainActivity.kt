package ru.alfanomy.opencodechat

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.format.DateFormat
import android.util.Base64
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private enum class Screen { SETTINGS, PROJECTS, SESSIONS, CHAT, FILES }

private enum class UsagePeriod { DAY, MONTH, ALL, CUSTOM }

private fun startOfDay(ms: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = ms
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun endOfDay(ms: Long): Long = startOfDay(ms) + 24L * 60 * 60 * 1000 - 1

private fun startOfMonth(ms: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = ms
    c.set(Calendar.DAY_OF_MONTH, 1)
    return startOfDay(c.timeInMillis)
}

private fun endOfMonth(ms: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = ms
    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
    return endOfDay(c.timeInMillis)
}

private fun usageRange(period: UsagePeriod, customStart: Long, customEnd: Long): Pair<Long?, Long?> =
    when (period) {
        UsagePeriod.DAY -> {
            val now = System.currentTimeMillis()
            startOfDay(now) to endOfDay(now)
        }
        UsagePeriod.MONTH -> {
            val now = System.currentTimeMillis()
            startOfMonth(now) to endOfMonth(now)
        }
        UsagePeriod.ALL -> null to null
        UsagePeriod.CUSTOM -> {
            val a = startOfDay(customStart)
            val b = endOfDay(customEnd)
            if (a <= b) a to b else b to a
        }
    }

private fun fmtDay(ms: Long): String =
    java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date(ms))

private enum class SessionAction { RENAME, UNDO, COMPACT, FORK, BACKGROUND, CHANGES, USAGE, EXPORT, DELETE }

class MainActivity : ComponentActivity() {
    var resumeHandler: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }

    override fun onResume() {
        super.onResume()
        resumeHandler?.invoke()
    }
}

private fun usableProjects(list: List<Project>): List<Project> =
    list.filter { it.worktree.isNotBlank() && it.worktree != "/" }
        .distinctBy { it.worktree }
        .sortedBy { it.name.lowercase() }

private fun projectLabel(projects: List<Project>, directory: String): String =
    projects.firstOrNull { it.worktree == directory }?.name
        ?: directory.trimEnd('/').substringAfterLast('/').ifBlank { directory }

private tailrec fun Context.findMainActivity(): MainActivity? = when (this) {
    is MainActivity -> this
    is ContextWrapper -> baseContext.findMainActivity()
    else -> null
}

private val LoraFontFamily = FontFamily(Font(R.font.lora))
private val RobotoMonoFontFamily = FontFamily(Font(R.font.roboto_mono))

private fun fontFamilyFor(name: String): FontFamily = when (name) {
    "sans" -> FontFamily.SansSerif
    "serif" -> LoraFontFamily
    "mono" -> RobotoMonoFontFamily
    else -> FontFamily.Default
}

private fun isTailscaleLike(url: String): Boolean {
    val host = runCatching { java.net.URI(url).host }.getOrNull()?.lowercase() ?: return false
    if (host.endsWith(".ts.net")) return true
    val parts = host.split('.')
    if (parts.size == 4 && parts[0] == "100") {
        val second = parts[1].toIntOrNull() ?: return false
        return second in 64..127
    }
    return false
}

private fun speechUrlFor(baseUrl: String): String {
    val base = baseUrl.trim().trimEnd('/')
    if (base.isBlank()) return ""
    if (base.contains("gmpony.alfanomy.ru")) return "$base/speech/inference"
    val host = runCatching { java.net.URI(base).host }.getOrNull()
    if (!host.isNullOrBlank()) return "http://$host:18080/inference"
    return "$base/speech/inference"
}

private fun prettyModel(id: String): String =
    id.split('-').filter { it.isNotBlank() }.joinToString(" ") { w ->
        val c = w.replaceFirstChar { it.uppercase() }
        when (c.lowercase()) {
            "deepseek" -> "DeepSeek"
            "gpt" -> "GPT"
            "glm" -> "GLM"
            else -> c
        }
    }

private fun Typography.withFontFamily(fontFamily: FontFamily): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = fontFamily),
    displayMedium = displayMedium.copy(fontFamily = fontFamily),
    displaySmall = displaySmall.copy(fontFamily = fontFamily),
    headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
    titleLarge = titleLarge.copy(fontFamily = fontFamily),
    titleMedium = titleMedium.copy(fontFamily = fontFamily),
    titleSmall = titleSmall.copy(fontFamily = fontFamily),
    bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = bodySmall.copy(fontFamily = fontFamily),
    labelLarge = labelLarge.copy(fontFamily = fontFamily),
    labelMedium = labelMedium.copy(fontFamily = fontFamily),
    labelSmall = labelSmall.copy(fontFamily = fontFamily)
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun App() {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    val hasLast = prefs.isConfigured() && prefs.lastSessionId.isNotBlank()
    val cachedMessages = remember { if (hasLast) ChatCache.load(context, prefs.lastSessionId) else null }
    var screen by remember {
        mutableStateOf(
            when {
                !prefs.isConfigured() -> Screen.SETTINGS
                prefs.lastScreen == Screen.CHAT.name && hasLast -> Screen.CHAT
                prefs.lastScreen == Screen.SESSIONS.name && prefs.directory.isNotBlank() -> Screen.SESSIONS
                prefs.lastScreen == Screen.PROJECTS.name -> Screen.PROJECTS
                prefs.lastScreen == Screen.SETTINGS.name -> Screen.SETTINGS
                hasLast -> Screen.CHAT
                else -> Screen.PROJECTS
            }
        )
    }

    LaunchedEffect(screen) { prefs.lastScreen = screen.name }

    var baseUrl by remember { mutableStateOf(prefs.baseUrl) }
    var tailscaleUrl by remember { mutableStateOf(prefs.tailscaleUrl) }
    var connectionMode by remember { mutableStateOf(prefs.connectionMode) }
    var speechUrl by remember { mutableStateOf(prefs.speechUrl) }
    var defaultRoot by remember { mutableStateOf(prefs.defaultRoot) }
    var activeBaseUrl by remember { mutableStateOf(prefs.activeBaseUrl) }
    var activeKind by remember { mutableStateOf(prefs.activeKind) }
    var user by remember { mutableStateOf(prefs.user) }
    var password by remember { mutableStateOf(prefs.password) }
    var themeMode by remember { mutableStateOf(prefs.themeMode) }
    var fontName by remember { mutableStateOf(prefs.fontFamily) }
    var fontScale by remember { mutableStateOf(prefs.fontScale) }
    var profiles by remember { mutableStateOf(prefs.profiles) }
    var activeProfileId by remember { mutableStateOf(prefs.activeProfileId) }
    var profileSyncError by remember { mutableStateOf("") }
    var profileSyncing by remember { mutableStateOf(false) }
    val profileSyncMutex = remember { Mutex() }
    var scopePath by remember {
        mutableStateOf(prefs.profiles.firstOrNull { it.id == prefs.activeProfileId }?.root?.trimEnd('/') ?: "")
    }

    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var directory by remember { mutableStateOf(prefs.directory) }
    var sessions by remember { mutableStateOf<List<ChatSession>>(emptyList()) }
    var messages by remember { mutableStateOf(cachedMessages ?: emptyList()) }
    var historyCursor by remember { mutableStateOf<String?>(null) }
    var historyLoading by remember { mutableStateOf(false) }
    var historyHasMore by remember { mutableStateOf(true) }
    var current by remember {
        mutableStateOf<ChatSession?>(
            if (hasLast) ChatSession(prefs.lastSessionId, prefs.lastSessionTitle.ifBlank { "Сессия" }, prefs.directory)
            else null
        )
    }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    LaunchedEffect(messages, current?.id) {
        val sid = current?.id ?: return@LaunchedEffect
        if (messages.isNotEmpty()) {
            withContext(Dispatchers.IO) { ChatCache.save(context, sid, messages) }
        }
    }

    var showNewProject by remember { mutableStateOf(false) }
    var newParent by remember { mutableStateOf(prefs.parentDir) }
    var newName by remember { mutableStateOf("") }
    var newProjectError by remember { mutableStateOf("") }
    var settingsFrom by remember { mutableStateOf(Screen.PROJECTS) }

    var models by remember { mutableStateOf<List<ModelInfo>>(emptyList()) }
    var providerId by remember { mutableStateOf(prefs.providerId) }
    var modelId by remember { mutableStateOf(prefs.modelId) }
    var agents by remember { mutableStateOf<List<String>>(emptyList()) }
    var agent by remember { mutableStateOf(prefs.agent) }
    var variant by remember { mutableStateOf(prefs.variant) }
    var showModelPicker by remember { mutableStateOf(false) }
    var showManageModels by remember { mutableStateOf(false) }
    var hiddenModels by remember { mutableStateOf(prefs.hiddenModels) }
    var modelQuery by remember { mutableStateOf("") }
    var pendingModel by remember { mutableStateOf<ModelInfo?>(null) }
    var commands by remember { mutableStateOf<List<CommandInfo>>(emptyList()) }
    var showSessionMenu by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var usage by remember { mutableStateOf<SessionStats?>(null) }
    var showUsage by remember { mutableStateOf(false) }
    var projectUsage by remember { mutableStateOf<ProjectUsage?>(null) }
    var showProjectUsage by remember { mutableStateOf(false) }
    var projectUsageIsProfile by remember { mutableStateOf(false) }
    var projectUsagePeriod by remember { mutableStateOf(UsagePeriod.MONTH) }
    var projectUsageCustomStart by remember { mutableStateOf(startOfMonth(System.currentTimeMillis())) }
    var projectUsageCustomEnd by remember { mutableStateOf(System.currentTimeMillis()) }
    var projectUsageLoading by remember { mutableStateOf(false) }
    var projectUsageRequest by remember { mutableStateOf(0) }
    var projectUsageDirectory by remember { mutableStateOf("") }
    var filesReturnScreen by remember { mutableStateOf(Screen.SESSIONS) }
    var showMentionPicker by remember { mutableStateOf(false) }
    var pickedMentions by remember { mutableStateOf<List<String>>(emptyList()) }
    val wavRecorder = remember { WavRecorder(context) }
    var recording by remember { mutableStateOf(false) }
    var transcribing by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableStateOf(0) }
    var micStartRequested by remember { mutableStateOf(false) }
    var changes by remember { mutableStateOf<List<FileChange>>(emptyList()) }
    var showChanges by remember { mutableStateOf(false) }

    var attachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var pendingPermission by remember { mutableStateOf<PendingPermission?>(null) }
    var pendingForm by remember { mutableStateOf<PendingForm?>(null) }
    var contextUsed by remember {
        mutableStateOf(if (prefs.contextSessionId.isNotBlank() && prefs.contextSessionId == prefs.lastSessionId) prefs.contextUsed else 0L)
    }

    val liveParts = remember { mutableStateMapOf<String, Block>() }
    val liveOrder = remember { mutableStateListOf<String>() }
    var liveAssistantId by remember { mutableStateOf<String?>(null) }

    fun liveClear() {
        liveParts.clear()
        liveOrder.clear()
    }

    fun liveUpsert(partId: String, block: Block) {
        if (!liveParts.containsKey(partId)) liveOrder.add(partId)
        liveParts[partId] = block
    }

    fun liveTextOf(partId: String): String = (liveParts[partId] as? TextBlock)?.text ?: ""
    val streamRef = remember { mutableStateOf<EventStream?>(null) }

    fun client() = ApiClient(
        prefs.activeBaseUrl.ifBlank { prefs.baseUrl },
        prefs.user, prefs.password,
        prefs.activeSpeechUrl
    )

    suspend fun resolveConnection(): Boolean {
        val mode = prefs.connectionMode
        val pub = prefs.baseUrl.trim().trimEnd('/')
        val ts = prefs.tailscaleUrl.trim().trimEnd('/')
        val cached = prefs.activeBaseUrl.trim().trimEnd('/')

        suspend fun pingUrl(url: String, timeoutMs: Int): Boolean =
            url.isNotBlank() && withContext(Dispatchers.IO) {
                runCatching {
                    ApiClient(url, prefs.user, prefs.password).ping(timeoutMs)
                }.getOrDefault(false)
            }

        val chosen = when (mode) {
            "public" -> when {
                pingUrl(pub, 4000) -> pub
                pingUrl(ts, 3000) -> ts
                else -> ""
            }
            "tailscale" -> when {
                pingUrl(ts, 4000) -> ts
                pingUrl(pub, 3000) -> pub
                else -> ""
            }
            else -> {
                var result = ""
                if (pingUrl(ts, 1500)) result = ts
                if (result.isBlank() && cached.isNotBlank() && cached != ts &&
                    pingUrl(cached, 1500)
                ) result = cached
                if (result.isBlank() && pub.isNotBlank() && pub != ts && pub != cached &&
                    pingUrl(pub, 2500)
                ) result = pub
                if (result.isBlank() && pub.isNotBlank()) result = pub
                result
            }
        }

        if (chosen.isBlank()) return false
        prefs.activeBaseUrl = chosen
        activeBaseUrl = chosen
        val kind = when {
            ts.isNotBlank() && chosen == ts -> "tailscale"
            pub.isNotBlank() && chosen == pub -> "public"
            isTailscaleLike(chosen) -> "tailscale"
            else -> "public"
        }
        prefs.activeKind = kind
        activeKind = kind
        prefs.activeSpeechUrl =
            if (prefs.speechUrl.isNotBlank()) prefs.speechUrl.trim()
            else speechUrlFor(chosen)
        return true
    }

    fun run(block: suspend () -> Unit) {
        scope.launch {
            busy = true
            status = ""
            try {
                block()
            } catch (e: Exception) {
                status = e.message ?: "Ошибка"
            } finally {
                busy = false
            }
        }
    }

    fun collectProjects(): List<Project> {
        val map = LinkedHashMap<String, Project>()
        for (p in runCatching { client().listProjects() }.getOrDefault(emptyList())) {
            if (p.worktree.isNotBlank()) map[p.worktree] = p
        }
        for (d in runCatching { client().listSessionDirectories() }.getOrDefault(emptyList())) {
            if (d.isNotBlank() && !map.containsKey(d)) {
                map[d] = Project("", d.trimEnd('/').substringAfterLast('/').ifBlank { d }, d)
            }
        }
        return usableProjects(map.values.toList())
    }

    fun loadProjects() = run {
        projects = withContext(Dispatchers.IO) { collectProjects() }
    }

    fun loadSessions() = run {
        sessions = withContext(Dispatchers.IO) { client().listSessions(directory) }
    }

    fun loadOlderMessages() {
        val s = current ?: return
        if (historyLoading || !historyHasMore) return
        historyLoading = true
        val cursor = historyCursor
        scope.launch {
            val page = withContext(Dispatchers.IO) {
                runCatching { client().messagePage(s.id, cursor) }.getOrNull()
            }
            if (current?.id != s.id) {
                historyLoading = false
                return@launch
            }
            if (page != null) {
                val (older, next) = page
                if (older.isNotEmpty()) {
                    messages = (messages + older)
                        .distinctBy { it.id }
                        .sortedBy { it.created }
                }
                historyCursor = next
                historyHasMore = next != null && older.isNotEmpty()
            }
            historyLoading = false
        }
    }

    fun mergeHistory(old: List<ChatMessage>, fresh: List<ChatMessage>): List<ChatMessage> {
        // После каждого шага сервер отдаёт только последний вопрос и ответы.
        // Объединяем со старым списком, чтобы уже показанные вопросы и
        // подгруженная вверх история не исчезали. Локальный «пузырь» вопроса
        // убираем, когда сервер вернул настоящий user-message.
        val freshHasUser = fresh.any { it.role == "user" }
        val newestFreshUser = fresh.filter { it.role == "user" }.maxOfOrNull { it.created } ?: 0L
        val base = if (freshHasUser) {
            old.filterNot { it.id.startsWith("local-") && it.created <= newestFreshUser }
        } else old
        return (fresh + base).distinctBy { it.id }.sortedBy { it.created }
    }

    fun loadProjectUsage() {
        val dir = projectUsageDirectory
        val recursive = projectUsageIsProfile
        val (from, to) = usageRange(projectUsagePeriod, projectUsageCustomStart, projectUsageCustomEnd)
        projectUsageRequest += 1
        val request = projectUsageRequest
        projectUsageLoading = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { client().projectUsage(dir, recursive, from, to) }.getOrNull()
            }
            if (request == projectUsageRequest) {
                projectUsage = result
                projectUsageLoading = false
            }
        }
    }

    fun openProjectUsage() {
        val dir = directory
        if (dir.isBlank()) return
        projectUsageDirectory = dir
        projectUsageIsProfile = false
        projectUsage = null
        showProjectUsage = true
        loadProjectUsage()
    }

    /**
     * Если сохранённая модель не существует на этом сервере (например,
     * приложение подключили к другому opencode), выбираем доступную.
     */
    fun ensureModelAvailable(list: List<ModelInfo>) {
        if (list.isEmpty()) return
        if (list.any { it.providerId == providerId && it.modelId == modelId }) return
        val pick = list.firstOrNull { it.providerId == "opencode" } ?: list.first()
        providerId = pick.providerId
        modelId = pick.modelId
        prefs.providerId = pick.providerId
        prefs.modelId = pick.modelId
        if (variant.isNotBlank() && variant !in pick.variants) {
            variant = ""
            prefs.variant = ""
        }
    }

    fun loadModels() {
        scope.launch {
            val loaded = withContext(Dispatchers.IO) {
                runCatching { client().listModels() }.getOrDefault(emptyList())
            }
            models = loaded
            ensureModelAvailable(loaded)
        }
    }

    fun loadAgents() {
        scope.launch {
            agents = withContext(Dispatchers.IO) { runCatching { client().listAgents() }.getOrDefault(emptyList()) }
        }
    }

    fun loadCommands() {
        scope.launch {
            commands = withContext(Dispatchers.IO) { runCatching { client().listCommands() }.getOrDefault(emptyList()) }
        }
    }

    fun stopStream() {
        streamRef.value?.close()
        streamRef.value = null
    }

    fun readAttachment(uri: Uri) {
        val cr = context.contentResolver
        val name = runCatching {
            cr.query(uri, null, null, null, null)?.use { cur ->
                val idx = cur.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cur.moveToFirst() && idx >= 0) cur.getString(idx) else null
            }
        }.getOrNull() ?: "файл"
        val mime = cr.getType(uri)?.takeIf { it.isNotBlank() } ?: when (name.substringAfterLast('.', "").lowercase()) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
        val bytes = runCatching { cr.openInputStream(uri)?.use { it.readBytes() } }.getOrNull() ?: return
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        attachments = attachments + Attachment(name, mime, b64)
    }

    fun openProject(p: Project) {
        stopStream()
        directory = p.worktree
        prefs.directory = p.worktree
        current = null
        screen = Screen.SESSIONS
        loadSessions()
    }

    fun checkPending(sessionId: String) {
        scope.launch {
            val forms = withContext(Dispatchers.IO) { runCatching { client().pendingForms(sessionId) }.getOrDefault(emptyList()) }
            val ps = withContext(Dispatchers.IO) { runCatching { client().pendingPermissions(sessionId) }.getOrDefault(emptyList()) }
            if (forms.isNotEmpty() && pendingForm == null) pendingForm = forms.first()
            if (ps.isNotEmpty() && pendingPermission == null) pendingPermission = ps.first()
        }
    }

    fun refreshContext(sessionId: String) {
        scope.launch {
            val used = withContext(Dispatchers.IO) { runCatching { client().contextTokens(sessionId) }.getOrDefault(0L) }
            if (used > 0) contextUsed = used
        }
    }

    val watchJob = remember { mutableStateOf<Job?>(null) }

    fun switchProfile(id: String) {
        if (id == prefs.activeProfileId) {
            // Повторный выбор профиля возвращает из общего списка в его корень.
            scopePath = prefs.profiles.firstOrNull { it.id == id }?.root?.trimEnd('/') ?: ""
            return
        }
        watchJob.value?.cancel()
        watchJob.value = null
        stopStream()
        prefs.activeProfileId = id
        activeProfileId = id
        val p = prefs.profiles.firstOrNull { it.id == id }
        scopePath = p?.root?.trimEnd('/') ?: ""
        // У профиля свои модель, агент, вариант и последняя сессия.
        providerId = prefs.providerId
        modelId = prefs.modelId
        agent = prefs.agent
        variant = prefs.variant
        ensureModelAvailable(models)
        hiddenModels = prefs.hiddenModels
        directory = prefs.directory
        current = null
        messages = emptyList()
        input = ""
        attachments = emptyList()
        pickedMentions = emptyList()
        showMentionPicker = false
        liveAssistantId = null
        liveClear()
        busy = false
        status = ""
        pendingPermission = null
        pendingForm = null
        contextUsed = 0L
        if (screen == Screen.CHAT || screen == Screen.SESSIONS || screen == Screen.FILES) {
            screen = Screen.PROJECTS
        }
        if (screen == Screen.SETTINGS) settingsFrom = Screen.PROJECTS
        loadProjects()
    }

    /**
     * /profiles/api живёт на публичном прокси (там же, где веб-UI), поэтому
     * синхронизацию ведём через публичный адрес, даже если основные запросы
     * идут по Tailscale. Если публичный адрес не задан — через активный.
     */
    fun profilesClient() = ApiClient(
        prefs.baseUrl.trim().trimEnd('/').ifBlank {
            prefs.activeBaseUrl.trim().trimEnd('/')
        },
        prefs.user, prefs.password
    )

    /**
     * Забирает общий список профилей с сервера. При первом синке локальные
     * профили (если они были до этой версии) добавляются на сервер, дальше
     * сервер — источник истины. При ошибке остаётся локальный кэш.
     */
    suspend fun syncProfiles(): Boolean = profileSyncMutex.withLock {
        profileSyncing = true
        try {
            val api = profilesClient()
            val remoteResult = withContext(Dispatchers.IO) {
                runCatching { api.listProfiles() }
            }
            if (remoteResult.isFailure) {
                val msg = remoteResult.exceptionOrNull()?.message
                    ?: "Не удалось загрузить профили"
                profileSyncError = if (msg.contains("404")) {
                    "Сервер не поддерживает /profiles/api — профили хранятся локально"
                } else {
                    msg
                }
                return@withLock false
            }
            var remote = remoteResult.getOrThrow()

            if (!prefs.profilesSynced || prefs.profiles.any { it.id.startsWith("local-") }) {
                val local = prefs.profiles
                val missing = local.filter { lp ->
                    remote.none {
                        it.name == lp.name &&
                            it.root.trimEnd('/') == lp.root.trimEnd('/')
                    }
                }
                if (missing.isNotEmpty()) {
                    val saved = withContext(Dispatchers.IO) {
                        missing.map { lp -> runCatching { api.saveProfile(lp.name, lp.root) } }
                    }
                    val failed = saved.firstOrNull { it.isFailure }
                    if (failed != null) {
                        profileSyncError = failed.exceptionOrNull()?.message
                            ?: "Не удалось сохранить локальные профили"
                        return@withLock false
                    }
                    remote = withContext(Dispatchers.IO) {
                        runCatching { api.listProfiles() }.getOrDefault(remote)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                prefs.saveProfiles(remote)
                prefs.profilesSynced = true
                profiles = prefs.profiles
                val active = prefs.activeProfileId
                if (active.isNotBlank() && remote.none { it.id == active }) {
                    prefs.activeProfileId = ""
                    activeProfileId = ""
                    scopePath = ""
                } else {
                    activeProfileId = prefs.activeProfileId
                    scopePath = remote.firstOrNull { it.id == active }?.root?.trimEnd('/') ?: ""
                }
                profileSyncError = ""
            }
            true
        } finally {
            profileSyncing = false
        }
    }

    fun addProfile(name: String, root: String) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { profilesClient().saveProfile(name, root) }
            }
            val saved = result.getOrNull()
            if (saved == null) {
                // Новый/чужой сервер может не иметь /profiles/api. Профиль
                // всё равно должен работать локально, а при появлении API
                // синхронизация перенесёт его на сервер.
                val local = Profile(
                    "local-" + UUID.randomUUID().toString(),
                    name.trim(),
                    root.trim().trimEnd('/')
                )
                prefs.saveProfiles(prefs.profiles + local)
                profiles = prefs.profiles
                profileSyncError = "Профиль сохранён локально: сервер не поддерживает /profiles/api"
                switchProfile(local.id)
                return@launch
            }
            syncProfiles()
            if (prefs.profiles.none { it.id == saved.id }) {
                prefs.saveProfiles(prefs.profiles + saved)
                profiles = prefs.profiles
            }
            switchProfile(saved.id)
        }
    }

    fun updateProfile(p: Profile) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { profilesClient().saveProfile(p.name, p.root, p.id) }
            }
            val saved = result.getOrNull()
            if (saved == null) {
                if (p.id.startsWith("local-")) {
                    val local = p.copy(name = p.name.trim(), root = p.root.trim().trimEnd('/'))
                    prefs.saveProfiles(prefs.profiles.map { if (it.id == p.id) local else it })
                    profiles = prefs.profiles
                    if (prefs.activeProfileId == p.id) {
                        scopePath = local.root.trimEnd('/')
                        loadProjects()
                    }
                    profileSyncError = "Профиль сохранён локально: сервер не поддерживает /profiles/api"
                } else {
                    profileSyncError = result.exceptionOrNull()?.message
                        ?: "Не удалось сохранить профиль"
                }
                return@launch
            }
            // Если профиль был только локальным, сервер выдал новый id.
            if (prefs.activeProfileId == p.id && saved.id != p.id) {
                prefs.activeProfileId = saved.id
                activeProfileId = saved.id
            }
            if (prefs.profiles.any { it.id == saved.id }) {
                prefs.saveProfiles(prefs.profiles.map { if (it.id == saved.id) saved else it })
            } else if (prefs.profiles.any { it.id == p.id }) {
                prefs.saveProfiles(prefs.profiles.map { if (it.id == p.id) saved else it })
            } else {
                prefs.saveProfiles(prefs.profiles + saved)
            }
            profiles = prefs.profiles
            syncProfiles()
            if (prefs.activeProfileId == saved.id) {
                scopePath = saved.root.trimEnd('/')
                loadProjects()
            }
        }
    }

    fun deleteProfile(id: String) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { profilesClient().deleteProfile(id) }
            }
            if (result.isFailure) {
                if (id.startsWith("local-")) {
                    prefs.saveProfiles(prefs.profiles.filterNot { it.id == id })
                    profiles = prefs.profiles
                    profileSyncError = ""
                    if (prefs.activeProfileId == id) switchProfile("")
                } else {
                    profileSyncError = result.exceptionOrNull()?.message
                        ?: "Не удалось удалить профиль"
                }
                return@launch
            }
            prefs.saveProfiles(prefs.profiles.filterNot { it.id == id })
            profiles = prefs.profiles
            syncProfiles()
            if (prefs.activeProfileId == id) switchProfile("")
        }
    }

    LaunchedEffect(screen) {
        if (screen == Screen.SETTINGS && prefs.isConfigured()) syncProfiles()
    }

    fun openProfileUsage() {
        val root = profiles.firstOrNull { it.id == activeProfileId }?.root?.trimEnd('/') ?: ""
        projectUsageDirectory = root
        projectUsageIsProfile = true
        projectUsage = null
        showProjectUsage = true
        loadProjectUsage()
    }

    fun scopeUp() {
        if (scopePath.isBlank()) return
        val p = parentPath(scopePath).trimEnd('/')
        scopePath = p.ifBlank { "/" }
    }

    fun scopeToProfile() {
        scopePath = profiles.firstOrNull { it.id == activeProfileId }?.root?.trimEnd('/') ?: ""
    }

    /**
     * Если сессия уже выполняется на сервере (например, приложение открыли
     * заново, пока модель ещё думала), показываем спиннер и ждём завершения,
     * затем подтягиваем готовый ответ — без очистки экрана.
     */
    fun watchActive(sessionId: String) {
        watchJob.value?.cancel()
        watchJob.value = scope.launch(Dispatchers.IO) {
            try {
                while (true) {
                    delay(2000)
                    val active = runCatching { client().activeSessions().contains(sessionId) }.getOrNull()
                    if (active != true) break
                }
            } finally {
                val fresh = runCatching { client().recentMessages(sessionId) }.getOrNull()
                withContext(Dispatchers.Main) {
                    if (current?.id == sessionId) {
                        if (fresh != null) messages = mergeHistory(messages, fresh)
                        busy = false
                        checkPending(sessionId)
                        refreshContext(sessionId)
                    }
                }
            }
        }
    }

    fun resumeSession(sessionId: String) {
        if (watchJob.value?.isActive == true) return
        scope.launch {
            val active = withContext(Dispatchers.IO) {
                runCatching { client().activeSessions().contains(sessionId) }.getOrNull()
            }
            if (current?.id != sessionId) return@launch
            if (streamRef.value != null) {
                // Стрим ещё жив. Если сессия уже не активна, значит событие
                // завершения могло потеряться — подтягиваем итог вручную.
                if (active == false) {
                    stopStream()
                    liveClear()
                    val fresh = withContext(Dispatchers.IO) {
                        runCatching { client().recentMessages(sessionId) }.getOrNull()
                    }
                    if (fresh != null) messages = mergeHistory(messages, fresh)
                    busy = false
                }
                return@launch
            }
            if (active == true) {
                busy = true
                watchActive(sessionId)
            } else if (active == false) {
                val fresh = withContext(Dispatchers.IO) {
                    runCatching { client().recentMessages(sessionId) }.getOrNull()
                }
                if (current?.id != sessionId) return@launch
                if (fresh != null) messages = mergeHistory(messages, fresh)
                busy = false
                checkPending(sessionId)
                refreshContext(sessionId)
            }
        }
    }

    fun openSession(s: ChatSession) {
        watchJob.value?.cancel()
        watchJob.value = null
        stopStream()
        current = s
        prefs.lastSessionId = s.id
        prefs.lastSessionTitle = s.title
        screen = Screen.CHAT
        attachments = emptyList()
        pickedMentions = emptyList()
        showMentionPicker = false
        pendingPermission = null
        pendingForm = null
        messages = emptyList()
        historyCursor = null
        historyHasMore = true
        historyLoading = false
        liveClear()
        contextUsed = if (prefs.contextSessionId == s.id) prefs.contextUsed else 0L
        run { messages = withContext(Dispatchers.IO) { client().messages(s.id) } }
        checkPending(s.id)
        refreshContext(s.id)
        scope.launch {
            val active = withContext(Dispatchers.IO) {
                runCatching { client().activeSessions().contains(s.id) }.getOrDefault(false)
            }
            if (active && current?.id == s.id) {
                busy = true
                watchActive(s.id)
            }
        }
    }

    /** Откатить сессию до указанного вопроса (включительно) и перечитать историю. */
    fun revertToMessage(messageId: String) {
        val s = current ?: return
        scope.launch {
            busy = true
            try {
                withContext(Dispatchers.IO) {
                    client().revertStage(s.id, messageId)
                    client().revertCommit(s.id)
                }
                val fresh = withContext(Dispatchers.IO) { client().messages(s.id) }
                messages = fresh
                historyCursor = null
                historyHasMore = true
                historyLoading = false
            } catch (e: Exception) {
                status = e.message ?: "Ошибка"
            } finally {
                busy = false
            }
        }
    }

    /**
     * Короткая сводка настроек профиля для списка в настройках. Для
     * активного профиля берём текущее состояние, для остальных — scoped prefs.
     */
    fun profileSummaryText(p: Profile): String {
        val pid = if (p.id == activeProfileId) providerId else prefs.providerIdFor(p.id)
        val mid = if (p.id == activeProfileId) modelId else prefs.modelIdFor(p.id)
        val ag = if (p.id == activeProfileId) agent else prefs.agentFor(p.id)
        val v = if (p.id == activeProfileId) variant else prefs.variantFor(p.id)
        val hidden = if (p.id == activeProfileId) hiddenModels.size else prefs.hiddenModelsFor(p.id).size
        val vLabel = if (v.isBlank() || v == "default") "Default" else v
        val hiddenLabel = if (hidden > 0) " · скрыто: $hidden" else ""
        return "$pid/$mid · $ag · $vLabel$hiddenLabel"
    }

    /**
     * «Настроить» у профиля: активируем его и открываем последнюю сессию,
     * где модель/агент/вариант меняются в шапке чата. Если сессии нет —
     * остаёмся на проектах профиля.
     */
    fun configureProfile(id: String) {
        if (id != prefs.activeProfileId) switchProfile(id)
        val sid = prefs.lastSessionId
        if (sid.isNotBlank()) {
            openSession(
                ChatSession(
                    sid,
                    prefs.lastSessionTitle.ifBlank { "Сессия" },
                    prefs.directory
                )
            )
        } else {
            screen = Screen.PROJECTS
        }
    }

    fun streamSession(sessionId: String) {
        scope.launch(Dispatchers.IO) {
            val s = runCatching { client().openEventStream() }.getOrNull()
            if (s == null) {
                withContext(Dispatchers.Main) {
                    val fresh = withContext(Dispatchers.IO) { client().recentMessages(sessionId) }
                    messages = mergeHistory(messages, fresh)
                    busy = false
                }
                return@launch
            }
            streamRef.value = s
            var execError = ""
            try {
                while (true) {
                    val line = s.reader.readLine() ?: break
                    if (line.isBlank() || line.startsWith(":")) continue
                    var json = line.trim()
                    if (json.startsWith("data:")) json = json.removePrefix("data:").trim()
                    if (!json.startsWith("{")) continue
                    val obj = runCatching { JSONObject(json) }.getOrNull() ?: continue
                    val type = obj.optString("type")
                    val d = obj.optJSONObject("data") ?: continue
                    val sid = d.optString("sessionID")
                    if (sid.isNotBlank() && sid != sessionId) continue

                    when (type) {
                        "session.text.delta" -> {
                            val delta = d.optString("delta")
                            val key = "t-" + d.optString("assistantMessageID") + "-" + d.optInt("ordinal")
                            if (delta.isNotEmpty()) {
                                withContext(Dispatchers.Main) { liveUpsert(key, TextBlock(liveTextOf(key) + delta)) }
                            }
                        }
                        "session.reasoning.delta" -> {
                            val delta = d.optString("delta")
                            val key = "r-" + d.optString("assistantMessageID") + "-" + d.optInt("ordinal")
                            if (delta.isNotEmpty()) {
                                withContext(Dispatchers.Main) {
                                    val prev = (liveParts[key] as? ReasoningBlock)?.text ?: ""
                                    liveUpsert(key, ReasoningBlock(prev + delta))
                                }
                            }
                        }
                        "session.step.ended" -> {
                            val tk = d.optJSONObject("tokens")
                            if (tk != null) {
                                val cache = tk.optJSONObject("cache")
                                val used = tk.optLong("input") + (cache?.optLong("read") ?: 0L) + (cache?.optLong("write") ?: 0L)
                                withContext(Dispatchers.Main) {
                                    if (used > 0) {
                                        contextUsed = used
                                        prefs.contextSessionId = sessionId
                                        prefs.contextUsed = used
                                    }
                                }
                            }
                            withContext(Dispatchers.Main) {
                                liveClear()
                                val fresh = withContext(Dispatchers.IO) { client().recentMessages(sessionId) }
                                messages = mergeHistory(messages, fresh)
                            }
                        }
                        "session.step.failed" -> {
                            val errText = structuredErrorText(d.optJSONObject("error"))
                            if (errText.isNotBlank()) {
                                val key = "e-" + d.optString("assistantMessageID")
                                withContext(Dispatchers.Main) { liveUpsert(key, ErrorBlock(errText)) }
                            }
                        }
                        "session.execution.succeeded", "session.execution.failed", "session.execution.interrupted" -> {
                            execError = structuredErrorText(d.optJSONObject("error"))
                            withContext(Dispatchers.Main) {
                                liveClear()
                                val fresh = withContext(Dispatchers.IO) { client().recentMessages(sessionId) }
                                messages = mergeHistory(messages, fresh)
                            }
                            break
                        }
                        else -> {
                            if (type.contains("form") || type.contains("permission")) checkPending(sessionId)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { if (busy && status.isBlank()) status = e.message ?: "Ошибка стрима" }
            } finally {
                stopStream()
                withContext(Dispatchers.Main) {
                    liveClear()
                    val loaded = withContext(Dispatchers.IO) { client().recentMessages(sessionId) }
                    val merged = mergeHistory(messages, loaded)
                    messages = if (execError.isNotBlank() && merged.none { m ->
                            m.blocks.any { it is ErrorBlock && it.text == execError }
                        }) {
                        merged + ChatMessage(
                            "err-" + sessionId, "assistant", listOf(ErrorBlock(execError)),
                            created = System.currentTimeMillis()
                        )
                    } else merged
                    busy = false
                }
                checkPending(sessionId)
                refreshContext(sessionId)
            }
        }
    }

    fun copyToClipboard(label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun exportSession() {
        val s = current ?: return
        val sb = StringBuilder()
        sb.append("# ").append(s.title).append("\n\n")
        messages.forEach { m ->
            sb.append(if (m.role == "user") "## Вы\n\n" else "## Ассистент\n\n")
            m.blocks.forEach { b ->
                when (b) {
                    is TextBlock -> sb.append(b.text).append("\n\n")
                    is ReasoningBlock -> sb.append("> ").append(b.text).append("\n\n")
                    is ErrorBlock -> sb.append("⚠ ").append(b.text).append("\n\n")
                    is ToolBlock -> sb.append("*").append(toolLabel(b.name)).append(" ")
                        .append(b.detail).append("*\n\n")
                }
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, s.title)
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        runCatching { context.startActivity(Intent.createChooser(intent, "Экспорт сессии")) }
    }


    fun executeSlash(text: String): Boolean {
        val s = current ?: return false
        if (!text.startsWith("/")) return false
        val name = text.substring(1).substringBefore(' ').trim()
        val args = text.substringAfter(' ', "").trim()
        when (name) {
            "new" -> {
                run {
                    val ns = withContext(Dispatchers.IO) { client().createSession("Новый чат", directory) }
                    sessions = withContext(Dispatchers.IO) { client().listSessions(directory) }
                    openSession(ns)
                }
                return true
            }
            "compact" -> {
                run {
                    withContext(Dispatchers.IO) { client().compactSession(s.id) }
                    messages = withContext(Dispatchers.IO) { client().messages(s.id) }
                    historyCursor = null
                    historyHasMore = true
                }
                return true
            }
            "fork" -> {
                val last = messages.lastOrNull { !it.id.startsWith("local-") }
                run {
                    val ns = withContext(Dispatchers.IO) { client().forkSession(s.id, last?.id) }
                    sessions = withContext(Dispatchers.IO) { client().listSessions(directory) }
                    openSession(ns)
                }
                return true
            }
            "export" -> { exportSession(); return true }
        }
        if (commands.any { it.name == name }) {
            busy = true
            status = ""
            liveAssistantId = null
            liveClear()
            messages = messages + ChatMessage(
                "local-" + System.currentTimeMillis(), "user", listOf(TextBlock(text)),
                created = System.currentTimeMillis()
            )
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        client().sendCommand(s.id, name, args, providerId, modelId, variant, agent)
                    }
                } catch (e: Exception) {
                    status = e.message ?: "Ошибка"
                    busy = false
                    return@launch
                }
                streamSession(s.id)
            }
            return true
        }
        return false
    }


    fun sessionAction(action: SessionAction) {
        val s = current ?: return
        when (action) {
            SessionAction.RENAME -> {
                renameText = s.title
                showRename = true
            }
            SessionAction.UNDO -> {
                val lastUser = messages.lastOrNull { it.role == "user" && !it.id.startsWith("local-") }
                if (lastUser == null) {
                    status = "Нечего отменять"
                } else {
                    run {
                        withContext(Dispatchers.IO) {
                            client().revertStage(s.id, lastUser.id)
                            client().revertCommit(s.id)
                        }
                        messages = withContext(Dispatchers.IO) { client().messages(s.id) }
                        historyCursor = null
                        historyHasMore = true
                    }
                }
            }
            SessionAction.COMPACT -> run {
                withContext(Dispatchers.IO) { client().compactSession(s.id) }
                messages = withContext(Dispatchers.IO) { client().messages(s.id) }
                historyCursor = null
                historyHasMore = true
            }
            SessionAction.FORK -> {
                val last = messages.lastOrNull { !it.id.startsWith("local-") }
                run {
                    val ns = withContext(Dispatchers.IO) { client().forkSession(s.id, last?.id) }
                    sessions = withContext(Dispatchers.IO) { client().listSessions(directory) }
                    openSession(ns)
                }
            }
            SessionAction.BACKGROUND -> {
                scope.launch { runCatching { withContext(Dispatchers.IO) { client().background(s.id) } } }
                stopStream()
                liveClear()
                busy = false
                status = "Задача переведена в фон"
            }
            SessionAction.EXPORT -> exportSession()
            SessionAction.CHANGES -> run {
                changes = withContext(Dispatchers.IO) { runCatching { client().sessionDiff(s.id) }.getOrDefault(emptyList()) }
                showChanges = true
            }
            SessionAction.USAGE -> run {
                usage = withContext(Dispatchers.IO) { runCatching { client().sessionStats(s.id) }.getOrNull() }
                showUsage = true
            }
            SessionAction.DELETE -> confirmDelete = true
        }
    }

    fun pickCommand(name: String) {
        val builtin = setOf("new", "compact", "fork", "export")
        if (name in builtin) {
            input = ""
            executeSlash("/$name")
        } else {
            input = "/$name "
        }
    }

    fun buildMentionFiles(text: String, picked: List<String>, dir: String): List<MentionFile> {
        val out = ArrayList<MentionFile>()
        for (rel in picked.distinct()) {
            if (rel.isBlank()) continue
            val token = "@" + rel
            var from = 0
            while (true) {
                val idx = text.indexOf(token, from)
                if (idx < 0) break
                val before = if (idx == 0) ' ' else text[idx - 1]
                val end = idx + token.length
                val after = if (end >= text.length) ' ' else text[end]
                if (before.isWhitespace() &&
                    (after.isWhitespace() || after == '.' || after == ',' || after == ';' ||
                        after == ')' || after == ']' || after == '}' || after == ':' || after == '!')
                ) {
                    val file = java.io.File(dir, rel)
                    out.add(
                        MentionFile(
                            uri = Uri.fromFile(file).toString(),
                            name = rel,
                            start = idx,
                            end = end,
                            text = token
                        )
                    )
                }
                from = idx + 1
            }
        }
        return out
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) micStartRequested = true
        else status = "Нет разрешения на микрофон"
    }

    fun startMicRecording() {
        if (recording || transcribing) return
        if (!wavRecorder.hasPermission()) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        if (wavRecorder.start()) {
            recording = true
            recordSeconds = 0
            status = ""
        } else {
            status = "Не удалось начать запись"
        }
    }

    fun stopMicRecording() {
        if (!recording) return
        recording = false
        transcribing = true
        status = ""
        scope.launch {
            val wav = withContext(Dispatchers.IO) { wavRecorder.stop() }
            if (wav == null) {
                status = "Слишком короткая запись"
                transcribing = false
                return@launch
            }
            try {
                val text = withContext(Dispatchers.IO) { client().transcribe(wav) }
                if (text.isBlank()) {
                    status = "Не удалось распознать речь"
                } else {
                    input = if (input.isBlank()) text else input.trimEnd() + " " + text
                }
            } catch (e: Exception) {
                status = e.message ?: "Ошибка распознавания"
            } finally {
                transcribing = false
            }
        }
    }

    fun onMicTap() {
        when {
            transcribing -> {}
            recording -> stopMicRecording()
            else -> startMicRecording()
        }
    }

    LaunchedEffect(recording) {
        if (!recording) {
            recordSeconds = 0
            return@LaunchedEffect
        }
        val started = System.currentTimeMillis()
        while (true) {
            delay(500)
            recordSeconds = ((System.currentTimeMillis() - started) / 1000).toInt()
            if (recordSeconds >= WavRecorder.MAX_SECONDS) {
                stopMicRecording()
                break
            }
        }
    }

    LaunchedEffect(micStartRequested) {
        if (micStartRequested) {
            micStartRequested = false
            startMicRecording()
        }
    }

    DisposableEffect(wavRecorder) {
        onDispose { wavRecorder.cancel() }
    }

    fun send() {
        val text = input.trim()
        val s = current ?: return
        if ((text.isBlank() && attachments.isEmpty()) || busy) return
        if (attachments.isEmpty() && executeSlash(text)) {
            input = ""
            pickedMentions = emptyList()
            return
        }
        val sent = attachments
        val mentionFiles = buildMentionFiles(text, pickedMentions, s.directory)
        input = ""
        attachments = emptyList()
        pickedMentions = emptyList()
        busy = true
        status = ""
        liveAssistantId = null
        liveClear()
        messages = messages + ChatMessage(
            "local-" + System.currentTimeMillis(), "user", listOf(TextBlock(text)), sent,
            created = System.currentTimeMillis()
        )
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    client().sendAsync(
                        s.id, text, sent, providerId, modelId, variant, agent,
                        mentions = mentionFiles
                    )
                }
            } catch (e: Exception) {
                status = e.message ?: "Ошибка"
                busy = false
                return@launch
            }
            streamSession(s.id)
        }
    }


    fun stopGeneration() {
        val s = current ?: return
        watchJob.value?.cancel()
        watchJob.value = null
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { client().interrupt(s.id) } }
            withContext(Dispatchers.Main) {
                val fresh = withContext(Dispatchers.IO) { client().recentMessages(s.id) }
                messages = mergeHistory(messages, fresh)
            }
        }
        stopStream()
        liveClear()
        busy = false
    }

    fun respondPermission(response: String) {
        val p = pendingPermission ?: return
        pendingPermission = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { client().respondPermission(p.sessionId, p.id, response) }
            }.onFailure { status = it.message ?: "Ошибка" }
        }
    }

    fun answerForm(answer: JSONObject) {
        val f = pendingForm ?: return
        pendingForm = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { client().replyForm(f.sessionId, f.id, answer) }
            }.onFailure { status = it.message ?: "Ошибка" }
        }
    }

    fun rejectForm() {
        val f = pendingForm ?: return
        pendingForm = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { client().cancelForm(f.sessionId, f.id) }
            }.onFailure { status = it.message ?: "Ошибка" }
        }
    }

    fun createProject() {
        val name = newName.trim()
        val parent = newParent.trim().ifBlank { "/" }
        if (name.isBlank()) return
        val root = profiles.firstOrNull { it.id == activeProfileId }?.root?.trimEnd('/') ?: ""
        if (root.isNotBlank()) {
            val p = parent.trimEnd('/')
            if (p != root && !p.startsWith("$root/")) {
                newProjectError = "Проект должен быть внутри $root"
                return
            }
        }
        newProjectError = ""
        run {
            val path = withContext(Dispatchers.IO) { client().createProject(parent, name) }
            prefs.parentDir = parent
            showNewProject = false
            newName = ""
            projects = withContext(Dispatchers.IO) { collectProjects() }
            directory = path
            prefs.directory = path
            screen = Screen.SESSIONS
            sessions = withContext(Dispatchers.IO) { runCatching { client().listSessions(path) }.getOrDefault(emptyList()) }
            status = "Проект создан: $path"
        }
    }

    LaunchedEffect(Unit) {
        if (!prefs.isConfigured()) return@LaunchedEffect
        launch { syncProfiles() }
        try {
            val connected = resolveConnection()
            if (!connected && status.isBlank()) status = "Сервер недоступен"
            // Списки грузим сразу и параллельно, чтобы не тормозить показ чата.
            val projectsJob = async(Dispatchers.IO) { collectProjects() }
            val modelsJob = async(Dispatchers.IO) {
                runCatching { client().listModels() }.getOrDefault(emptyList())
            }
            val agentsJob = async(Dispatchers.IO) {
                runCatching { client().listAgents() }.getOrDefault(emptyList())
            }
            val commandsJob = async(Dispatchers.IO) {
                runCatching { client().listCommands() }.getOrDefault(emptyList())
            }

            val sid = prefs.lastSessionId
            when (screen) {
                Screen.SESSIONS -> {
                    directory = prefs.directory
                    sessions = withContext(Dispatchers.IO) {
                        runCatching { client().listSessions(directory) }.getOrDefault(emptyList())
                    }
                }
                Screen.CHAT -> {
                    if (sid.isNotBlank()) {
                        directory = prefs.directory
                        current = ChatSession(sid, prefs.lastSessionTitle.ifBlank { "Сессия" }, prefs.directory)
                        historyCursor = null
                        historyHasMore = true
                        historyLoading = false
                        // Активность и последнюю историю запрашиваем параллельно.
                        val activeJob = async(Dispatchers.IO) {
                            runCatching { client().activeSessions().contains(sid) }.getOrDefault(false)
                        }
                        val loadedJob = async(Dispatchers.IO) {
                            runCatching { client().messages(sid) }
                        }
                        val active = activeJob.await()
                        if (active) {
                            busy = true
                            watchActive(sid)
                        }
                        // Кэш уже показан; тихо обновляем историю с сервера, не очищая экран.
                        val loaded = loadedJob.await()
                        if (loaded.isSuccess) {
                            // Не перетираем уже загруженный финальный результат,
                            // если наблюдение успело завершиться.
                            if (!active || watchJob.value?.isActive == true) {
                                messages = mergeHistory(messages, loaded.getOrThrow())
                            }
                        } else if (loaded.exceptionOrNull()?.message?.contains("HTTP 404") == true) {
                            watchJob.value?.cancel()
                            watchJob.value = null
                            ChatCache.clear(context)
                            prefs.lastSessionId = ""
                            screen = if (prefs.directory.isNotBlank()) Screen.SESSIONS else Screen.PROJECTS
                            return@LaunchedEffect
                        }
                        checkPending(sid)
                        refreshContext(sid)
                    } else {
                        screen = if (prefs.directory.isNotBlank()) Screen.SESSIONS else Screen.PROJECTS
                    }
                }
                else -> {}
            }

            projects = projectsJob.await()
            models = modelsJob.await()
            ensureModelAvailable(models)
            agents = agentsJob.await()
            commands = commandsJob.await()
        } catch (e: Exception) {
            // Не сбрасываем экран и последнюю сессию: показываем то, что есть,
            // и просто сообщаем об ошибке.
            status = e.message ?: "Ошибка"
        }
    }

    val activity = context.findMainActivity()
    DisposableEffect(activity) {
        activity?.resumeHandler = {
            scope.launch {
                resolveConnection()
                if (screen == Screen.CHAT) {
                    current?.let { s -> resumeSession(s.id) }
                }
            }
        }
        onDispose { activity?.resumeHandler = null }
    }

    val liveBlocks: List<Block> = liveOrder.mapNotNull { liveParts[it] }
    val contextLimit = models.firstOrNull { it.providerId == providerId && it.modelId == modelId }?.contextLimit ?: 0L
    val visibleModels = models.filter {
        val key = it.providerId + "/" + it.modelId
        key !in hiddenModels || (it.providerId == providerId && it.modelId == modelId)
    }
    val visibleProjects = projects.filter { p ->
        if (scopePath.isBlank()) true
        else {
            val s = scopePath.trimEnd('/')
            p.worktree.trimEnd('/') == s || p.worktree.startsWith("$s/")
        }
    }
    val activeProfile = profiles.firstOrNull { it.id == activeProfileId }

    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    SideEffect {
        val activityWindow = activity?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(activityWindow, activityWindow.decorView)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
    }
    val chosenFont = fontFamilyFor(fontName)
    val baseTypography = remember { Typography() }
    val typography = remember(chosenFont) { baseTypography.withFontFamily(chosenFont) }
    val baseDensity = LocalDensity.current

    CompositionLocalProvider(
        LocalDensity provides Density(baseDensity.density, baseDensity.fontScale * fontScale)
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
            typography = typography
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = chosenFont)
            ) {
                Surface(Modifier.fillMaxSize().imePadding(), color = MaterialTheme.colorScheme.background) {
                    when (screen) {
                Screen.SETTINGS -> SettingsScreen(
                    baseUrl = baseUrl, user = user, password = password,
                    busy = busy, status = status,
                    tailscaleUrl = tailscaleUrl, connectionMode = connectionMode,
                    speechUrl = speechUrl, activeBaseUrl = activeBaseUrl,
                    defaultRoot = defaultRoot,
                    themeMode = themeMode, fontName = fontName, fontScale = fontScale,
                    profiles = profiles, activeProfileId = activeProfileId,
                    profileSyncError = profileSyncError,
                    profileSyncing = profileSyncing,
                    profileSummary = { p -> profileSummaryText(p) },
                    onRefreshProfiles = { scope.launch { syncProfiles() } },
                    onConfigureProfile = { configureProfile(it) },
                    onActivateProfile = { switchProfile(it) },
                    onAddProfile = { n, r -> addProfile(n, r) },
                    onUpdateProfile = { updateProfile(it) },
                    onDeleteProfile = { deleteProfile(it) },
                    onThemeMode = { themeMode = it; prefs.themeMode = it },
                    onFontName = { fontName = it; prefs.fontFamily = it },
                    onFontScale = { fontScale = it; prefs.fontScale = it },
                    onTailscaleUrl = { tailscaleUrl = it },
                    onConnectionMode = { connectionMode = it },
                    onSpeechUrl = { speechUrl = it },
                    onDefaultRoot = { defaultRoot = it },
                    onBack = if (prefs.isConfigured()) ({ screen = settingsFrom }) else null,
                    onBaseUrl = { baseUrl = it }, onUser = { user = it }, onPassword = { password = it },
                    onSave = {
                        prefs.baseUrl = baseUrl.trim()
                        prefs.tailscaleUrl = tailscaleUrl.trim()
                        prefs.connectionMode = connectionMode
                        prefs.speechUrl = speechUrl.trim()
                        prefs.defaultRoot = defaultRoot
                        if (prefs.parentDir.isBlank() && prefs.defaultRoot.isNotBlank()) {
                            prefs.parentDir = prefs.defaultRoot
                        }
                        prefs.user = user.trim()
                        prefs.password = password
                        prefs.activeBaseUrl = ""
                        activeBaseUrl = ""
                        run {
                            val ok = resolveConnection()
                            if (!ok) {
                                status = "Сервер недоступен"
                            } else {
                                val health = withContext(Dispatchers.IO) { client().health() }
                                projects = withContext(Dispatchers.IO) { collectProjects() }
                                status = "Связь есть: $health (${prefs.activeBaseUrl})"
                                screen = Screen.PROJECTS
                                loadModels()
                            }
                        }
                    }
                )

                Screen.PROJECTS -> ProjectsScreen(
                    projects = visibleProjects, busy = busy, status = status,
                    profileName = activeProfile?.name ?: "Все проекты",
                    profiles = profiles, activeProfileId = activeProfileId, scopePath = scopePath,
                    onOpen = { openProject(it) },
                    onNew = {
                        newParent = (activeProfile?.root?.ifBlank { scopePath } ?: scopePath)
                            .ifBlank { prefs.defaultRoot }
                            .ifBlank { prefs.parentDir }
                        newProjectError = ""
                        showNewProject = true
                    },
                    onReload = { loadProjects() },
                    onSelectProfile = { switchProfile(it) },
                    onOpenProfiles = { settingsFrom = Screen.PROJECTS; screen = Screen.SETTINGS },
                    onScopeUp = { scopeUp() },
                    onScopeRoot = { scopeToProfile() },
                    // «Все проекты» — это не просто сброс области, а выход
                    // из профиля: чип профиля должен смениться на «Все проекты».
                    onAllProjects = { switchProfile("") },
                    onUsage = { openProfileUsage() },
                    onSettings = { settingsFrom = Screen.PROJECTS; screen = Screen.SETTINGS }
                )

                Screen.SESSIONS -> SessionsScreen(
                    title = projectLabel(projects, directory),
                    subtitle = directory,
                    sessions = sessions, busy = busy, status = status,
                    onOpen = { openSession(it) },
                    onNew = {
                        run {
                            val s = withContext(Dispatchers.IO) { client().createSession("Новый чат", directory) }
                            sessions = withContext(Dispatchers.IO) { client().listSessions(directory) }
                            openSession(s)
                        }
                    },
                    onReload = { loadSessions() },
                    onUsage = { openProjectUsage() },
                    onFiles = { filesReturnScreen = Screen.SESSIONS; screen = Screen.FILES },
                    onBack = { stopStream(); screen = Screen.PROJECTS; loadProjects() },
                    onSettings = { settingsFrom = Screen.SESSIONS; screen = Screen.SETTINGS }
                )

                Screen.CHAT -> ChatScreen(
                    title = current?.title ?: "Чат",
                    messages = messages, liveBlocks = if (busy) liveBlocks else emptyList(), busy = busy, status = status,
                    input = input, attachments = attachments,
                    models = visibleModels, providerId = providerId, modelId = modelId,
                    variant = variant,
                    contextUsed = contextUsed, contextLimit = contextLimit,
                    commands = commands,
                    recording = recording, recordSeconds = recordSeconds, transcribing = transcribing,
                    onMic = { onMicTap() },
                    connectionKind = activeKind,
                    profileName = activeProfile?.name ?: "Все проекты",
                    sessionId = current?.id.orEmpty(),
                    historyLoading = historyLoading,
                    historyHasMore = historyHasMore,
                    onLoadOlder = { loadOlderMessages() },
                    onRevertMessage = { id -> revertToMessage(id) },
                    onInput = { v ->
                        input = v
                        if (v.endsWith("@") && !showMentionPicker) showMentionPicker = true
                    },
                    onSend = { send() },
                    onStop = { stopGeneration() },
                    onOpenModelPicker = { modelQuery = ""; showModelPicker = true },
                    onSessionAction = { sessionAction(it) },
                    onPickCommand = { pickCommand(it) },
                    onSelectVariant = { variant = it; prefs.variant = it },
                    onAttach = { uri -> readAttachment(uri) },
                    onRemoveAttachment = { idx -> attachments = attachments.filterIndexed { i, _ -> i != idx } },
                    onOpenFiles = { filesReturnScreen = Screen.CHAT; screen = Screen.FILES },
                    onBack = { stopStream(); screen = Screen.SESSIONS; loadSessions() },
                    onSettings = { stopStream(); settingsFrom = Screen.CHAT; screen = Screen.SETTINGS }
                )

                Screen.FILES -> FilesScreen(
                    directory = current?.directory?.ifBlank { directory } ?: directory,
                    baseUrl = activeBaseUrl.ifBlank { prefs.baseUrl },
                    user = prefs.user, password = prefs.password,
                    onBack = { screen = filesReturnScreen }
                )
            }
        }

    if (showNewProject) {
        NewProjectDialog(
            parent = newParent, name = newName, busy = busy, error = newProjectError,
            onParent = { newParent = it; newProjectError = "" },
            onName = { newName = it },
            onDismiss = { if (!busy) { showNewProject = false; newProjectError = "" } },
            onCreate = { createProject() }
        )
    }

    if (pendingPermission != null) {
        PermissionDialog(
            permission = pendingPermission!!,
            busy = busy,
            onOnce = { respondPermission("once") },
            onAlways = { respondPermission("always") },
            onReject = { respondPermission("reject") }
        )
    }

    if (pendingForm != null) {
        FormDialog(
            form = pendingForm!!,
            onSubmit = { answerForm(it) },
            onCancel = { rejectForm() }
        )
    }

    if (showModelPicker) {
        ModelPickerDialog(
            models = visibleModels,
            providerId = providerId,
            modelId = modelId,
            query = modelQuery,
            onQuery = { modelQuery = it },
            onDismiss = { showModelPicker = false },
            onManage = { showModelPicker = false; showManageModels = true },
            onPick = { pendingModel = it; showModelPicker = false }
        )
    }

    if (showManageModels) {
        ModelManageDialog(
            models = models,
            hidden = hiddenModels,
            onToggle = { mi ->
                val key = mi.providerId + "/" + mi.modelId
                hiddenModels = if (key in hiddenModels) hiddenModels - key else hiddenModels + key
                prefs.hiddenModels = hiddenModels
            },
            onShowAll = { hiddenModels = emptySet(); prefs.hiddenModels = hiddenModels },
            onHideAll = { hiddenModels = models.map { it.providerId + "/" + it.modelId }.toSet(); prefs.hiddenModels = hiddenModels },
            onDismiss = { showManageModels = false }
        )
    }

    if (pendingModel != null) {
        val mi = pendingModel!!
        AlertDialog(
            onDismissRequest = { pendingModel = null },
            title = { Text("Сменить модель?") },
            text = { Text("Сменить модель на «${mi.name}»?") },
            confirmButton = {
                TextButton(onClick = {
                    providerId = mi.providerId
                    modelId = mi.modelId
                    prefs.providerId = mi.providerId
                    prefs.modelId = mi.modelId
                    if (variant.isNotBlank() && variant !in mi.variants) {
                        variant = ""
                        prefs.variant = ""
                    }
                    pendingModel = null
                }) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { pendingModel = null }) { Text("Нет") }
            }
        )
    }

    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text("Переименовать сессию") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val s = current
                    val newTitle = renameText.trim()
                    showRename = false
                    if (s != null && newTitle.isNotBlank()) {
                        run {
                            withContext(Dispatchers.IO) { client().renameSession(s.id, newTitle) }
                            current = s.copy(title = newTitle)
                            prefs.lastSessionTitle = newTitle
                            sessions = withContext(Dispatchers.IO) { client().listSessions(directory) }
                        }
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { showRename = false }) { Text("Отмена") } }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить сессию?") },
            text = { Text("Сессия «${current?.title ?: ""}» будет удалена безвозвратно.") },
            confirmButton = {
                TextButton(onClick = {
                    val s = current
                    confirmDelete = false
                    if (s != null) {
                        run {
                            withContext(Dispatchers.IO) { client().deleteSession(s.id) }
                            if (prefs.lastSessionId == s.id) prefs.lastSessionId = ""
                            ChatCache.clear(context)
                            stopStream()
                            current = null
                            screen = Screen.SESSIONS
                            loadSessions()
                        }
                    }
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Отмена") } }
        )
    }

    if (showUsage) {
        UsageDialog(usage = usage, contextUsed = contextUsed, contextLimit = contextLimit, onDismiss = { showUsage = false })
    }

    if (showProjectUsage) {
        val profileRoot = activeProfile?.root?.trimEnd('/') ?: ""
        ProjectUsageDialog(
            usage = projectUsage,
            directory = if (projectUsageIsProfile) profileRoot.ifBlank { "Все проекты" } else directory,
            title = if (projectUsageIsProfile) "Использование профиля" else "Использование проекта",
            subject = if (projectUsageIsProfile) "Профиль" else "Проект",
            period = projectUsagePeriod,
            customStart = projectUsageCustomStart,
            customEnd = projectUsageCustomEnd,
            loading = projectUsageLoading,
            onPeriod = { period ->
                projectUsagePeriod = period
                loadProjectUsage()
            },
            onCustomStart = { ms ->
                projectUsageCustomStart = ms
                loadProjectUsage()
            },
            onCustomEnd = { ms ->
                projectUsageCustomEnd = ms
                loadProjectUsage()
            },
            onDismiss = { showProjectUsage = false }
        )
    }

    if (showMentionPicker) {
        MentionPickerDialog(
            directory = current?.directory?.ifBlank { directory } ?: directory,
            baseUrl = activeBaseUrl.ifBlank { prefs.baseUrl },
            user = prefs.user, password = prefs.password,
            onDismiss = { showMentionPicker = false },
            onPick = { path ->
                input = if (input.endsWith("@")) input.dropLast(1) + "@" + path + " "
                else input + "@" + path + " "
                pickedMentions = (pickedMentions + path).distinct()
                showMentionPicker = false
            }
        )
    }

    if (showChanges) {
        ChangesDialog(changes = changes, onDismiss = { showChanges = false })
    }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(
    baseUrl: String, user: String, password: String, busy: Boolean, status: String,
    tailscaleUrl: String, connectionMode: String, speechUrl: String, activeBaseUrl: String,
    defaultRoot: String,
    themeMode: String, fontName: String, fontScale: Float,
    profiles: List<Profile>, activeProfileId: String,
    profileSyncError: String, profileSyncing: Boolean,
    profileSummary: (Profile) -> String,
    onRefreshProfiles: () -> Unit,
    onConfigureProfile: (String) -> Unit,
    onActivateProfile: (String) -> Unit,
    onAddProfile: (String, String) -> Unit,
    onUpdateProfile: (Profile) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onThemeMode: (String) -> Unit, onFontName: (String) -> Unit, onFontScale: (Float) -> Unit,
    onTailscaleUrl: (String) -> Unit, onConnectionMode: (String) -> Unit, onSpeechUrl: (String) -> Unit,
    onDefaultRoot: (String) -> Unit,
    onBack: (() -> Unit)?,
    onBaseUrl: (String) -> Unit, onUser: (String) -> Unit, onPassword: (String) -> Unit,
    onSave: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<Profile?>(null) }
    var confirmDeleteProfile by remember { mutableStateOf<Profile?>(null) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Настройки") },
            navigationIcon = {
                if (onBack != null) TextButton(onClick = onBack) { Text("← Назад") }
            }
        )
    }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (baseUrl.isBlank() && tailscaleUrl.isBlank()) {
                Text(
                    "Первый запуск. Укажите адрес сервера opencode, логин и пароль. " +
                        "Адрес по Tailscale нужен только при подключении через Tailscale.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
            }
            Text("Оформление", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            Text("Тема", fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("system" to "Авто", "light" to "Светлая", "dark" to "Тёмная").forEach { (value, label) ->
                    FilterChip(
                        selected = themeMode == value,
                        onClick = { onThemeMode(value) },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Шрифт", fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "system" to "Системный", "sans" to "Sans",
                    "serif" to "Lora", "mono" to "Roboto Mono"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = fontName == value,
                        onClick = { onFontName(value) },
                        label = { Text(label) }
                    )
                }
            }
            Text(
                "Пример: Съешь ещё этих мягких французских булок",
                fontFamily = fontFamilyFor(fontName),
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Размер шрифта: ${(fontScale * 100).roundToInt()}%",
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = fontScale,
                onValueChange = onFontScale,
                valueRange = 0.8f..1.4f,
                steps = 5
            )
            Spacer(Modifier.height(20.dp))
            Text("Подключение", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            Text("Режим", fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "auto" to "Авто", "public" to "Публичный", "tailscale" to "Tailscale"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = connectionMode == value,
                        onClick = { onConnectionMode(value) },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Публичный адрес", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                baseUrl, onBaseUrl, Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("https://opencode.example.com") }
            )
            Spacer(Modifier.height(10.dp))
            Text("Адрес по Tailscale", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                tailscaleUrl, onTailscaleUrl, Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("http://100.x.x.x:4096") }
            )
            Spacer(Modifier.height(10.dp))
            Text("Адрес распознавания речи (необязательно)", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                speechUrl, onSpeechUrl, Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("пусто — определится автоматически") }
            )
            Spacer(Modifier.height(10.dp))
            Text("Корень проектов (необязательно)", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                defaultRoot, onDefaultRoot, Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("/home/пользователь/проекты") }
            )
            Text(
                "Отсюда создаются новые проекты, если у профиля не задана своя папка. " +
                    "Пусто — /.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (activeBaseUrl.isBlank()) "Сейчас: ещё не определён" else "Сейчас: $activeBaseUrl",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Профили", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = onRefreshProfiles,
                    enabled = !profileSyncing
                ) { Text(if (profileSyncing) "Обновление…" else "Обновить") }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Имя и папка общие с веб-интерфейсом. Модель, агент и вариант — локальные; «Настроить» открывает чат с нужными настройками. Логин общий.",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (profileSyncError.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Синхронизация: $profileSyncError",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(6.dp))
            if (profiles.isEmpty()) {
                Text(
                    "Профилей пока нет. Добавьте, например, «Алиса» и «Боб» и укажите каждому свою папку.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            profiles.forEach { p ->
                Row(
                    Modifier.fillMaxWidth()
                        .clickable { onActivateProfile(p.id) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = p.id == activeProfileId,
                        onClick = { onActivateProfile(p.id) }
                    )
                    Column(Modifier.weight(1f)) {
                        Text(p.name, fontWeight = FontWeight.Medium)
                        Text(
                            p.root.ifBlank { "все проекты" },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            profileSummary(p),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        TextButton(
                            onClick = { onConfigureProfile(p.id) },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.heightIn(min = 0.dp)
                        ) {
                            Text("Настроить", fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = { editingProfile = p; showProfileDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Изменить профиль")
                    }
                    IconButton(onClick = { confirmDeleteProfile = p }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Удалить профиль")
                    }
                }
            }
            TextButton(
                onClick = { editingProfile = null; showProfileDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("+ Добавить профиль") }
            Spacer(Modifier.height(20.dp))
            Text("Сервер", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            Text("Логин", fontWeight = FontWeight.Bold)
            OutlinedTextField(user, onUser, Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            Text("Пароль", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = password,
                onValueChange = onPassword,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "Скрыть пароль" else "Показать пароль"
                        )
                    }
                }
            )
            Text(
                "Пусто — если сервер без авторизации.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onSave, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text(if (busy) "Проверка…" else "Сохранить и проверить связь")
            }
            if (status.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(status, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    if (showProfileDialog) {
        ProfileEditDialog(
            initial = editingProfile,
            defaultRoot = defaultRoot,
            onDismiss = { showProfileDialog = false },
            onSave = { name, root ->
                val e = editingProfile
                if (e == null) onAddProfile(name, root)
                else onUpdateProfile(e.copy(name = name, root = root))
                showProfileDialog = false
            }
        )
    }

    confirmDeleteProfile?.let { p ->
        AlertDialog(
            onDismissRequest = { confirmDeleteProfile = null },
            title = { Text("Удалить профиль?") },
            text = { Text("Профиль «${p.name}» будет удалён. Проекты и сессии на сервере останутся.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteProfile(p.id)
                    confirmDeleteProfile = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteProfile = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun ProfileEditDialog(
    initial: Profile?,
    defaultRoot: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var root by remember(initial?.id, defaultRoot) {
        mutableStateOf(initial?.root ?: if (initial == null) defaultRoot else "")
    }
    val rootValid = root.isBlank() || root.trim().startsWith("/")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новый профиль" else "Профиль") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Имя") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = root,
                    onValueChange = { root = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Папка проектов") },
                    placeholder = { Text("/home/пользователь/проекты") },
                    isError = !rootValid
                )
                Text(
                    if (!rootValid) "Нужен абсолютный путь, например /home/пользователь/проекты"
                    else "Папка создастся при первом проекте. Пусто — все проекты.",
                    fontSize = 11.sp,
                    color = if (!rootValid) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), root.trim()) },
                enabled = name.isNotBlank() && rootValid
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectsScreen(
    projects: List<Project>, busy: Boolean, status: String,
    profileName: String, profiles: List<Profile>, activeProfileId: String, scopePath: String,
    onOpen: (Project) -> Unit, onNew: () -> Unit, onReload: () -> Unit, onSettings: () -> Unit,
    onSelectProfile: (String) -> Unit, onOpenProfiles: () -> Unit,
    onScopeUp: () -> Unit, onScopeRoot: () -> Unit, onAllProjects: () -> Unit,
    onUsage: () -> Unit
) {
    var profileMenu by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Проекты") },
                actions = { TextButton(onClick = onSettings) { Text("Настройки") } }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    AssistChip(
                        onClick = { profileMenu = true },
                        label = {
                            Text(
                                profileName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(Icons.Filled.ExpandMore, contentDescription = null)
                        }
                    )
                    DropdownMenu(expanded = profileMenu, onDismissRequest = { profileMenu = false }) {
                        profiles.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (p.id == activeProfileId) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.size(6.dp))
                                        }
                                        Column {
                                            Text(p.name)
                                            if (p.root.isNotBlank()) {
                                                Text(
                                                    p.root,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    profileMenu = false
                                    onSelectProfile(p.id)
                                }
                            )
                        }
                        if (profiles.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Профили не настроены") },
                                onClick = {
                                    profileMenu = false
                                    onOpenProfiles()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (activeProfileId.isBlank()) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.size(6.dp))
                                    }
                                    Text("Все проекты")
                                }
                            },
                            onClick = {
                                profileMenu = false
                                onAllProjects()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Использование профиля") },
                            onClick = {
                                profileMenu = false
                                onUsage()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Настроить профили") },
                            onClick = {
                                profileMenu = false
                                onOpenProfiles()
                            }
                        )
                    }
                }
                if (activeProfileId.isNotBlank()) {
                    if (scopePath.isNotBlank()) {
                        IconButton(
                            onClick = onScopeUp,
                            enabled = scopePath.trimEnd('/').isNotBlank()
                        ) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "На уровень выше")
                        }
                    }
                    TextButton(onClick = onScopeRoot) { Text("Профиль") }
                }
                IconButton(onClick = onUsage) {
                    Icon(Icons.Filled.BarChart, contentDescription = "Использование профиля")
                }
            }
            Text(
                if (scopePath.isBlank()) "Все проекты" else scopePath,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Button(onClick = onNew, enabled = !busy, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("+ Новый проект")
            }
            if (busy) Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(28.dp)) }
            if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
            if (projects.isEmpty() && !busy) {
                Text("Проектов нет. Создайте новый.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(projects, key = { it.worktree }) { p ->
                    Column(
                        Modifier.fillMaxWidth().clickable { onOpen(p) }.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(p.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(p.worktree, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            TextButton(onClick = onReload, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Обновить проекты") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SessionsScreen(
    title: String, subtitle: String,
    sessions: List<ChatSession>, busy: Boolean, status: String,
    onOpen: (ChatSession) -> Unit, onNew: () -> Unit, onReload: () -> Unit,
    onUsage: () -> Unit, onFiles: () -> Unit,
    onBack: () -> Unit, onSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, maxLines = 1)
                        if (subtitle.isNotBlank()) Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Проекты") } },
                actions = {
                    IconButton(onClick = onFiles) { Icon(Icons.Filled.Folder, contentDescription = "Файлы проекта") }
                    IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = "Настройки") }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            FlowRow(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onNew, enabled = !busy, modifier = Modifier.weight(1f)) {
                    Text("+ Новый чат", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(onClick = onUsage, enabled = !busy && sessions.isNotEmpty()) {
                    Text("Использование", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (busy) Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(28.dp)) }
            if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(sessions, key = { it.id }) { s ->
                    Column(
                        Modifier.fillMaxWidth().clickable { onOpen(s) }.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(s.title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            TextButton(onClick = onReload, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Обновить список") }
        }
    }
}

private fun parentPath(path: String): String {
    val p = path.trimEnd('/')
    val i = p.lastIndexOf('/')
    return if (i < 0) "" else p.substring(0, i + 1)
}

private fun queryFileName(context: Context, uri: Uri): String {
    var name = uri.lastPathSegment ?: "file"
    runCatching {
        context.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) c.getString(idx)?.let { if (it.isNotBlank()) name = it }
            }
        }
    }
    return name.replace('/', '_').replace("..", "").ifBlank { "file" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilesScreen(
    directory: String,
    baseUrl: String, user: String, password: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { ApiClient(baseUrl, user, password) }
    var path by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf<List<FsEntry>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var preview by remember { mutableStateOf<FsRead?>(null) }
    var previewName by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<FsEntry?>(null) }
    var renameTarget by remember { mutableStateOf<FsEntry?>(null) }
    var renameText by remember { mutableStateOf("") }
    var entryMenuPath by remember { mutableStateOf<String?>(null) }
    var newFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var editTarget by remember { mutableStateOf<FsEntry?>(null) }
    var editText by remember { mutableStateOf("") }
    var editOriginal by remember { mutableStateOf("") }
    var editSaving by remember { mutableStateOf(false) }
    var editError by remember { mutableStateOf("") }
    var pendingDownload by remember { mutableStateOf<FsEntry?>(null) }

    fun fileNameOf(p: String): String = p.trimEnd('/').substringAfterLast('/')

    fun refresh() {
        scope.launch {
            loading = true
            runCatching { withContext(Dispatchers.IO) { api.fsList(directory, path) } }
                .onSuccess { entries = it }
                .onFailure { status = it.message ?: "Ошибка" }
            loading = false
        }
    }

    fun search() {
        scope.launch {
            loading = true
            runCatching { withContext(Dispatchers.IO) { api.fsFind(directory, query, "file", 100) } }
                .onSuccess { entries = it }
                .onFailure { status = it.message ?: "Ошибка" }
            loading = false
        }
    }

    fun reload() {
        if (query.isBlank()) refresh() else search()
    }

    val createDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val target = pendingDownload
        pendingDownload = null
        if (uri != null && target != null) {
            scope.launch {
                loading = true
                runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            api.fsDownload(directory, target.path, out)
                        } ?: throw RuntimeException("Не удалось открыть файл")
                    }
                    status = "Сохранено: ${fileNameOf(target.path)}"
                }.onFailure { status = it.message ?: "Ошибка скачивания" }
                loading = false
            }
        }
    }

    LaunchedEffect(path, query) {
        if (query.isBlank()) refresh() else {
            delay(300)
            search()
        }
    }

    fun saveToDownloads(target: FsEntry) {
        val name = fileNameOf(target.path)
        val mime = fileMime(name)
        if (Build.VERSION.SDK_INT >= 29) {
            scope.launch {
                loading = true
                status = ""
                runCatching {
                    withContext(Dispatchers.IO) {
                        val resolver = context.contentResolver
                        val values = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                            put(MediaStore.MediaColumns.MIME_TYPE, mime)
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }
                        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                            ?: throw RuntimeException("Не удалось создать файл в «Загрузках»")
                        try {
                            resolver.openOutputStream(uri)?.use { out ->
                                api.fsDownload(directory, target.path, out)
                            } ?: throw RuntimeException("Не удалось открыть поток записи")
                            val done = ContentValues().apply {
                                put(MediaStore.MediaColumns.IS_PENDING, 0)
                            }
                            resolver.update(uri, done, null, null)
                        } catch (e: Exception) {
                            resolver.delete(uri, null, null)
                            throw e
                        }
                    }
                    status = "Сохранено в «Загрузки»: $name"
                }.onFailure { status = it.message ?: "Ошибка скачивания" }
                loading = false
            }
        } else {
            pendingDownload = target
            createDocLauncher.launch(name)
        }
    }

    fun openEditor(target: FsEntry) {
        scope.launch {
            loading = true
            status = ""
            runCatching { withContext(Dispatchers.IO) { api.fsRead(directory, target.path) } }
                .onSuccess { r ->
                    val t = r.text
                    when {
                        t == null -> status = "Это не текстовый файл — редактор не откроется"
                        r.size > 1_000_000 -> status = "Файл больше 1 МБ — редактор не откроется"
                        else -> {
                            editTarget = target
                            editText = t
                            editOriginal = t
                            editError = ""
                        }
                    }
                }
                .onFailure { status = it.message ?: "Ошибка чтения" }
            loading = false
        }
    }

    fun saveEditor() {
        val target = editTarget ?: return
        editSaving = true
        editError = ""
        scope.launch {
            runCatching {
                val bytes = editText.toByteArray(Charsets.UTF_8)
                withContext(Dispatchers.IO) {
                    ByteArrayInputStream(bytes).use { input ->
                        api.fsWrite(directory, target.path, input, bytes.size.toLong())
                    }
                }
            }.onSuccess {
                editTarget = null
                status = "Сохранено: ${fileNameOf(target.path)}"
                reload()
            }.onFailure { editError = it.message ?: "Ошибка сохранения" }
            editSaving = false
        }
    }

    val uploadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                loading = true
                status = ""
                try {
                    val name = queryFileName(context, uri)
                    val target = path + name
                    withContext(Dispatchers.IO) {
                        val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            api.fsWrite(directory, target, input, size)
                        } ?: throw RuntimeException("Не удалось открыть файл")
                    }
                    status = "Загружено: $target"
                    loading = false
                    reload()
                } catch (e: Exception) {
                    status = e.message ?: "Ошибка загрузки"
                    loading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Файлы проекта", maxLines = 1)
                        Text(
                            if (path.isBlank()) directory else path,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Назад") } },
                actions = {
                    IconButton(onClick = { uploadLauncher.launch("*/*") }, enabled = !loading) {
                        Icon(Icons.Filled.UploadFile, contentDescription = "Загрузить файл")
                    }
                    IconButton(
                        onClick = { newFolderName = ""; newFolder = true },
                        enabled = !loading
                    ) {
                        Icon(Icons.Filled.CreateNewFolder, contentDescription = "Создать папку")
                    }
                    IconButton(onClick = { reload() }, enabled = !loading) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                placeholder = { Text("Поиск по файлам проекта") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Очистить")
                        }
                    }
                }
            )
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (status.isNotBlank()) {
                Text(
                    status,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
            if (query.isBlank() && path.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().clickable { path = parentPath(path) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.size(10.dp))
                    Text("..", fontSize = 14.sp)
                }
            }
            val shown = if (query.isBlank()) {
                entries.sortedWith(compareBy({ it.type != "directory" }, { it.path.lowercase() }))
            } else entries
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(shown, key = { it.type + ":" + it.path }) { e ->
                    val isDir = e.type == "directory"
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            if (isDir) {
                                path = e.path
                                query = ""
                            } else {
                                scope.launch {
                                    loading = true
                                    status = ""
                                    runCatching { withContext(Dispatchers.IO) { api.fsRead(directory, e.path) } }
                                        .onSuccess { preview = it; previewName = e.path }
                                        .onFailure { status = it.message ?: "Ошибка чтения" }
                                    loading = false
                                }
                            }
                        }.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isDir) Icons.Filled.Folder else Icons.Filled.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(10.dp))
                        Text(
                            if (query.isBlank()) e.path.removePrefix(path) else e.path,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Box {
                            IconButton(onClick = { entryMenuPath = e.path }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "Действия",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = entryMenuPath == e.path,
                                onDismissRequest = { entryMenuPath = null }
                            ) {
                                if (!isDir) {
                                    DropdownMenuItem(
                                        text = { Text("Скачать на телефон") },
                                        onClick = {
                                            entryMenuPath = null
                                            saveToDownloads(e)
                                        }
                                    )
                                    if (api.isTextLike("", e.path)) {
                                        DropdownMenuItem(
                                            text = { Text("Редактировать") },
                                            onClick = {
                                                entryMenuPath = null
                                                openEditor(e)
                                            }
                                        )
                                    }
                                }
                                DropdownMenuItem(
                                    text = { Text("Переименовать") },
                                    onClick = {
                                        entryMenuPath = null
                                        renameTarget = e
                                        renameText = e.path.trimEnd('/').substringAfterLast('/')
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isDir) "Удалить папку" else "Удалить файл") },
                                    onClick = {
                                        entryMenuPath = null
                                        deleteTarget = e
                                    }
                                )
                            }
                        }
                    }
                }
                if (shown.isEmpty() && !loading) {
                    item {
                        Text(
                            "Пусто",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (preview != null) {
        FilePreviewDialog(
            name = previewName,
            data = preview!!,
            onDismiss = { preview = null },
            onDownload = {
                preview = null
                saveToDownloads(FsEntry(previewName, "file"))
            },
            onEdit = {
                preview = null
                openEditor(FsEntry(previewName, "file"))
            }
        )
    }

    if (editTarget != null) {
        FileEditorDialog(
            name = editTarget!!.path,
            text = editText,
            dirty = editText != editOriginal,
            saving = editSaving,
            error = editError,
            onTextChange = { editText = it },
            onSave = { saveEditor() },
            onCancel = { editTarget = null }
        )
    }

    if (deleteTarget != null) {
        val target = deleteTarget!!
        val isDir = target.type == "directory"
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(if (isDir) "Удалить папку?" else "Удалить файл?") },
            text = {
                Text(
                    if (isDir) {
                        "Папка «${target.path}» и всё её содержимое будут удалены безвозвратно."
                    } else {
                        "Файл «${target.path}» будет удалён безвозвратно."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    deleteTarget = null
                    scope.launch {
                        loading = true
                        runCatching {
                            withContext(Dispatchers.IO) {
                                api.fsDelete(directory, target.path, recursive = isDir)
                            }
                        }.onFailure { status = it.message ?: "Ошибка удаления" }
                        loading = false
                        reload()
                    }
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Отмена") } }
        )
    }

    if (newFolder) {
        AlertDialog(
            onDismissRequest = { newFolder = false },
            title = { Text("Создать папку") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    singleLine = true,
                    placeholder = { Text("Имя папки") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newFolderName.isNotBlank(),
                    onClick = {
                        val name = newFolderName.trim()
                        if (name.isBlank() || name.contains('/') || name.contains("..")) {
                            status = "Недопустимое имя папки"
                        } else {
                            newFolder = false
                            scope.launch {
                                loading = true
                                runCatching {
                                    withContext(Dispatchers.IO) { api.fsMkdir(directory, path + name + "/") }
                                }.onFailure { status = it.message ?: "Ошибка создания папки" }
                                loading = false
                                reload()
                            }
                        }
                    }
                ) { Text("Создать") }
            },
            dismissButton = { TextButton(onClick = { newFolder = false }) { Text("Отмена") } }
        )
    }

    if (renameTarget != null) {
        val target = renameTarget!!
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Переименовать") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    placeholder = { Text("Новое имя") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameText.isNotBlank(),
                    onClick = {
                        val name = renameText.trim()
                        if (name.isBlank() || name.contains('/') || name.contains("..")) {
                            status = "Недопустимое имя"
                        } else {
                            val to = parentPath(target.path) + name
                            renameTarget = null
                            scope.launch {
                                loading = true
                                runCatching {
                                    withContext(Dispatchers.IO) { api.fsRename(directory, target.path, to) }
                                }.onFailure { status = it.message ?: "Ошибка переименования" }
                                loading = false
                                reload()
                            }
                        }
                    }
                ) { Text("Переименовать") }
            },
            dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun FilePreviewDialog(
    name: String, data: FsRead, onDismiss: () -> Unit,
    onDownload: () -> Unit, onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(name, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
                Text(
                    "${data.mime} · ${fmtNum(data.size)} байт",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            val bytes = data.bytes
            val text = data.text
            when {
                bytes != null -> {
                    val bmp = remember(bytes) {
                        runCatching {
                            val opts = BitmapFactory.Options().apply {
                                inSampleSize = if (bytes.size > 4_000_000) 4
                                else if (bytes.size > 1_000_000) 2 else 1
                            }
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)?.asImageBitmap()
                        }.getOrNull()
                    }
                    if (bmp != null) {
                        Image(
                            bmp,
                            contentDescription = name,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("Не удалось показать изображение")
                    }
                }
                text != null -> {
                    Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                        SelectionContainer {
                            Text(
                                text + if (data.size > 1_000_000) "\n\n… (файл обрезан)" else "",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                else -> Text("Бинарный файл (${fmtNum(data.size)} байт). Просмотр не поддерживается.")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
        dismissButton = {
            Row {
                TextButton(onClick = onDownload) { Text("Скачать") }
                if (data.text != null) {
                    TextButton(onClick = onEdit) { Text("Редактировать") }
                }
            }
        }
    )
}

/** Полноэкранный текстовый редактор для файлов проекта. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileEditorDialog(
    name: String, text: String, dirty: Boolean, saving: Boolean, error: String,
    onTextChange: (String) -> Unit, onSave: () -> Unit, onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!saving) onCancel() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    name.trimEnd('/').substringAfterLast('/'),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 15.sp
                                )
                                Text(
                                    if (error.isNotBlank()) error else if (dirty) "Изменено" else name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 11.sp,
                                    color = if (error.isNotBlank()) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            TextButton(onClick = onCancel, enabled = !saving) { Text("← Отмена") }
                        },
                        actions = {
                            TextButton(onClick = onSave, enabled = dirty && !saving) {
                                Text(if (saving) "Сохраняю…" else "Сохранить")
                            }
                        }
                    )
                }
            ) { pad ->
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .padding(pad)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun fileMime(name: String): String {
    val ext = name.substringAfterLast('.', "").lowercase()
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        ?: "application/octet-stream"
}

@Composable
private fun MentionPickerDialog(
    directory: String,
    baseUrl: String, user: String, password: String,
    onDismiss: () -> Unit, onPick: (String) -> Unit
) {
    val api = remember { ApiClient(baseUrl, user, password) }
    var query by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<FsEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(path, query) {
        if (query.isNotBlank()) delay(250)
        loading = true
        error = ""
        runCatching {
            withContext(Dispatchers.IO) {
                if (query.isBlank()) {
                    api.fsList(directory, path)
                } else {
                    api.fsFind(directory, query, "file", 50)
                }
            }
        }.onSuccess { results = it }
            .onFailure { error = it.message ?: "Ошибка" }
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Файл в контекст") },
        text = {
            Column(Modifier.heightIn(max = 480.dp)) {
                Text(
                    if (query.isBlank()) (if (path.isBlank()) "/" else path) else "Поиск по проекту",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Поиск файла в проекте") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                )
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 2.dp))
                if (error.isNotBlank()) {
                    Text(error, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
                Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                    if (query.isBlank() && path.isNotEmpty()) {
                        Row(
                            Modifier.fillMaxWidth().clickable { path = parentPath(path) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("..", fontSize = 13.sp)
                        }
                    }
                    results.forEach { e ->
                        val isDir = e.type == "directory"
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                if (isDir && query.isBlank()) {
                                    path = e.path
                                    query = ""
                                } else if (!isDir) {
                                    onPick(e.path)
                                }
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isDir) Icons.Filled.Folder else Icons.Filled.Description,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                if (query.isBlank()) e.path.removePrefix(path) else e.path,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (results.isEmpty() && !loading) {
                        Text(
                            "Ничего не найдено",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreen(
    title: String, messages: List<ChatMessage>, liveBlocks: List<Block>, busy: Boolean, status: String,
    input: String, attachments: List<Attachment>,
    models: List<ModelInfo>, providerId: String, modelId: String,
    variant: String,
    contextUsed: Long, contextLimit: Long,
    commands: List<CommandInfo>,
    onInput: (String) -> Unit, onSend: () -> Unit,
    onStop: () -> Unit,
    onOpenModelPicker: () -> Unit,
    onSessionAction: (SessionAction) -> Unit,
    onPickCommand: (String) -> Unit,
    onSelectVariant: (String) -> Unit,
    onAttach: (Uri) -> Unit, onRemoveAttachment: (Int) -> Unit,
    recording: Boolean, recordSeconds: Int, transcribing: Boolean,
    onMic: () -> Unit,
    connectionKind: String,
    profileName: String,
    sessionId: String,
    historyLoading: Boolean,
    historyHasMore: Boolean,
    onLoadOlder: () -> Unit,
    onRevertMessage: (String) -> Unit,
    onOpenFiles: () -> Unit,
    onBack: () -> Unit, onSettings: () -> Unit
) {
    val context = LocalContext.current
    val currentModelName = models.firstOrNull { it.providerId == providerId && it.modelId == modelId }?.name
        ?: prettyModel(modelId)
    var variantMenu by remember { mutableStateOf(false) }
    var sessionMenu by remember { mutableStateOf(false) }
    var addMenu by remember { mutableStateOf(false) }
    val currentVariants = models.firstOrNull { it.providerId == providerId && it.modelId == modelId }?.variants ?: emptyList()
    val variantLabel = if (variant.isBlank() || variant == "default") "Default" else variant.replaceFirstChar { it.uppercase() }
    val contextFraction = if (contextLimit > 0) (contextUsed.toFloat() / contextLimit).coerceIn(0f, 1f) else 0f
    val slashQuery = if (input.startsWith("/") && !input.contains(' ')) input.drop(1) else null
    val builtinCommands = listOf(
        "new" to "Новая сессия",
        "compact" to "Сократить сессию",
        "fork" to "Создать сессию из сообщения",
        "export" to "Экспортировать историю"
    )
    val slashItems: List<Pair<String, String>> = if (slashQuery == null) emptyList() else {
        val base = builtinCommands.map { it.first to it.second } +
            commands.map { it.name to it.description }
        base.filter { it.first.startsWith(slashQuery, ignoreCase = true) }
    }

    val attachLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onAttach(uri)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onAttach(uri)
    }

    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            pendingCameraUri?.let { value -> onAttach(Uri.parse(value)) }
        }
        pendingCameraUri = null
    }

    fun openCamera() {
        val dir = runCatching {
            File(context.cacheDir, "camera").apply { if (!exists()) mkdirs() }
        }.getOrNull()
        val file = runCatching {
            File.createTempFile("photo_", ".jpg", dir)
        }.getOrNull()
        if (file == null) {
            Toast.makeText(context, "Не удалось создать файл для фото", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = runCatching {
            FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        }.getOrNull()
        if (uri == null) {
            Toast.makeText(context, "Не удалось подготовить камеру", Toast.LENGTH_SHORT).show()
            return
        }
        pendingCameraUri = uri.toString()
        runCatching { cameraLauncher.launch(uri) }.onFailure {
            pendingCameraUri = null
            Toast.makeText(context, "Камера недоступна", Toast.LENGTH_SHORT).show()
        }
    }

    val listState = rememberLazyListState()
    val listScope = rememberCoroutineScope()
    var historyArmed by remember(sessionId) { mutableStateOf(false) }
    var pendingAnchor by remember(sessionId) { mutableStateOf<Pair<String, Int>?>(null) }
    var revertTarget by remember(sessionId) { mutableStateOf<ChatMessage?>(null) }

    fun messageTime(ms: Long): String =
        if (ms > 0) DateFormat.getTimeFormat(context).format(Date(ms)) else ""

    fun formatDuration(ms: Long): String {
        val total = (ms / 1000).coerceAtLeast(0)
        return if (total < 60) "$total с" else "${total / 60} мин ${total % 60} с"
    }

    /** Время всего хода (от вопроса до последнего ответа) — как в веб-интерфейсе. */
    fun turnDurationMs(message: ChatMessage): Long {
        if (message.role != "assistant") return 0
        val idx = messages.indexOfFirst { it.id == message.id }
        if (idx < 0) return 0
        var nextUser = messages.size
        for (j in idx + 1 until messages.size) {
            if (messages[j].role == "user") {
                nextUser = j
                break
            }
        }
        var lastAssistant = -1
        for (j in nextUser - 1 downTo idx) {
            if (messages[j].role == "assistant") {
                lastAssistant = j
                break
            }
        }
        if (lastAssistant != idx) return 0
        var userIdx = -1
        for (j in idx downTo 0) {
            if (messages[j].role == "user") {
                userIdx = j
                break
            }
        }
        if (userIdx < 0) return 0
        val start = messages[userIdx].created
        val end = message.completed
        if (start <= 0 || end <= 0 || end < start) return 0
        return end - start
    }

    fun requestOlder() {
        if (historyLoading || !historyHasMore) return
        val first = listState.firstVisibleItemIndex
        val offset = listState.firstVisibleItemScrollOffset
        val anchorId = messages.getOrNull(first)?.id
        if (anchorId != null) pendingAnchor = anchorId to offset
        onLoadOlder()
    }

    val scrollToBottom: () -> Unit = {
        listScope.launch {
            val last = listState.layoutInfo.totalItemsCount - 1
            if (last >= 0) listState.animateScrollToItem(last)
        }
    }
    val atBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            info.totalItemsCount == 0 || info.visibleItemsInfo.lastOrNull()?.index == info.totalItemsCount - 1
        }
    }
    var stickToBottom by remember(sessionId) { mutableStateOf(true) }
    var stickReady by remember(sessionId) { mutableStateOf(false) }
    LaunchedEffect(listState, sessionId) {
        snapshotFlow {
            val info = listState.layoutInfo
            info.totalItemsCount == 0 ||
                info.visibleItemsInfo.lastOrNull()?.index == info.totalItemsCount - 1
        }.collect { atEnd ->
            if (stickReady) stickToBottom = atEnd
        }
    }
    // Открыли сессию / появилась история — сразу в конец.
    LaunchedEffect(sessionId, messages.isNotEmpty()) {
        if (messages.isEmpty() || historyLoading || pendingAnchor != null) return@LaunchedEffect
        val target = if (busy) messages.size + 1 else messages.size
        listState.scrollToItem(target)
        stickToBottom = true
        stickReady = true
    }
    // Ответ готов (busy true -> false) — тоже в конец.
    LaunchedEffect(busy) {
        if (busy || messages.isEmpty() || historyLoading || pendingAnchor != null) return@LaunchedEffect
        listState.scrollToItem(messages.size)
        stickToBottom = true
        stickReady = true
    }
    LaunchedEffect(messages.size, liveBlocks.size, busy, historyLoading) {
        // При подгрузке старых сообщений не прыгаем вниз: позицию
        // восстанавливает pendingAnchor. Если пользователь читает историю
        // вверху — новые токены тоже не должны утаскивать его вниз.
        if (historyLoading || pendingAnchor != null || !stickReady || !stickToBottom) return@LaunchedEffect
        val target = if (busy) messages.size + 1 else messages.size
        if (target >= 0) listState.scrollToItem(target)
    }
    LaunchedEffect(messages.size, historyLoading) {
        val anchor = pendingAnchor ?: return@LaunchedEffect
        if (historyLoading) return@LaunchedEffect
        val index = messages.indexOfFirst { it.id == anchor.first }
        if (index >= 0) listState.scrollToItem(index, anchor.second)
        pendingAnchor = null
    }
    LaunchedEffect(listState, sessionId, messages.size, historyLoading, historyHasMore) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            if (index > 1) historyArmed = true
            if (historyArmed && index == 0 && offset <= 8 &&
                messages.isNotEmpty() && !historyLoading &&
                historyHasMore && pendingAnchor == null
            ) {
                requestOlder()
            }
        }
    }

    revertTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { revertTarget = null },
            title = { Text("Откатить до этого вопроса?") },
            text = { Text("Этот вопрос и все сообщения после него будут убраны из сессии.") },
            confirmButton = {
                TextButton(onClick = {
                    revertTarget = null
                    onRevertMessage(target.id)
                }) { Text("Откатить") }
            },
            dismissButton = {
                TextButton(onClick = { revertTarget = null }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text("← Сессии") }
                    Text(
                        title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    Modifier.fillMaxWidth().padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        ConnectionBadge(connectionKind)
                        if (profileName.isNotBlank()) {
                            Spacer(Modifier.size(6.dp))
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.size(2.dp))
                            Text(
                                profileName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                    Box {
                        TextButton(onClick = { variantMenu = true }) {
                            Text(variantLabel, fontSize = 13.sp)
                            Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(expanded = variantMenu, onDismissRequest = { variantMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Default", fontSize = 13.sp) },
                                onClick = { variantMenu = false; onSelectVariant("") }
                            )
                            currentVariants.forEach { v ->
                                DropdownMenuItem(
                                    text = { Text(v.replaceFirstChar { it.uppercase() }, fontSize = 13.sp) },
                                    onClick = { variantMenu = false; onSelectVariant(v) }
                                )
                            }
                        }
                    }
                    ContextRing(contextUsed, contextLimit, contextFraction)
                    Box {
                        IconButton(onClick = { sessionMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Меню сессии")
                        }
                        DropdownMenu(expanded = sessionMenu, onDismissRequest = { sessionMenu = false }) {
                            DropdownMenuItem(text = { Text("Переименовать") }, onClick = { sessionMenu = false; onSessionAction(SessionAction.RENAME) })
                            DropdownMenuItem(text = { Text("Отменить последнее") }, onClick = { sessionMenu = false; onSessionAction(SessionAction.UNDO) })
                            DropdownMenuItem(text = { Text("Компактифицировать") }, onClick = { sessionMenu = false; onSessionAction(SessionAction.COMPACT) })
                            DropdownMenuItem(text = { Text("Форк из сообщения") }, onClick = { sessionMenu = false; onSessionAction(SessionAction.FORK) })
                            DropdownMenuItem(text = { Text("В фон") }, onClick = { sessionMenu = false; onSessionAction(SessionAction.BACKGROUND) })
                            DropdownMenuItem(text = { Text("Изменения") }, onClick = { sessionMenu = false; onSessionAction(SessionAction.CHANGES) })
                            DropdownMenuItem(text = { Text("Использование") }, onClick = { sessionMenu = false; onSessionAction(SessionAction.USAGE) })
                            DropdownMenuItem(text = { Text("Экспортировать…") }, onClick = { sessionMenu = false; onSessionAction(SessionAction.EXPORT) })
                            DropdownMenuItem(text = { Text("Удалить…") }, onClick = { sessionMenu = false; onSessionAction(SessionAction.DELETE) })
                            DropdownMenuItem(text = { Text("Настройки") }, onClick = { sessionMenu = false; onSettings() })
                        }
                    }
                }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (historyLoading) {
                    item(key = "history-loading") {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                items(messages, key = { it.id }) { m ->
                    val isUser = m.role == "user"
                    val attachmentsBlock: @Composable () -> Unit = {
                        m.attachments.forEach { a ->
                            if (a.mime.startsWith("image/")) {
                                AttachmentImage(a.data, a.name)
                            } else {
                                Text("📄 ${a.name}", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                    if (isUser) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.82f)
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        SelectionContainer {
                                            Column {
                                                m.blocks.forEach { block -> BlockView(block, MaterialTheme.colorScheme.primary) }
                                            }
                                        }
                                        attachmentsBlock()
                                    }
                                }
                            }
                            Row(
                                Modifier.padding(top = 2.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val time = messageTime(m.created)
                                if (time.isNotBlank()) {
                                    Text(
                                        time,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (!m.id.startsWith("local-")) {
                                    IconButton(
                                        onClick = { revertTarget = m },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Undo,
                                            contentDescription = "Откатить до этого вопроса",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                val copyText = m.blocks.filterIsInstance<TextBlock>()
                                    .joinToString("\n\n") { it.text }.trim()
                                if (copyText.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            cm.setPrimaryClip(ClipData.newPlainText("opencode", copyText))
                                            Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.ContentCopy,
                                            contentDescription = "Копировать",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            SelectionContainer {
                                Column {
                                    m.blocks.forEach { block -> BlockView(block, MaterialTheme.colorScheme.primary) }
                                    attachmentsBlock()
                                }
                            }
                            val copyText = m.blocks.filterIsInstance<TextBlock>().joinToString("\n\n") { it.text }
                            val durationMs = turnDurationMs(m)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (copyText.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            cm.setPrimaryClip(ClipData.newPlainText("opencode", copyText))
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.ContentCopy,
                                            contentDescription = "Копировать",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                if (durationMs > 0) {
                                    Text(
                                        formatDuration(durationMs),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                if (busy) {
                    item {
                        Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            if (liveBlocks.isNotEmpty()) {
                                SelectionContainer {
                                    Column {
                                        liveBlocks.forEach { block -> BlockView(block, MaterialTheme.colorScheme.primary) }
                                    }
                                }
                            }
                            WorkingIndicator()
                        }
                    }
                }
                item { Spacer(Modifier.height(1.dp)) }
            }
            if (!atBottom) {
                Surface(
                    onClick = scrollToBottom,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 8.dp)
                        .size(40.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Вниз",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            }
            if (busy) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp)
                )
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "opencode работает…",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 12.dp))
            if (attachments.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    attachments.forEachIndexed { idx, a ->
                        Box(Modifier.size(72.dp)) {
                            if (a.mime.startsWith("image/")) {
                                AttachmentThumb(a.data)
                            } else {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(a.name.take(10), fontSize = 11.sp, maxLines = 2)
                                }
                            }
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { onRemoveAttachment(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Убрать", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
            if (slashItems.isNotEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    slashItems.forEach { (name, desc) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPickCommand(name) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("/$name", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.size(10.dp))
                            Text(desc, fontSize = 12.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            if (recording) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "Запись ${recordSeconds} с — нажмите ■, чтобы остановить",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (transcribing) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "Распознаю речь…",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Box(Modifier.fillMaxWidth()) {
                    BasicTextField(
                        value = input,
                        onValueChange = onInput,
                        modifier = Modifier.fillMaxWidth().padding(start = 6.dp, end = 48.dp),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = LocalTextStyle.current.fontFamily
                        ),
                        maxLines = 6,
                        decorationBox = { inner ->
                            Box(Modifier.fillMaxWidth()) {
                                if (input.isEmpty()) {
                                    Text(
                                        "Спросите что угодно, / — команды, @ — контекст…",
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                inner()
                            }
                        }
                    )
                    if (transcribing) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 2.dp, end = 2.dp)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    } else {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 2.dp, end = 2.dp)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                                .clickable { onMic() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (recording) Icons.Filled.Stop else Icons.Filled.Mic,
                                contentDescription = if (recording) "Остановить запись" else "Голосовой ввод",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        CircleButton(icon = Icons.Filled.Add, enabled = !busy) {
                            addMenu = true
                        }
                        DropdownMenu(expanded = addMenu, onDismissRequest = { addMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Камера") },
                                leadingIcon = {
                                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                                },
                                onClick = {
                                    addMenu = false
                                    openCamera()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Галерея") },
                                leadingIcon = {
                                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                                },
                                onClick = {
                                    addMenu = false
                                    runCatching {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Прикрепить файл") },
                                leadingIcon = {
                                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                                },
                                onClick = {
                                    addMenu = false
                                    runCatching { attachLauncher.launch("*/*") }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Файлы проекта") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Folder, contentDescription = null)
                                },
                                onClick = { addMenu = false; onOpenFiles() }
                            )
                        }
                    }
                    Spacer(Modifier.size(6.dp))
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        BoxWithConstraints(
                            Modifier
                                .weight(1f, fill = false)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { onOpenModelPicker() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            val density = LocalDensity.current
                            val measurer = rememberTextMeasurer()
                            val arrowPx = with(density) { 18.dp.toPx() }
                            val gapPx = with(density) { 2.dp.toPx() }
                            val availablePx = with(density) { maxWidth.toPx() } - arrowPx - gapPx
                            val baseStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontFamily = LocalTextStyle.current.fontFamily
                            )
                            val measured = measurer.measure(
                                AnnotatedString(currentModelName),
                                style = baseStyle,
                                maxLines = 1,
                                softWrap = false
                            )
                            val fit = if (measured.size.width > 0)
                                (availablePx / measured.size.width).coerceIn(0f, 1f) else 1f
                            val fittedSize = (14f * fit).coerceAtLeast(8f).sp
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    currentModelName,
                                    fontSize = fittedSize,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.size(2.dp))
                                Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.size(6.dp))
                    if (busy) {
                        CircleButton(icon = Icons.Filled.Stop, enabled = true, filled = true) { onStop() }
                    } else {
                        CircleButton(
                            icon = Icons.Filled.ArrowUpward,
                            enabled = (input.isNotBlank() || attachments.isNotEmpty()) && !recording && !transcribing,
                            filled = true
                        ) { onSend() }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionDialog(
    permission: PendingPermission, busy: Boolean,
    onOnce: () -> Unit, onAlways: () -> Unit, onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Разрешение") },
        text = {
            Column {
                Text("opencode просит разрешение:")
                Spacer(Modifier.height(8.dp))
                Text(permission.permission, fontWeight = FontWeight.Bold)
                if (permission.patterns.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(permission.patterns.joinToString("\n"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Column {
                Row {
                    TextButton(onClick = onOnce) { Text("Один раз") }
                    TextButton(onClick = onAlways) { Text("Всегда") }
                }
                TextButton(onClick = onReject) { Text("Отклонить") }
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun FormDialog(
    form: PendingForm,
    onSubmit: (JSONObject) -> Unit,
    onCancel: () -> Unit
) {
    var selections by remember(form.id) { mutableStateOf(form.fields.associate { it.key to emptySet<String>() }) }
    var texts by remember(form.id) { mutableStateOf(form.fields.associate { it.key to "" }) }
    var bools by remember(form.id) { mutableStateOf(form.fields.associate { it.key to false }) }
    AlertDialog(
        onDismissRequest = {},
        title = { Text(form.title) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                form.fields.forEachIndexed { index, f ->
                    if (f.title.isNotBlank()) Text(f.title, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    when {
                        f.options.isNotEmpty() -> {
                            f.options.forEach { (value, label) ->
                                val selected = selections[f.key]?.contains(value) == true
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val cur = selections[f.key] ?: emptySet()
                                            val next = if (selected) cur - value else cur + value
                                            selections = selections + (f.key to next)
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(if (selected) "◉  " else "○  ", color = MaterialTheme.colorScheme.primary)
                                    Text(label, fontSize = 14.sp)
                                }
                            }
                        }
                        f.type == "boolean" -> {
                            val checked = bools[f.key] == true
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { bools = bools + (f.key to !checked) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (checked) "☑  " else "☐  ", color = MaterialTheme.colorScheme.primary)
                                Text("Да", fontSize = 14.sp)
                            }
                        }
                        else -> {
                            OutlinedTextField(
                                value = texts[f.key] ?: "",
                                onValueChange = { v -> texts = texts + (f.key to v) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Ответ…") },
                                singleLine = true
                            )
                        }
                    }
                    if (index < form.fields.lastIndex) Spacer(Modifier.height(14.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val answer = JSONObject()
                form.fields.forEach { f ->
                    val sel = selections[f.key] ?: emptySet()
                    val txt = (texts[f.key] ?: "").trim()
                    when {
                        f.options.isNotEmpty() -> {
                            if (f.type == "multiselect") {
                                val arr = JSONArray()
                                sel.forEach { arr.put(it) }
                                if (arr.length() > 0) answer.put(f.key, arr)
                            } else if (sel.isNotEmpty()) {
                                answer.put(f.key, sel.first())
                            }
                        }
                        f.type == "boolean" -> answer.put(f.key, bools[f.key] == true)
                        f.type == "number" || f.type == "integer" -> {
                            if (txt.isNotBlank()) {
                                txt.toDoubleOrNull()?.let { answer.put(f.key, it) } ?: answer.put(f.key, txt)
                            }
                        }
                        else -> if (txt.isNotBlank()) answer.put(f.key, txt)
                    }
                }
                onSubmit(answer)
            }) { Text("Отправить") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Отклонить") }
        }
    )
}

@Composable
private fun ConnectionBadge(kind: String) {
    if (kind.isBlank()) return
    val ts = kind == "tailscale"
    val bg = if (ts) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (ts) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (ts) Icons.Filled.VpnLock else Icons.Filled.Language,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = fg
        )
        Spacer(Modifier.size(4.dp))
        Text(
            if (ts) "Tailscale" else "Интернет",
            fontSize = 11.sp,
            color = fg
        )
    }
}

@Composable
private fun ContextRing(used: Long, limit: Long, fraction: Float) {
    val track = MaterialTheme.colorScheme.outlineVariant
    val color = when {
        fraction >= 0.9f -> Color(0xFFE53935)
        fraction >= 0.7f -> Color(0xFFF9A825)
        else -> MaterialTheme.colorScheme.primary
    }
    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(22.dp)) {
            val stroke = 3.dp.toPx()
            drawArc(color = track, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = stroke))
            if (fraction > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun CircleButton(
    icon: ImageVector, enabled: Boolean, filled: Boolean = false, onClick: () -> Unit
) {
    val bg = when {
        filled && enabled -> MaterialTheme.colorScheme.primary
        filled -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    val fg = if (filled && enabled) MaterialTheme.colorScheme.onPrimary
    else if (enabled) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun BlockView(block: Block, linkColor: Color) {
    when (block) {
        is TextBlock -> if (block.text.isNotBlank()) {
            MarkdownView(block.text, linkColor)
        }
        is ReasoningBlock -> {
            Column(Modifier.padding(vertical = 4.dp)) {
                Text(
                    "Размышление",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    block.text,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        is ToolBlock -> ToolRow(block)
        is ErrorBlock -> Row(
            Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                block.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WorkingIndicator() {
    val transition = rememberInfiniteTransition(label = "working")
    val alphas = (0 until 3).map { i ->
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500, delayMillis = i * 150),
                repeatMode = RepeatMode.Reverse
            ),
            label = "workingDot$i"
        )
    }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        alphas.forEach { a ->
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = a.value))
            )
            Spacer(Modifier.size(4.dp))
        }
        Spacer(Modifier.size(4.dp))
        Text(
            "opencode работает…",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ToolRow(t: ToolBlock) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            toolIcon(t.name),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.size(8.dp))
        Text(
            toolLabel(t.name) + if (t.detail.isNotBlank()) " ${t.detail}" else "",
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (t.additions > 0) {
            Spacer(Modifier.size(8.dp))
            Text("+${t.additions}", fontSize = 12.sp, color = Color(0xFF2E7D32))
        }
        if (t.deletions > 0) {
            Spacer(Modifier.size(6.dp))
            Text("-${t.deletions}", fontSize = 12.sp, color = Color(0xFFC62828))
        }
    }
}

private fun toolLabel(name: String): String = when (name) {
    "edit" -> "Редактировать"
    "write" -> "Запись"
    "read" -> "Чтение"
    "bash" -> "Оболочка"
    "shell" -> "Оболочка"
    "grep" -> "Поиск"
    "glob" -> "Поиск файлов"
    "list" -> "Список"
    "webfetch" -> "Загрузка"
    "todowrite" -> "Задачи"
    "task" -> "Задача"
    "patch" -> "Патч"
    else -> name.replaceFirstChar { it.uppercase() }
}

private fun toolIcon(name: String): ImageVector = when (name) {
    "edit", "patch" -> Icons.Filled.Edit
    "write" -> Icons.Filled.Save
    "read" -> Icons.Filled.Description
    "bash" -> Icons.Filled.Terminal
    "shell" -> Icons.Filled.Terminal
    "grep", "glob", "find" -> Icons.Filled.Search
    "list" -> Icons.AutoMirrored.Filled.List
    "webfetch" -> Icons.Filled.Language
    "todowrite" -> Icons.Filled.Checklist
    "task" -> Icons.Filled.AccountTree
    else -> Icons.Filled.Build
}

@Composable
private fun rememberDecodedBitmap(data: String): ImageBitmap? {
    var bitmap by remember(data) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(data) {
        bitmap = withContext(Dispatchers.Default) {
            runCatching {
                val bytes = Base64.decode(data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.getOrNull()
        }
    }
    return bitmap
}

@Composable
private fun AttachmentImage(data: String, name: String) {
    val b = rememberDecodedBitmap(data)
    if (b != null) {
        Image(
            bitmap = b,
            contentDescription = name,
            modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp).clip(RoundedCornerShape(10.dp)).padding(top = 4.dp),
            contentScale = ContentScale.Fit
        )
    } else {
        Text("🖼 $name", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun AttachmentThumb(data: String) {
    val b = rememberDecodedBitmap(data)
    if (b != null) {
        Image(
            bitmap = b,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(Modifier.size(18.dp)) }
    }
}

@Composable
private fun ModelPickerDialog(
    models: List<ModelInfo>, providerId: String, modelId: String,
    query: String, onQuery: (String) -> Unit,
    onDismiss: () -> Unit, onManage: () -> Unit, onPick: (ModelInfo) -> Unit
) {
    val filtered = models.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true) || it.modelId.contains(query, ignoreCase = true)
    }
    val grouped = filtered.groupBy { it.providerName }.toSortedMap()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбор модели") },
        text = {
            Column(Modifier.heightIn(max = 460.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Поиск моделей") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.weight(1f, fill = false)) {
                    grouped.forEach { (provider, list) ->
                        item {
                            Text(
                                provider,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(list, key = { it.providerId + "/" + it.modelId }) { mi ->
                            val current = mi.providerId == providerId && mi.modelId == modelId
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(mi) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    mi.name,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f),
                                    color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (mi.free) {
                                    Text(
                                        "Бесплатно",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Spacer(Modifier.size(6.dp))
                                }
                                if (current) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
        dismissButton = { TextButton(onClick = onManage) { Text("Настроить модели") } }
    )
}

@Composable
private fun ModelManageDialog(
    models: List<ModelInfo>,
    hidden: Set<String>,
    onToggle: (ModelInfo) -> Unit,
    onShowAll: () -> Unit,
    onHideAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = models.filter {
        query.isBlank() ||
            it.name.contains(query, ignoreCase = true) ||
            it.modelId.contains(query, ignoreCase = true) ||
            it.providerName.contains(query, ignoreCase = true)
    }
    val grouped = filtered.groupBy { it.providerName }.toSortedMap()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Модели") },
        text = {
            Column(Modifier.heightIn(max = 480.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Поиск моделей") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Очистить")
                            }
                        }
                    }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Отметьте модели, которые показывать в выборе.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                    if (filtered.isEmpty()) {
                        Text(
                            "Ничего не найдено",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    grouped.forEach { (provider, list) ->
                        Text(
                            provider, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        list.forEach { mi ->
                            val key = mi.providerId + "/" + mi.modelId
                            val visible = key !in hidden
                            Row(
                                Modifier.fillMaxWidth().clickable { onToggle(mi) }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (visible) "☑  " else "☐  ",
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(mi.name, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } },
        dismissButton = {
            Row {
                TextButton(onClick = onShowAll) { Text("Показать все") }
                TextButton(onClick = onHideAll) { Text("Скрыть все") }
            }
        }
    )
}

@Composable
private fun UsageDialog(
    usage: SessionStats?, contextUsed: Long, contextLimit: Long, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Использование") },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                val u = usage
                if (u == null) {
                    Text("Нет данных")
                } else {
                    UsageRow("Стоимость", "$" + String.format(java.util.Locale.US, "%.4f", u.cost))
                    val ctx = if (contextLimit > 0) {
                        "${fmtNum(contextUsed)} / ${fmtNum(contextLimit)}  (${contextUsed * 100 / contextLimit}%)"
                    } else fmtNum(contextUsed)
                    UsageRow("Контекст", ctx)
                    Spacer(Modifier.height(10.dp))
                    Text("Токены", fontWeight = FontWeight.Bold)
                    UsageRow("Вход", fmtNum(u.input))
                    UsageRow("Выход", fmtNum(u.output))
                    UsageRow("Размышления", fmtNum(u.reasoning))
                    UsageRow("Кэш (чтение)", fmtNum(u.cacheRead))
                    UsageRow("Кэш (запись)", fmtNum(u.cacheWrite))
                    Spacer(Modifier.height(10.dp))
                    Text("Сеанс", fontWeight = FontWeight.Bold)
                    UsageRow("Модель", u.model)
                    UsageRow("Агент", u.agent)
                    UsageRow("Каталог", u.directory)
                    if (u.created > 0) UsageRow("Создан", fmtDate(u.created))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

private fun usagePeriodLabel(period: UsagePeriod, customStart: Long, customEnd: Long): String =
    when (period) {
        UsagePeriod.DAY -> "Сегодня"
        UsagePeriod.MONTH -> "Текущий месяц"
        UsagePeriod.ALL -> "Всё время"
        UsagePeriod.CUSTOM -> fmtDay(customStart) + " — " + fmtDay(customEnd)
    }

@Composable
private fun UsagePeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectUsageDialog(
    usage: ProjectUsage?, directory: String,
    title: String = "Использование проекта", subject: String = "Проект",
    period: UsagePeriod,
    customStart: Long, customEnd: Long,
    loading: Boolean,
    onPeriod: (UsagePeriod) -> Unit,
    onCustomStart: (Long) -> Unit,
    onCustomEnd: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    fun pickDate(initial: Long, onPick: (Long) -> Unit) {
        val c = Calendar.getInstance()
        c.timeInMillis = initial
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val picked = Calendar.getInstance()
                picked.set(year, month, day, 0, 0, 0)
                picked.set(Calendar.MILLISECOND, 0)
                onPick(picked.timeInMillis)
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                Text("Период", fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UsagePeriodChip("День", period == UsagePeriod.DAY) { onPeriod(UsagePeriod.DAY) }
                    UsagePeriodChip("Месяц", period == UsagePeriod.MONTH) { onPeriod(UsagePeriod.MONTH) }
                    UsagePeriodChip("Всё время", period == UsagePeriod.ALL) { onPeriod(UsagePeriod.ALL) }
                    UsagePeriodChip("Период", period == UsagePeriod.CUSTOM) { onPeriod(UsagePeriod.CUSTOM) }
                }
                if (period == UsagePeriod.CUSTOM) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { pickDate(customStart, onCustomStart) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("С: " + fmtDay(customStart), fontSize = 12.sp, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { pickDate(customEnd, onCustomEnd) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("По: " + fmtDay(customEnd), fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
                Text(
                    "Для дня/месяца/периода — по датам сообщений (ответов модели)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (loading) {
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(10.dp))
                val u = usage
                if (u == null) {
                    Text(if (loading) "Загрузка…" else "Нет данных")
                } else {
                    UsageRow("Период", usagePeriodLabel(period, customStart, customEnd))
                    UsageRow("Стоимость", "$" + String.format(java.util.Locale.US, "%.4f", u.cost))
                    UsageRow("Сессий", fmtNum(u.sessions.toLong()))
                    Spacer(Modifier.height(10.dp))
                    Text("Токены", fontWeight = FontWeight.Bold)
                    UsageRow("Вход", fmtNum(u.input))
                    UsageRow("Выход", fmtNum(u.output))
                    UsageRow("Размышления", fmtNum(u.reasoning))
                    UsageRow("Кэш (чтение)", fmtNum(u.cacheRead))
                    UsageRow("Кэш (запись)", fmtNum(u.cacheWrite))
                    Spacer(Modifier.height(10.dp))
                    Text(subject, fontWeight = FontWeight.Bold)
                    UsageRow("Каталог", directory)
                    if (u.created > 0) UsageRow("Первый сеанс", fmtDate(u.created))
                    if (u.updated > 0) UsageRow("Последний", fmtDate(u.updated))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

@Composable
private fun UsageRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(120.dp))
        Text(value, fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ChangesDialog(changes: List<FileChange>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменения") },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                if (changes.isEmpty()) {
                    Text("Нет изменённых файлов")
                } else {
                    changes.forEach { c ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(c.file, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f))
                            if (c.additions > 0) Text("+${c.additions}", fontSize = 12.sp, color = Color(0xFF2E7D32))
                            if (c.deletions > 0) {
                                Spacer(Modifier.size(6.dp))
                                Text("-${c.deletions}", fontSize = 12.sp, color = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

private fun fmtNum(n: Long): String = java.text.NumberFormat.getIntegerInstance().format(n)

private fun fmtDate(ms: Long): String =
    java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ms))

@Composable
private fun NewProjectDialog(
    parent: String, name: String, busy: Boolean, error: String = "",
    onParent: (String) -> Unit, onName: (String) -> Unit,
    onDismiss: () -> Unit, onCreate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый проект") },
        text = {
            Column {
                Text("Папка, где создать", fontWeight = FontWeight.Bold)
                OutlinedTextField(parent, onParent, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("/home/gmpony") })
                Spacer(Modifier.height(12.dp))
                Text("Название проекта", fontWeight = FontWeight.Bold)
                OutlinedTextField(name, onName, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("my-project") })
                Spacer(Modifier.height(8.dp))
                Text(
                    "Будет создана папка <папка>/<название> и git-репозиторий.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (error.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(error, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = onCreate, enabled = !busy && name.isNotBlank()) {
                Text(if (busy) "Создание…" else "Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Отмена") }
        }
    )
}

private val inlineMd = Regex(
    "\\[([^\\]]+)\\]\\(([^)\\s]+)\\)" +
        "|https?://[^\\s)\\]]+" +
        "|(`{1,3})([^`].*?)\\3" +
        "|\\*\\*([^*]+)\\*\\*"
)

private fun AnnotatedString.Builder.appendInlineMarkdown(text: String, linkColor: Color) {
    var i = 0
    for (m in inlineMd.findAll(text)) {
        if (m.range.first > i) append(text.substring(i, m.range.first))
        val value = m.value
        val linkStyle = TextLinkStyles(
            style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
        )
        when {
            m.groups[1] != null && m.groups[2] != null ->
                withLink(LinkAnnotation.Url(m.groupValues[2], linkStyle)) {
                    appendInlineMarkdown(m.groupValues[1], linkColor)
                }

            value.startsWith("http") ->
                withLink(LinkAnnotation.Url(value, linkStyle)) { append(value) }

            m.groups[4] != null ->
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(m.groupValues[4].trim()) }

            m.groups[5] != null ->
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    appendInlineMarkdown(m.groupValues[5], linkColor)
                }
        }
        i = m.range.last + 1
    }
    if (i < text.length) append(text.substring(i))
}

private val urlJoin = Regex("(https?://[^\\s)\\]]+)\\n(/[^\\s)\\]]*)")

/** Начало/конец блока кода: ``` или ```язык и больше ничего в строке. */
private val codeFence = Regex("^\\s*```[A-Za-z0-9_+.\\-]*\\s*$")
private fun markdown(text: String, linkColor: Color): AnnotatedString = buildAnnotatedString {
    val lines = text.replace(urlJoin, "$1$2").split("\n")
    var inCode = false
    lines.forEachIndexed { idx, line ->
        if (codeFence.matches(line)) {
            inCode = !inCode
        } else if (inCode) {
            withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(line) }
        } else {
            appendInlineMarkdown(line, linkColor)
        }
        if (idx < lines.lastIndex) append("\n")
    }
}

// --- markdown-таблицы -------------------------------------------------------

private data class MdTable(val header: List<String>, val rows: List<List<String>>)

private val tableSeparator = Regex("^:?-{2,}:?$")

private fun splitTableRow(line: String): List<String> {
    var s = line.trim()
    if (s.startsWith("|")) s = s.substring(1)
    if (s.endsWith("|")) s = s.dropLast(1)
    return s.split("|").map { it.trim() }
}

private fun parseTableAt(lines: List<String>, start: Int): Pair<MdTable, Int>? {
    if (start + 1 >= lines.size) return null
    if (!lines[start].trim().startsWith("|")) return null
    if (!lines[start + 1].trim().startsWith("|")) return null
    val sep = splitTableRow(lines[start + 1])
    if (sep.isEmpty() || !sep.all { tableSeparator.matches(it.replace(" ", "")) }) return null
    val header = splitTableRow(lines[start])
    val rows = ArrayList<List<String>>()
    var i = start + 2
    while (i < lines.size && lines[i].trim().startsWith("|")) {
        rows.add(splitTableRow(lines[i]))
        i++
    }
    return MdTable(header, rows) to i
}

/** Делит текст на обычные куски и таблицы; блоки кода не трогает. */
private fun markdownParts(text: String): List<Any> {
    val lines = text.split("\n")
    val parts = ArrayList<Any>()
    val buf = ArrayList<String>()
    fun flush() {
        if (buf.isNotEmpty()) {
            parts.add(buf.joinToString("\n"))
            buf.clear()
        }
    }
    var inCode = false
    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        if (codeFence.matches(line)) {
            inCode = !inCode
            buf.add(line)
            i++
            continue
        }
        if (!inCode) {
            val table = parseTableAt(lines, i)
            if (table != null) {
                flush()
                parts.add(table.first)
                i = table.second
                continue
            }
        }
        buf.add(line)
        i++
    }
    flush()
    return parts
}

@Composable
private fun MarkdownView(text: String, linkColor: Color) {
    val parts = remember(text) { markdownParts(text.replace(urlJoin, "$1$2")) }
    Column {
        parts.forEach { part ->
            when (part) {
                is MdTable -> TableView(part, linkColor)
                else -> {
                    val s = part as String
                    if (s.isNotBlank()) Text(markdown(s, linkColor), fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun TableView(table: MdTable, linkColor: Color) {
    val columns = maxOf(
        table.header.size,
        table.rows.maxOfOrNull { it.size } ?: 0
    )
    if (columns == 0) return
    val cellWidths = remember(table) {
        (0 until columns).map { c ->
            val longest = (listOf(table.header.getOrNull(c) ?: "") +
                table.rows.map { it.getOrNull(c) ?: "" }).maxOf { it.length }
            (longest.coerceIn(5, 36) * 7.2f).dp
        }
    }
    val divider = MaterialTheme.colorScheme.outlineVariant
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(8.dp))
    ) {
        Row(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            for (c in 0 until columns) {
                TableCell(table.header.getOrNull(c) ?: "", cellWidths[c], linkColor, true)
            }
        }
        table.rows.forEachIndexed { index, row ->
            Row {
                for (c in 0 until columns) {
                    TableCell(row.getOrNull(c) ?: "", cellWidths[c], linkColor, false)
                }
            }
            if (index < table.rows.lastIndex) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(divider))
            }
        }
    }
}

@Composable
private fun TableCell(text: String, width: androidx.compose.ui.unit.Dp, linkColor: Color, header: Boolean) {
    Box(Modifier.width(width).padding(horizontal = 8.dp, vertical = 6.dp)) {
        Text(
            markdown(text, linkColor),
            fontSize = 13.sp,
            fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
