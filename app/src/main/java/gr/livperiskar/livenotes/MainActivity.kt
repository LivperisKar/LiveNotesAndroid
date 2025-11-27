package gr.livperiskar.livenotes

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gr.livperiskar.livenotes.ui.theme.LivenotesTheme
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.BoxWithConstraints

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LivenotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    MainScreenRoot()
                }
            }
        }
    }
}

/* --------------------- ROOT ΜΕ DRAWER SWIPE --------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenRoot() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.4f), // ελαφρύ dim πάνω από τον editor
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 320.dp),             // max πλάτος drawer
                drawerContainerColor = Color.Black      // ίδιο dark style
            ) {
                // η λίστα σημειώσεων μέσα στο drawer
                NotesListScreen()
            }
        }
    ) {
        // Κύρια οθόνη: ο editor
        LiveNotesEditorScreen()
    }
}


/* --------------------- EDITOR SCREEN --------------------- */

@Composable
fun LiveNotesEditorScreen() {
    var text by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }

    // Debounce πληκτρολόγησης
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

    // Αναβόσβημα όταν είναι σε αναμονή
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
        Color(0xFF00FFFF) // typing: γαλάζιο
    } else {
        Color(0xFF00FF00) // idle: πράσινο
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "indicator_color"
    )

    val indicatorAlpha = if (isTyping) 1f else blinkAlpha

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF111111))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Μολύβι νέας σημείωσης (καθαρίζει editor)
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

            // Capsule με φωτάκι
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFF202020),
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

        // "Φύλλο" σημείωσης
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    color = Color(0xFF050505),
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
                    color = Color.White,
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
                        color = Color.Gray,
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
fun NotesListScreen() {
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

    // state για toggle μεταξύ λίστας & ρυθμίσεων
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
    ) {
        // Header: All notes + δεξιά icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All notes",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.weight(1f))

            if (!selectionMode) {
                // ⚙ / ☰ toggle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable {
                            showSettings = !showSettings
                            // προαιρετικά, καθάρισε επιλογή αν είχες
                            selectedIds = emptySet()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showSettings) "☰" else "⚙",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            } else {
                // Εικονίδιο διαγραφής όταν υπάρχει επιλογή
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF401818))
                        .clickable {
                            // προς το παρόν: απλά καθαρίζει την επιλογή
                            selectedIds = emptySet()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🗑",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        if (showSettings) {
            // Εμφάνιση ρυθμίσεων αντί για λίστα
            NotesSettingsView()
        } else {
            // Κανονική λίστα σημειώσεων
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

@Composable
fun NotesSettingsView() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsRow(
            title = "Auto-save",
            subtitle = "Save notes while typing"
        )

        SettingsRow(
            title = "Sort notes",
            subtitle = "Newest first"
        )

        SettingsRow(
            title = "Theme",
            subtitle = "Dark"
        )
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListItem(
    note: NotePreview,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val bgColor = if (isSelected) Color(0xFF222233) else Color(0xFF111111)

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
                .background(Color(0xFF222222)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📝",
                color = Color.White,
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
                    color = Color.White,
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
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.timestamp,
                color = Color.White.copy(alpha = 0.5f),
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
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}
