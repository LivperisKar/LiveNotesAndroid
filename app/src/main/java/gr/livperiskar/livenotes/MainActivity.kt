package gr.livperiskar.livenotes

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gr.livperiskar.livenotes.ui.theme.LivenotesTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// DataStore imports
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

/* --------------------- THEME SETUP --------------------- */

enum class AppTheme {
    Dark,
    Light
}

// Extension property για DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val THEME_KEY = stringPreferencesKey("app_theme")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DataStore εκτός composables
        val dataStore = applicationContext.dataStore

        // Flow που διαβάζει το αποθηκευμένο θέμα
        val themeFlow = dataStore.data.map { prefs ->
            when (prefs[THEME_KEY]) {
                "light" -> AppTheme.Light
                else -> AppTheme.Dark   // default: το δικό σου dark
            }
        }

        setContent {
            val currentTheme by themeFlow.collectAsState(initial = AppTheme.Dark)
            val scope = rememberCoroutineScope()

            val onThemeChange: (AppTheme) -> Unit = { newTheme ->
                scope.launch {
                    dataStore.edit { prefs ->
                        prefs[THEME_KEY] = when (newTheme) {
                            AppTheme.Dark -> "dark"
                            AppTheme.Light -> "light"
                        }
                    }
                }
            }

            LivenotesTheme {
                val windowColor =
                    if (currentTheme == AppTheme.Dark) Color.Black else Color(0xFFF5F5F5)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = windowColor
                ) {
                    MainScreenRoot(
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange
                    )
                }
            }
        }
    }
}

/* --------------------- ROOT ΜΕ DRAWER --------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenRoot(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.4f),
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 320.dp),
                drawerContainerColor =
                    if (currentTheme == AppTheme.Dark) Color.Black else Color.White
            ) {
                NotesListScreen(
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
            }
        }
    ) {
        LiveNotesEditorScreen(currentTheme = currentTheme)
    }
}

/* --------------------- EDITOR SCREEN --------------------- */

