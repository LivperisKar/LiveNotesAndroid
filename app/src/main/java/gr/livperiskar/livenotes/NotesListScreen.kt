package gr.livperiskar.livenotes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gr.livperiskar.livenotes.data.NoteEntity
import gr.livperiskar.livenotes.ui.theme.LNBlue
import gr.livperiskar.livenotes.ui.theme.LNGreen
import gr.livperiskar.livenotes.ui.theme.LNOrange
import gr.livperiskar.livenotes.ui.theme.LNPink
import gr.livperiskar.livenotes.ui.theme.LNPurple
import gr.livperiskar.livenotes.ui.theme.LNYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.ColumnScope

@Composable
fun NotesListScreen(
    appTheme: AppTheme,
    showSettings: Boolean,
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
    onToggleSettings: () -> Unit,
    notes: List<NoteEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenNote: (Long) -> Unit,
    onDeleteSelected: (Set<Long>) -> Unit
) {
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val selectionMode = selectedIds.isNotEmpty()

    val headerTextColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF000000)
    }

    val deleteBgColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFE35506).copy(alpha = 0.18f)
    } else {
        Color(0xFFE35506).copy(alpha = 0.12f)
    }

    val deleteTextColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFFE35506)
    }

    val iconColor = headerTextColor

    val dotPalette = listOf(
        LNBlue,
        LNGreen,
        LNYellow,
        LNPink,
        LNOrange,
        LNPurple
    )

    fun dotColorFor(note: NoteEntity): Color {
        val seedSource = if (note.id != 0L) note.id else note.createdAt
        val seed = seedSource.hashCode()
        val index = (seed and Int.MAX_VALUE) % dotPalette.size
        return dotPalette[index]
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    if (appTheme == AppTheme.LIVENOTES_DARK)
                        Color(0xFF000000).copy(alpha = 0.96f)
                    else
                        Color(0xFFFFFEFE)
                )
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
                IconButton(
                    onClick = {
                        selectedIds = emptySet()
                        onToggleSettings()
                    }
                ) {
                    Icon(
                        imageVector = if (showSettings) Icons.Filled.Menu else Icons.Filled.Settings,
                        contentDescription = if (showSettings) "Back to notes" else "Open settings",
                        tint = iconColor
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(deleteBgColor)
                        .clickable {
                            onDeleteSelected(selectedIds)
                            selectedIds = emptySet()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete selected",
                        tint = deleteTextColor
                    )
                }
            }
        }

        if (showSettings) {
            NotesSettingsView(
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
                onWaveformStyleChange = onWaveformStyleChange
            )
        } else {
            SearchBar(
                appTheme = appTheme,
                value = searchQuery,
                onValueChange = onSearchQueryChange
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp)
            ) {
                itemsIndexed(notes, key = { _, note -> note.id }) { _, note ->
                    val isSelected = selectedIds.contains(note.id)
                    val dotColor = dotColorFor(note)

                    NoteListItem(
                        note = note,
                        isSelected = isSelected,
                        appTheme = appTheme,
                        searchQuery = searchQuery,
                        dotColor = dotColor,
                        onClick = {
                            if (selectionMode) {
                                selectedIds =
                                    if (isSelected) selectedIds - note.id else selectedIds + note.id
                            } else {
                                onOpenNote(note.id)
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
private fun SearchBar(
    appTheme: AppTheme,
    value: String,
    onValueChange: (String) -> Unit
) {
    val isDark = appTheme == AppTheme.LIVENOTES_DARK

    val containerColor = if (isDark) {
        Color(0xFF212020)
    } else {
        Color(0xFFF2F2F2)
    }

    val borderColor = if (isDark) {
        Color(0xFF424242)
    } else {
        Color(0xFFE0E0E0)
    }

    val textColor = if (isDark) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF000000)
    }

    val placeholderColor = if (isDark) {
        Color(0xFFB0B0B1)
    } else {
        Color(0xFF757575)
    }

    val cursorColor = Color(0xFF00C26F)

    val inputTextStyle = TextStyle(
        color = textColor,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontFamily = FontFamily.SansSerif
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(containerColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = placeholderColor,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = inputTextStyle,
                    cursorBrush = SolidColor(cursorColor),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = "Search notes...",
                                style = inputTextStyle.copy(color = placeholderColor),
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                )

                if (value.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { onValueChange("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = placeholderColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotesSettingsView(
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
    onWaveformStyleChange: (WaveformStyle) -> Unit
) {
    val titleColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF000000)
    }

    // Ενιαία παλέτα για cursor & blinking dot – προστέθηκε και κόκκινο
    val accentColors = listOf(
        0xFF01A340.toInt(), // green
        0xFF0169CC.toInt(), // blue
        0xFFE0AC00.toInt(), // yellow
        0xFFE04D91.toInt(), // pink
        0xFFFFFFFF.toInt(), // white
        0xFF8046D9.toInt(), // purple
        0xFFE35506.toInt(), // orange
        0xFFFF4444.toInt()  // red
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Settings",
            color = titleColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Theme toggle
        SettingsSection(
            appTheme = appTheme,
            sectionTitle = "Theme"
        ) {
            Text(
                text = "Appearance",
                color = titleColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeChip(
                    label = "LiveNotes dark",
                    selected = appTheme == AppTheme.LIVENOTES_DARK,
                    appTheme = appTheme,
                    onClick = { onThemeChange(AppTheme.LIVENOTES_DARK) }
                )

                ThemeChip(
                    label = "ChatGPT light",
                    selected = appTheme == AppTheme.CHATGPT_LIGHT,
                    appTheme = appTheme,
                    onClick = { onThemeChange(AppTheme.CHATGPT_LIGHT) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cursor & indicator – πάντα ορατό
        SettingsSection(
            appTheme = appTheme,
            sectionTitle = "Cursor & indicator"
        ) {

            if (appTheme == AppTheme.CHATGPT_LIGHT) {
                Text(
                    text = "These settings are used in the LiveNotes dark editor.",
                    color = titleColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "Cursor color",
                color = titleColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accentColors.forEach { colorInt ->
                    val isSelected = cursorColorDark == colorInt
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 24.dp else 20.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(
                                width = 2.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onCursorColorDarkChange(colorInt) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Blinking dot color",
                color = titleColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accentColors.forEach { colorInt ->
                    val isSelected = indicatorColorDark == colorInt
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 24.dp else 20.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(
                                width = 2.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onIndicatorColorDarkChange(colorInt) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Blinking dot size",
                color = titleColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = indicatorScaleDark,
                onValueChange = { onIndicatorScaleDarkChange(it) },
                valueRange = 0.8f..1.6f,
                steps = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cursor thickness & text size",
                color = titleColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = cursorScaleDark,
                onValueChange = { onCursorScaleDarkChange(it) },
                valueRange = 0.8f..1.4f,
                steps = 3
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsSection(
    appTheme: AppTheme,
    sectionTitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardBg = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF333233)
    } else {
        Color(0xFFFFFEFE)
    }

    val borderColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF).copy(alpha = 0.08f)
    } else {
        Color(0xFF333233).copy(alpha = 0.18f)
    }

    val titleColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF).copy(alpha = 0.85f)
    } else {
        Color(0xFF000000)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(
                width = 0.7.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = sectionTitle,
            color = titleColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif
        )

        Spacer(modifier = Modifier.height(6.dp))

        content()
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    appTheme: AppTheme
) {
    val titleColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF000000)
    }

    val subtitleColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFEFE).copy(alpha = 0.6f)
    } else {
        Color(0xFF414140)
    }

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
fun ThemeChip(
    label: String,
    selected: Boolean,
    appTheme: AppTheme,
    onClick: () -> Unit
) {
    val bgColor = when {
        selected && appTheme == AppTheme.LIVENOTES_DARK ->
            Color(0xFF0169CC).copy(alpha = 0.18f)
        selected && appTheme == AppTheme.CHATGPT_LIGHT ->
            Color(0xFF0169CC).copy(alpha = 0.08f)
        !selected && appTheme == AppTheme.LIVENOTES_DARK ->
            Color(0xFF131618)
        else ->
            Color(0xFFFFFEFE)
    }

    val textColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF000000)
    }

    val borderColor = when {
        selected && appTheme == AppTheme.LIVENOTES_DARK ->
            Color(0xFFFFFFFF).copy(alpha = 0.35f)
        selected && appTheme == AppTheme.CHATGPT_LIGHT ->
            Color(0xFF0169CC).copy(alpha = 0.35f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .border(
                width = 0.6.dp,
                color = borderColor,
                shape = RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListItem(
    note: NoteEntity,
    isSelected: Boolean,
    appTheme: AppTheme,
    searchQuery: String,
    dotColor: Color,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val bgColor = when {
        isSelected && appTheme == AppTheme.LIVENOTES_DARK -> Color(0xFF333233)
        isSelected && appTheme == AppTheme.CHATGPT_LIGHT -> Color(0xFF0169CC).copy(alpha = 0.08f)
        !isSelected && appTheme == AppTheme.LIVENOTES_DARK -> Color(0xFF111111)
        else -> Color(0xFFFFFEFE)
    }

    val titleColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF000000)
    }

    val previewColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFEFE).copy(alpha = 0.7f)
    } else {
        Color(0xFF414140)
    }

    val timestampColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFEFE).copy(alpha = 0.5f)
    } else {
        Color(0xFF333233)
    }

    val highlightBgColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        LNYellow.copy(alpha = 0.35f)
    } else {
        LNBlue.copy(alpha = 0.25f)
    }

    val displayTexts: Pair<String, String> = remember(note.title, note.content) {
        note.resolveTitleAndPreview()
    }
    val titleText: String = displayTexts.first
    val previewText: String = displayTexts.second

    val createdDateText = remember(note.createdAt) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.format(Date(note.createdAt))
    }

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
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            HighlightedText(
                text = titleText,
                query = searchQuery,
                baseStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif
                ),
                normalColor = titleColor,
                highlightBgColor = highlightBgColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (previewText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))

                HighlightedText(
                    text = previewText,
                    query = searchQuery,
                    baseStyle = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    normalColor = previewColor,
                    highlightBgColor = highlightBgColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Created: $createdDateText",
                    color = timestampColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

@Composable
private fun HighlightedText(
    text: String,
    query: String,
    baseStyle: TextStyle,
    normalColor: Color,
    highlightBgColor: Color,
    maxLines: Int,
    overflow: TextOverflow
) {
    if (query.isBlank()) {
        Text(
            text = text,
            color = normalColor,
            style = baseStyle,
            maxLines = maxLines,
            overflow = overflow
        )
        return
    }

    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()

    val annotated = buildAnnotatedString {
        var startIndex = 0
        var index = lowerText.indexOf(lowerQuery, startIndex)

        while (index >= 0) {
            if (index > startIndex) {
                append(text.substring(startIndex, index))
            }

            withStyle(
                style = SpanStyle(
                    background = highlightBgColor
                )
            ) {
                append(text.substring(index, index + query.length))
            }

            startIndex = index + query.length
            index = lowerText.indexOf(lowerQuery, startIndex)
        }

        if (startIndex < text.length) {
            append(text.substring(startIndex))
        }
    }

    Text(
        text = annotated,
        style = baseStyle,
        color = normalColor,
        maxLines = maxLines,
        overflow = overflow
    )
}
