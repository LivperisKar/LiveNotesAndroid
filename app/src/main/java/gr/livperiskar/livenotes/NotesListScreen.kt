package gr.livperiskar.livenotes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
        Color.White
    } else {
        Color(0xFF202123)
    }

    val deleteBgColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF401818)
    } else {
        Color(0xFFFFE0E0)
    }

    val deleteTextColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White
    } else {
        Color(0xFFAA0000)
    }

    val iconColor = headerTextColor

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
                        Color(0xFF111111).copy(alpha = 0.96f)
                    else
                        Color(0xFFF0F0F5)
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
                items(notes, key = { it.id }) { note ->
                    val isSelected = selectedIds.contains(note.id)

                    NoteListItem(
                        note = note,
                        isSelected = isSelected,
                        appTheme = appTheme,
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
        Color(0xFF19191F)
    } else {
        Color.White
    }

    val textColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White
    } else {
        Color(0xFF202123)
    }

    val placeholderColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White.copy(alpha = 0.4f)
    } else {
        Color(0xFF6B6C7B)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = placeholderColor
        )

        Spacer(modifier = Modifier.width(4.dp))

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
            singleLine = true,
            textStyle = TextStyle(
                color = textColor,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif
            ),
            placeholder = {
                Text(
                    text = "Search notes...",
                    color = placeholderColor,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = placeholderColor
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = textColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
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
        Color.White
    } else {
        Color(0xFF202123)
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
                    Color.White.copy(alpha = 0.08f)
                else
                    Color(0xFFE0E0E5),
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
                ThemeChip(
                    label = "Keyboard",
                    selected = startMode == StartMode.KEYBOARD,
                    appTheme = appTheme,
                    onClick = { onStartModeChange(StartMode.KEYBOARD) }
                )
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
                    0xFF43E9A9.toInt(),
                    0xFF00FFFF.toInt(),
                    0xFFFF6B6B.toInt(),
                    0xFFFFFFFF.toInt(),
                    0xFF00FF00.toInt()
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
                    0xFF00FF00.toInt(),
                    0xFF00FFFF.toInt(),
                    0xFFFFC857.toInt(),
                    0xFFBB86FC.toInt(),
                    0xFFFF6B6B.toInt()
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
        Color(0xFF15151C)
    } else {
        Color(0xFFF7F7FB)
    }

    val borderColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color(0xFFE0E0E8)
    }

    val titleColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White.copy(alpha = 0.85f)
    } else {
        Color(0xFF202123)
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
        Color.White
    } else {
        Color(0xFF202123)
    }

    val subtitleColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White.copy(alpha = 0.6f)
    } else {
        Color(0xFF6B6C7B)
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
        selected && appTheme == AppTheme.LIVENOTES_DARK -> Color(0xFF222233)
        selected && appTheme == AppTheme.CHATGPT_LIGHT -> Color(0xFFE0F2EA)
        !selected && appTheme == AppTheme.LIVENOTES_DARK -> Color(0xFF1A1A1A)
        else -> Color(0xFFF0F0F5)
    }

    val textColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White
    } else {
        Color(0xFF202123)
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
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val bgColor = when {
        isSelected && appTheme == AppTheme.LIVENOTES_DARK -> Color(0xFF222233)
        isSelected && appTheme == AppTheme.CHATGPT_LIGHT -> Color(0xFFE0F2EA)
        !isSelected && appTheme == AppTheme.LIVENOTES_DARK -> Color(0xFF111111)
        else -> Color.White
    }

    val iconBg = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF222222)
    } else {
        Color(0xFFF0F0F5)
    }

    val titleColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White
    } else {
        Color(0xFF202123)
    }

    val previewColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White.copy(alpha = 0.7f)
    } else {
        Color(0xFF4A4B53)
    }

    val timestampColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White.copy(alpha = 0.5f)
    } else {
        Color(0xFF9A9BA5)
    }

    val overflowColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White.copy(alpha = 0.5f)
    } else {
        Color(0xFF9A9BA5)
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
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Note",
                tint = titleColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

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

            Text(
                text = "Last updated: ${note.updatedAt}",
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
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More",
                tint = overflowColor
            )
        }
    }
}