@Composable
fun LiveNotesEditorScreen(currentTheme: AppTheme) {
    var text by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }

    LaunchedEffect(text) {
        if (isFocused) {
            isTyping = true
            val currentText = text
            delay(800)
            if (text == currentText) {
                isTyping = false
            }
        } else {
            isTyping = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wait_indicator")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_anim"
    )

    val targetColor = if (isTyping) {
        Color(0xFF00FFFF)
    } else {
        Color(0xFF00FF00)
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "indicator_color"
    )

    val indicatorAlpha = if (isTyping) 1f else blinkAlpha

    val headerBg =
        if (currentTheme == AppTheme.Dark) Color(0xFF111111) else Color(0xFFEFEFEF)
    val sheetBg =
        if (currentTheme == AppTheme.Dark) Color(0xFF050505) else Color.White
    val textColor =
        if (currentTheme == AppTheme.Dark) Color.White else Color(0xFF111111)
    val placeholderColor =
        if (currentTheme == AppTheme.Dark) Color.Gray else Color(0xFF888888)

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(headerBg)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        text = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✎",
                    color = Color(0xFF00FFFF),
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .background(
                        color = if (currentTheme == AppTheme.Dark) Color(0xFF202020) else Color(
                            0xFFD0D0D0
                        ),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(indicatorAlpha)
                        .background(animatedColor, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    color = sheetBg,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
        ) {
            TextField(
                value = text,
                onValueChange = { newText ->
                    text = newText
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                singleLine = false,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = Color.Red,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = "Write a new note...",
                        color = placeholderColor,
                        fontSize = 17.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            )
        }
    }
}

/* --------------------- NOTES LIST SCREEN --------------------- */

data class NotePreview(
    val id: Int,
    val title: String,
    val preview: String,
    val timestamp: String,
    val isPinned: Boolean = false
)

@Composable
fun NotesListScreen(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val notes = remember {
        listOf(
            NotePreview(
                id = 1,
                title = "Shopping list",
                preview = "Milk, coffee, bread, olive oil...",
                timestamp = "Today · 09:42",
                isPinned = true
            ),
            NotePreview(
                id = 2,
                title = "Project ideas",
                preview = "Build a minimal notes app with live preview and tagging...",
                timestamp = "Yesterday · 22:10"
            ),
            NotePreview(
                id = 3,
                title = "Meeting notes",
                preview = "Key points from today's standup: performance issues on API v2...",
                timestamp = "Mon · 18:30"
            ),
            NotePreview(
                id = 4,
                title = "Reading list",
                preview = "Nicholas of Cusa, De docta ignorantia, plus some articles...",
                timestamp = "Sun · 14:05"
            ),
            NotePreview(
                id = 5,
                title = "Random thoughts",
                preview = "Sometimes the best ideas come when I stop trying too hard...",
                timestamp = "Sat · 23:11"
            )
        )
    }

    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    val selectionMode = selectedIds.isNotEmpty()
    var showSettings by remember { mutableStateOf(false) }

    val headerTextColor =
        if (currentTheme == AppTheme.Dark) Color.White else Color(0xFF111111)

    Column(
        modifier = Modifier
            .fillMaxHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All notes",
                color = headerTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.weight(1f))

            if (!selectionMode) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable {
                            showSettings = !showSettings
                            selectedIds = emptySet()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showSettings) "☰" else "⚙",
                        color = headerTextColor,
                        fontSize = 18.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (currentTheme == AppTheme.Dark)
                                Color(0xFF401818)
                            else
                                Color(0xFFFFE0E0)
                        )
                        .clickable {
                            selectedIds = emptySet()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🗑",
                        color = headerTextColor,
                        fontSize = 16.sp
                    )
                }
            }
        }

        if (showSettings) {
            NotesSettingsView(
                currentTheme = currentTheme,
                onThemeChange = onThemeChange
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    val isSelected = selectedIds.contains(note.id)

                    NoteListItem(
                        note = note,
                        isSelected = isSelected,
                        currentTheme = currentTheme,
                        onClick = {
                            if (selectionMode) {
                                selectedIds =
                                    if (isSelected) selectedIds - note.id else selectedIds + note.id
                            } else {
                                // εδώ αργότερα: άνοιγμα σημείωσης
                            }
                        },
                        onLongPress = {
                            selectedIds =
                                if (isSelected) selectedIds - note.id else selectedIds + note.id
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/* --------------------- SETTINGS VIEW --------------------- */

@Composable
fun NotesSettingsView(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val textColor =
        if (currentTheme == AppTheme.Dark) Color.White else Color(0xFF111111)
    val secondaryColor = textColor.copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Settings",
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsRow(
            title = "Auto-save",
            subtitle = "Save notes while typing",
            titleColor = textColor,
            subtitleColor = secondaryColor
        )

        SettingsRow(
            title = "Sort notes",
            subtitle = "Newest first",
            titleColor = textColor,
            subtitleColor = secondaryColor
        )

        ThemeSettingsRow(
            currentTheme = currentTheme,
            onThemeChange = onThemeChange,
            titleColor = textColor,
            subtitleColor = secondaryColor
        )
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    titleColor: Color,
    subtitleColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = titleColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = subtitleColor,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun ThemeSettingsRow(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    titleColor: Color,
    subtitleColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val newTheme =
                    if (currentTheme == AppTheme.Dark) AppTheme.Light else AppTheme.Dark
                onThemeChange(newTheme)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Theme",
                color = titleColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (currentTheme == AppTheme.Dark) "Dark" else "Light",
                color = subtitleColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(
                    if (currentTheme == AppTheme.Dark) Color(0xFF222233) else Color(0xFFE0E0E0)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (currentTheme == AppTheme.Dark) "Switch to Light" else "Switch to Dark",
                color = titleColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

/* --------------------- NOTE LIST ITEM --------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListItem(
    note: NotePreview,
    isSelected: Boolean,
    currentTheme: AppTheme,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val baseBg =
        if (currentTheme == AppTheme.Dark) Color(0xFF111111) else Color(0xFFF0F0F0)
    val selectedBg =
        if (currentTheme == AppTheme.Dark) Color(0xFF222233) else Color(0xFFE0E0FF)
    val bgColor = if (isSelected) selectedBg else baseBg

    val cardInnerBg =
        if (currentTheme == AppTheme.Dark) Color(0xFF222222) else Color(0xFFE5E5E5)
    val mainTextColor =
        if (currentTheme == AppTheme.Dark) Color.White else Color(0xFF111111)
    val previewTextColor = mainTextColor.copy(alpha = 0.7f)
    val timestampColor = mainTextColor.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cardInnerBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📝",
                color = mainTextColor,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = note.title,
                    color = mainTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.isPinned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• pinned",
                        color = Color(0xFF00FFFF),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = note.preview,
                color = previewTextColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.timestamp,
                color = timestampColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif
            )
        }

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⋯",
                color = timestampColor,
                fontSize = 14.sp
            )
        }
    }
}
