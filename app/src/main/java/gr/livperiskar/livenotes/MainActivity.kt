package gr.livperiskar.livenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

    companion object {
        private const val PREFS_NAME = "livenotes_prefs"
        private const val KEY_CURSOR_COLOR_DARK = "cursor_color_dark"
        private const val KEY_CURSOR_SCALE_DARK = "cursor_scale_dark"
        private const val KEY_BLINKING_DOT_SCALE = "blinking_dot_scale"

        // default cursor color για dark theme (mint)
        private val DEFAULT_CURSOR_COLOR_DARK = 0xFF43E9A9.toInt()
        private const val DEFAULT_CURSOR_SCALE_DARK = 1.0f
        private const val DEFAULT_BLINKING_DOT_SCALE = 1.0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ΠΑΝΤΑ dark theme
        val appTheme = AppTheme.LIVENOTES_DARK

        val initialCursorColorDark = loadCursorColorDarkFromPrefs()
        val initialCursorScaleDark = loadCursorScaleDarkFromPrefs()
        val initialBlinkingDotScale = loadBlinkingDotScaleFromPrefs()

        setContent {
            var cursorColorDark by remember { mutableStateOf(initialCursorColorDark) }
            var cursorScaleDark by remember { mutableStateOf(initialCursorScaleDark) }
            var blinkingDotScale by remember { mutableStateOf(initialBlinkingDotScale) }

            LivenotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    LiveNotesApp(
                        appTheme = appTheme,
                        cursorColorDark = cursorColorDark,
                        cursorScaleDark = cursorScaleDark,
                        blinkingDotScale = blinkingDotScale,
                        onCursorColorDarkChange = { newColor ->
                            cursorColorDark = newColor
                            saveCursorColorDarkToPrefs(newColor)
                        },
                        onCursorScaleDarkChange = { newScale ->
                            cursorScaleDark = newScale
                            saveCursorScaleDarkToPrefs(newScale)
                        },
                        onBlinkingDotScaleChange = { newScale ->
                            blinkingDotScale = newScale
                            saveBlinkingDotScaleToPrefs(newScale)
                        }
                    )
                }
            }
        }
    }

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

    private fun loadBlinkingDotScaleFromPrefs(): Float {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getFloat(KEY_BLINKING_DOT_SCALE, DEFAULT_BLINKING_DOT_SCALE)
    }

    private fun saveBlinkingDotScaleToPrefs(scale: Float) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_BLINKING_DOT_SCALE, scale)
            .apply()
    }
}
