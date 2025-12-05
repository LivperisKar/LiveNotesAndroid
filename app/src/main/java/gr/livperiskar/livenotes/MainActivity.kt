package gr.livperiskar.livenotes

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import gr.livperiskar.livenotes.ui.theme.LivenotesTheme

class MainActivity : ComponentActivity() {

    private val notesViewModel: NotesViewModel by viewModels()

    // Runtime permission για notifications (Android 13+)
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Προαιρετικά: Log / Toast αν θέλεις να ξέρεις αν δόθηκε η άδεια
        }

    companion object {
        private const val PREFS_NAME = "livenotes_prefs"

        private const val KEY_THEME = "app_theme"
        private const val KEY_CURSOR_COLOR_DARK = "cursor_color_dark"
        private const val KEY_CURSOR_SCALE_DARK = "cursor_scale_dark"
        private const val KEY_INDICATOR_COLOR_DARK = "indicator_color_dark"
        private const val KEY_INDICATOR_SCALE_DARK = "indicator_scale_dark"
        private const val KEY_START_MODE = "start_mode"
        private const val KEY_WAVEFORM_STYLE = "waveform_style"

        // Defaults – ΜΟΝΟ παλέτα
        private val DEFAULT_CURSOR_COLOR_DARK = 0xFF01A340.toInt()   // green
        private const val DEFAULT_CURSOR_SCALE_DARK = 1.0f
        private val DEFAULT_INDICATOR_COLOR_DARK = 0xFFE0AC00.toInt() // yellow
        private const val DEFAULT_INDICATOR_SCALE_DARK = 1.0f

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+ → ζητάμε ρητά άδεια για notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Αν ήρθαμε από notification, πάρε το noteId
        val startNoteId = intent?.getLongExtra(ReminderWorker.EXTRA_NOTE_ID, -1L) ?: -1L
        if (startNoteId > 0L) {
            notesViewModel.scheduleSelectNote(startNoteId)
        }

        val initialTheme = loadThemeFromPrefs()
        val initialCursorColorDark = loadCursorColorDarkFromPrefs()
        val initialCursorScaleDark = loadCursorScaleDarkFromPrefs()
        val initialIndicatorColorDark = loadIndicatorColorDarkFromPrefs()
        val initialIndicatorScaleDark = loadIndicatorScaleDarkFromPrefs()
        val initialStartMode = loadStartModeFromPrefs()
        val initialWaveformStyle = loadWaveformStyleFromPrefs()

        setContent {
            var currentTheme by remember { mutableStateOf(initialTheme) }
            var cursorColorDark by remember { mutableStateOf(initialCursorColorDark) }
            var cursorScaleDark by remember { mutableStateOf(initialCursorScaleDark) }
            var indicatorColorDark by remember { mutableStateOf(initialIndicatorColorDark) }
            var indicatorScaleDark by remember { mutableStateOf(initialIndicatorScaleDark) }
            var startMode by remember { mutableStateOf(initialStartMode) }
            var waveformStyle by remember { mutableStateOf(initialWaveformStyle) }

            LivenotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = when (currentTheme) {
                        AppTheme.LIVENOTES_DARK -> Color(0xFF131618)   // dark background
                        AppTheme.CHATGPT_LIGHT -> Color(0xFFFFFEFE)    // soft white
                    }
                ) {
                    LiveNotesApp(
                        appTheme = currentTheme,
                        onThemeChange = { newTheme ->
                            currentTheme = newTheme
                            saveThemeToPrefs(newTheme)
                        },
                        cursorColorDark = cursorColorDark,
                        cursorScaleDark = cursorScaleDark,
                        onCursorColorDarkChange = { newColor ->
                            cursorColorDark = newColor
                            saveCursorColorDarkToPrefs(newColor)
                        },
                        onCursorScaleDarkChange = { newScale ->
                            cursorScaleDark = newScale
                            saveCursorScaleDarkToPrefs(newScale)
                        },
                        indicatorColorDark = indicatorColorDark,
                        indicatorScaleDark = indicatorScaleDark,
                        onIndicatorColorDarkChange = { newColor ->
                            indicatorColorDark = newColor
                            saveIndicatorColorDarkToPrefs(newColor)
                        },
                        onIndicatorScaleDarkChange = { newScale ->
                            indicatorScaleDark = newScale
                            saveIndicatorScaleDarkToPrefs(newScale)
                        },
                        startMode = startMode,
                        onStartModeChange = { newMode ->
                            startMode = newMode
                            saveStartModeToPrefs(newMode)
                        },
                        waveformStyle = waveformStyle,
                        onWaveformStyleChange = { newStyle ->
                            waveformStyle = newStyle
                            saveWaveformStyleToPrefs(newStyle)
                        },
                        notesViewModel = notesViewModel
                    )
                }
            }
        }
    }

    // Αν η Activity είναι ήδη ανοιχτή και έρθει νέο Intent (από notification tap)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val noteId = intent.getLongExtra(ReminderWorker.EXTRA_NOTE_ID, -1L)
        if (noteId > 0L) {
            notesViewModel.scheduleSelectNote(noteId)
        }
    }


    // ---------- THEME PREFS ----------

    private fun loadThemeFromPrefs(): AppTheme {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val value = prefs.getString(KEY_THEME, null)
        return AppTheme.fromPrefs(value)
    }

    private fun saveThemeToPrefs(theme: AppTheme) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_THEME, theme.prefsValue)
            .apply()
    }

    // ---------- CURSOR COLOR (DARK) ----------

    private fun loadCursorColorDarkFromPrefs(): Int {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getInt(KEY_CURSOR_COLOR_DARK, DEFAULT_CURSOR_COLOR_DARK)
    }

    private fun saveCursorColorDarkToPrefs(color: Int) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_CURSOR_COLOR_DARK, color)
            .apply()
    }

    // ---------- CURSOR SCALE (DARK) ----------

    private fun loadCursorScaleDarkFromPrefs(): Float {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getFloat(KEY_CURSOR_SCALE_DARK, DEFAULT_CURSOR_SCALE_DARK)
    }

    private fun saveCursorScaleDarkToPrefs(scale: Float) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_CURSOR_SCALE_DARK, scale)
            .apply()
    }

    // ---------- INDICATOR COLOR (BLINKING DOT, DARK) ----------

    private fun loadIndicatorColorDarkFromPrefs(): Int {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getInt(KEY_INDICATOR_COLOR_DARK, DEFAULT_INDICATOR_COLOR_DARK)
    }

    private fun saveIndicatorColorDarkToPrefs(color: Int) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_INDICATOR_COLOR_DARK, color)
            .apply()
    }

    // ---------- INDICATOR SCALE (BLINKING DOT SIZE, DARK) ----------

    private fun loadIndicatorScaleDarkFromPrefs(): Float {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getFloat(KEY_INDICATOR_SCALE_DARK, DEFAULT_INDICATOR_SCALE_DARK)
    }

    private fun saveIndicatorScaleDarkToPrefs(scale: Float) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_INDICATOR_SCALE_DARK, scale)
            .apply()
    }

    // ---------- START MODE (KEYBOARD / NONE) ----------

    private fun loadStartModeFromPrefs(): StartMode {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val value = prefs.getString(KEY_START_MODE, null)
        return StartMode.fromPrefs(value)
    }

    private fun saveStartModeToPrefs(mode: StartMode) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_START_MODE, mode.prefsValue)
            .apply()
    }

    // ---------- WAVEFORM STYLE ----------

    private fun loadWaveformStyleFromPrefs(): WaveformStyle {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val value = prefs.getString(KEY_WAVEFORM_STYLE, null)
        return WaveformStyle.fromPrefs(value)
    }

    private fun saveWaveformStyleToPrefs(style: WaveformStyle) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_WAVEFORM_STYLE, style.prefsValue)
            .apply()
    }
}
