package gr.livperiskar.livenotes

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import gr.livperiskar.livenotes.data.NoteEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenRoot(
    appTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    cursorColorDark: Int,
    cursorScaleDark: Float,
    onCursorColorDarkChange: (Int) -> Unit,
    onCursorScaleDarkChange: (Float) -> Unit,
    indicatorColorDark: Int,
    indicatorScaleDark: Float,
    onIndicatorColorDarkChange: (Int) -> Unit,
    onIndicatorScaleDarkChange: (Float) -> Unit,
    startMode: StartMode,
    onStartModeChange: (StartMode) -> Unit,
    waveformStyle: WaveformStyle,
    onWaveformStyleChange: (WaveformStyle) -> Unit,
    notesViewModel: NotesViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var showSettings by remember { mutableStateOf(false) }
    var editorFocusRequestKey by remember { mutableStateOf(0) }

    val uiState by notesViewModel.uiState.collectAsState()

    // 🔹 ΕΔΩ γίνεται το φιλτράρισμα: τίτλος + σώμα
    val notes: List<NoteEntity> = remember(uiState.notes, uiState.searchQuery) {
        val query = uiState.searchQuery.trim()
        if (query.isBlank()) {
            uiState.notes
        } else {
            uiState.notes.filter { note ->
                val (titleText, previewText) = note.resolveTitleAndPreview()
                titleText.contains(query, ignoreCase = true) ||
                        previewText.contains(query, ignoreCase = true)
            }
        }
    }


    // ΠΑΝΤΑ νέα κενή σημείωση στην εκκίνηση
    var hasStartedNewNote by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasStartedNewNote) {
            notesViewModel.startNewNote()
            hasStartedNewNote = true
        }
    }

    // Όταν ανοίγει / κλείνει το drawer
    LaunchedEffect(drawerState.currentValue) {
        when (drawerState.currentValue) {
            DrawerValue.Open -> {
                focusManager.clearFocus(force = true)
            }
            DrawerValue.Closed -> {
                showSettings = false
                editorFocusRequestKey++
                // Όταν κλείνει το drawer, καθάρισε την αναζήτηση
                notesViewModel.onSearchQueryChange("")
            }
        }
    }

    val scrim = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF000000).copy(alpha = 0.4f) // LNBlack
    } else {
        Color(0xFF000000).copy(alpha = 0.1f)
    }

    val drawerBg = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF000000) // LNBlack
    } else {
        Color(0xFFFFFEFE) // LNWhiteSoft
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = scrim,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 320.dp),
                drawerContainerColor = drawerBg
            ) {
                NotesListScreen(
                    appTheme = appTheme,
                    showSettings = showSettings,
                    onThemeChange = onThemeChange,
                    cursorColorDark = cursorColorDark,
                    cursorScaleDark = cursorScaleDark,
                    onCursorColorDarkChange = onCursorColorDarkChange,
                    onCursorScaleDarkChange = onCursorScaleDarkChange,
                    indicatorColorDark = indicatorColorDark,
                    indicatorScaleDark = indicatorScaleDark,
                    onIndicatorColorDarkChange = onIndicatorColorDarkChange,
                    onIndicatorScaleDarkChange = onIndicatorScaleDarkChange,
                    startMode = startMode,
                    onStartModeChange = onStartModeChange,
                    waveformStyle = waveformStyle,
                    onWaveformStyleChange = onWaveformStyleChange,
                    onToggleSettings = {
                        showSettings = !showSettings
                        // Μόλις πάει/γυρίσει από settings → καθάρισε αναζήτηση
                        notesViewModel.onSearchQueryChange("")
                    },
                    notes = notes,                          // 🔹 ΠΕΡΝΑΜΕ ΤΑ ΦΙΛΤΡΑΡΙΣΜΕΝΑ notes
                    searchQuery = uiState.searchQuery,      // 🔹 Για το highlight
                    onSearchQueryChange = {
                        notesViewModel.onSearchQueryChange(it)
                    },
                    onOpenNote = { id ->
                        notesViewModel.selectNote(id)
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onDeleteSelected = { ids ->
                        notesViewModel.deleteNotes(ids)
                    }
                )
            }
        }
    ) {
        LiveNotesEditorScreen(
            appTheme = appTheme,
            cursorColorDark = cursorColorDark,
            cursorScaleDark = cursorScaleDark,
            indicatorColorDark = indicatorColorDark,
            indicatorScaleDark = indicatorScaleDark,
            startMode = startMode,
            focusRequestKey = editorFocusRequestKey,
            currentNote = uiState.currentNote,
            waveformStyle = waveformStyle,
            onNoteContentChange = { notesViewModel.updateCurrentContent(it) },
            onNewNote = { notesViewModel.startNewNote() }
        )
    }
}
