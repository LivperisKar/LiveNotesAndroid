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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ColumnScope
import gr.livperiskar.livenotes.data.NoteEntity
import gr.livperiskar.livenotes.ui.theme.LNBlue
import gr.livperiskar.livenotes.ui.theme.LNGreen
import gr.livperiskar.livenotes.ui.theme.LNYellow
import gr.livperiskar.livenotes.ui.theme.LNPink
import gr.livperiskar.livenotes.ui.theme.LNOrange
import gr.livperiskar.livenotes.ui.theme.LNPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

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
        Color(0xFFE35506).copy(alpha = 0.18f) // LNOrange, πολύ ήπιο
    } else {
        Color(0xFFE35506).copy(alpha = 0.12f)
    }

    val deleteTextColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFFE35506) // LNOrange
    }

    val iconColor = headerTextColor

    // Παλέτα για τα dots
    val dotPalette = listOf(
        LNBlue,
        LNGreen,
        LNYellow,
        LNPink,
        LNOrange,
        LNPurple
    )

    // Για κάθε note, δίνουμε ένα dotColor, πάντα διαφορετικό από το προηγούμενο
    val dotColors: List<Color> = remember(notes) {
        if (notes.isEmpty()) emptyList()
        else {
            val result = mutableListOf<Color>()
            var previous: Color? = null
            for (i in notes.indices) {
                val candidates = dotPalette.filter { it != previous }.ifEmpty { dotPalette }
                val color = candidates[Random.nextInt(candidates.size)]
                result += color
                previous = color
            }
            result
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    if (appTheme == AppTheme.LIVENOTES_DARK)
                        Color(0xFF000000).copy(alpha = 0.96f)  // LNBlack
                    else
                        Color(0xFFFFFEFE)                      // LNWhiteSoft
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
            // Search bar
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
                itemsIndexed(notes, key = { index, note -> note.id }) { index, note ->
                    val isSelected = selectedIds.contains(note.id)
                    val dotColor = dotColors.getOrNull(index)
                        ?: dotPalette[index % dotPalette.size]

                    NoteListItem(
                        note = note,
                        isSelected = isSelected,
                        appTheme = appTheme,
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
    val bgColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF333233) // LNDarkSurface
    } else {
        Color(0xFFFFFEFE) // LNWhiteSoft
    }

    val textColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF000000)
    }

    val placeholderColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFEFE).copy(alpha = 0.4f)
    } else {
        Color(0xFF414140) // LNDarkSurfaceAlt
    }

    val cursorColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF01A340) // LNGreen
    } else {
        Color(0xFF01A340)
    }

    // 🔹 ΚΟΙΝΟ style για input + placeholder
    val inputTextStyle = TextStyle(
        color = textColor,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontFamily = FontFamily.SansSerif
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(bgColor)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = placeholderColor,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = inputTextStyle,
                        cursorBrush = SolidColor(cursorColor),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (value.isEmpty()) {
                                Text(
                                    text = "Search notes...",
                                    // 🔹 ίδιο style, απλά άλλο χρώμα
                                    style = inputTextStyle.copy(color = placeholderColor),
                                    maxLines = 1
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                if (value.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onValueChange("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = placeholderColor,
                            modifier = Modifier.size(16.dp)
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

        // SECTION 1 – General
        SettingsSection(appTheme = appTheme, sectionTitle = "General") {
            SettingsRow(
                title = "Auto-save",
                subtitle = "Save notes while typing",
                appTheme = appTheme
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = if (appTheme == AppTheme.LIVENOTES_DARK)
                    Color(0xFFFFFFFF).copy(alpha = 0.08f)
                else
                    Color(0xFF333233).copy(alpha = 0.16f),
                thickness = 0.5.dp
            )

            SettingsRow(
                title = "Sort notes",
                subtitle = "Newest first",
                appTheme = appTheme
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION 2 – Startup behavior
        SettingsSection(appTheme = appTheme, sectionTitle = "Startup behavior") {
            SettingsRow(
                title = "Editor on app open",
                subtitle = "How the editor starts when the app opens",
                appTheme = appTheme
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Μόνο "None" – το Keyboard δεν εμφανίζεται πλέον
                ThemeChip(
                    label = "None",
                    selected = startMode == StartMode.NONE,
                    appTheme = appTheme,
                    onClick = { onStartModeChange(StartMode.NONE) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION 3 – Typing waveform
        SettingsSection(appTheme = appTheme, sectionTitle = "Typing waveform") {
            Text(
                text = "Waveform style",
                color = titleColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeChip(
                    label = "Bars",
                    selected = true,
                    appTheme = appTheme,
                    onClick = { onWaveformStyleChange(WaveformStyle.BARS) }
                )
            }
        }

        if (appTheme == AppTheme.LIVENOTES_DARK) {
            Spacer(modifier = Modifier.height(12.dp))

            // SECTION 4 – Cursor & indicator (dark)
            SettingsSection(appTheme = appTheme, sectionTitle = "Cursor & indicator (dark theme)") {

                Text(
                    text = "Cursor color",
                    color = titleColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(4.dp))

                val cursorColors = listOf(
                    0xFF01A340.toInt(), // green
                    0xFF0169CC.toInt(), // blue
                    0xFFE0AC00.toInt(), // yellow
                    0xFFE04D91.toInt(), // pink
                    0xFFFFFFFF.toInt()  // white
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cursorColors.forEach { colorInt ->
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

                val indicatorColors = listOf(
                    0xFFE0AC00.toInt(), // yellow
                    0xFF01A340.toInt(), // green
                    0xFF8046D9.toInt(), // purple
                    0xFFE04D91.toInt(), // pink
                    0xFFE35506.toInt()  // orange
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    indicatorColors.forEach { colorInt ->
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
        Color(0xFF333233) // LNDarkSurface
    } else {
        Color(0xFFFFFEFE) // LNWhiteSoft
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
        selected && appTheme == AppTheme.LIVENOTES_DARK -> Color(0xFF333233) // LNDarkSurface
        selected && appTheme == AppTheme.CHATGPT_LIGHT -> Color(0xFF0169CC).copy(alpha = 0.12f)
        !selected && appTheme == AppTheme.LIVENOTES_DARK -> Color(0xFF131618) // LNDarkBackground
        else -> Color(0xFFFFFEFE) // LNWhiteSoft
    }

    val textColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF000000)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
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
    dotColor: Color,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val bgColor = when {
        isSelected && appTheme == AppTheme.LIVENOTES_DARK -> Color(0xFF333233) // LNDarkSurface
        isSelected && appTheme == AppTheme.CHATGPT_LIGHT -> Color(0xFF0169CC).copy(alpha = 0.08f)
        !isSelected && appTheme == AppTheme.LIVENOTES_DARK -> Color(0xFF111111) // μπορείς να το κρατήσεις ή να το πας σε 0xFF131618
        else -> Color(0xFFFFFEFE) // LNWhiteSoft
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
            Text(
                text = if (note.title.isNotBlank()) note.title else "Untitled",
                color = titleColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = note.content,
                color = previewColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

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
