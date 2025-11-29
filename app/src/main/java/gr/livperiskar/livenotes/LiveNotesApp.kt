package gr.livperiskar.livenotes

import androidx.compose.runtime.Composable

@Composable
fun LiveNotesApp(
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
    MainScreenRoot(
        appTheme = appTheme,
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
        notesViewModel = notesViewModel
    )
}
