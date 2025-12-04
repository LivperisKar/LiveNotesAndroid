package gr.livperiskar.livenotes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import gr.livperiskar.livenotes.data.NoteEntity
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun LiveNotesEditorScreen(
    appTheme: AppTheme,
    cursorColorDark: Int,
    cursorScaleDark: Float,
    indicatorColorDark: Int,
    indicatorScaleDark: Float,
    startMode: StartMode,
    focusRequestKey: Int,
    currentNote: NoteEntity?,
    waveformStyle: WaveformStyle,
    onNoteContentChange: (String) -> Unit,
    onNewNote: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var isFocused by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }

    // Χρησιμοποιούμε TextFieldValue για να ξέρουμε τη θέση του cursor
    var textFieldValue by remember(currentNote?.id) {
        mutableStateOf(TextFieldValue(currentNote?.content ?: ""))
    }
    val localText = textFieldValue.text

    // 🔹 ΠΑΝΤΑ δίνουμε focus, ΠΟΤΕ δεν ανοίγουμε keyboard αυτόματα
    LaunchedEffect(startMode) {
        delay(150)
        focusRequester.requestFocus()
        keyboardController?.hide()
    }

    LaunchedEffect(focusRequestKey) {
        if (focusRequestKey > 0) {
            delay(120)
            focusRequester.requestFocus()
            keyboardController?.hide()
        }
    }

    LaunchedEffect(localText) {
        if (isFocused) {
            isTyping = true
            val snapshot = localText
            delay(800)
            if (localText == snapshot) {
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

    // Χρώμα του dot
    val baseIndicatorColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(indicatorColorDark)
    } else {
        Color(0xFF01A340) // πράσινο
    }

    val animatedColor by animateColorAsState(
        targetValue = baseIndicatorColor,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "indicator_color"
    )

    // Διαφάνεια (blink vs steady όταν γράφεις)
    val indicatorAlpha = if (isTyping) 1f else blinkAlpha

    val headerBackground = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF000000) // LNBlack
    } else {
        Color(0xFFFFFEFE) // LNWhiteSoft
    }

    val pageBackground = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF131618) // LNDarkBackground
    } else {
        Color(0xFFFFFEFE) // LNWhiteSoft
    }

    val editorTextColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFFFF) // LNWhite
    } else {
        Color(0xFF000000) // LNBlack
    }

    val placeholderColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFFFFEFE).copy(alpha = 0.6f)
    } else {
        Color(0xFF414140)
    }

    val editIconColor = Color(0xFF0169CC) // LNBlue (ίδιο σε light/dark)

    val reminderIconColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFFE0AC00) // alert yellow
    } else {
        Color(0xFFE35506) // orange
    }

    val effectiveCursorColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(cursorColorDark)
    } else {
        Color(0xFF01A340) // LNGreen
    }

    val baseFontSize = 17.sp
    val baseLineHeight = 24.sp

    val fontSize = if (appTheme == AppTheme.LIVENOTES_DARK) {
        baseFontSize * cursorScaleDark
    } else {
        baseFontSize
    }

    val lineHeight = if (appTheme == AppTheme.LIVENOTES_DARK) {
        baseLineHeight * cursorScaleDark
    } else {
        baseLineHeight
    }

    val baseDotSize = 6.dp
    val dotSize = if (appTheme == AppTheme.LIVENOTES_DARK) {
        baseDotSize * indicatorScaleDark
    } else {
        baseDotSize
    }

    // Μικρό animation στο καμπανάκι (pulse)
    var bellPressed by remember { mutableStateOf(false) }
    val bellScale by animateFloatAsState(
        targetValue = if (bellPressed) 1.1f else 1f,
        animationSpec = tween(durationMillis = 120, easing = LinearEasing),
        label = "bell_scale"
    )

    LaunchedEffect(bellPressed) {
        if (bellPressed) {
            delay(120)
            bellPressed = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(headerBackground.copy(alpha = 0.94f))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // New note
            IconButton(
                onClick = {
                    onNewNote()
                    textFieldValue = TextFieldValue("")
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "New note",
                    tint = editIconColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Καμπανάκι στο κέντρο
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        bellPressed = true

                        // Δημιουργία inline reminder στη ΘΕΣΗ του cursor
                        val now = Date()
                        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val formattedNow = formatter.format(now)

                        val template = "@rmd $formattedNow | reminder text |"

                        val oldText = textFieldValue.text
                        val selStart = textFieldValue.selection.start
                        val selEnd = textFieldValue.selection.end
                        val start = min(selStart, selEnd).coerceAtLeast(0)
                        val end = max(selStart, selEnd).coerceAtMost(oldText.length)

                        val newText = buildString {
                            append(oldText.substring(0, start))
                            append(template)
                            append(oldText.substring(end))
                        }

                        val newCursor = start + template.length

                        textFieldValue = TextFieldValue(
                            text = newText,
                            selection = TextRange(newCursor)
                        )

                        // ενημέρωση ViewModel
                        onNoteContentChange(newText)
                    },
                    modifier = Modifier.scale(bellScale)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Add reminder",
                        tint = reminderIconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Blinking dot
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .alpha(indicatorAlpha)
                        .background(animatedColor, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Editor
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    color = pageBackground,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
        ) {
            TextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onNoteContentChange(newValue.text)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                textStyle = TextStyle(
                    color = editorTextColor,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontFamily = FontFamily.SansSerif
                ),
                singleLine = false,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = effectiveCursorColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = "Write a new note...",
                        color = placeholderColor,
                        fontSize = fontSize,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            )
        }
    }
}
