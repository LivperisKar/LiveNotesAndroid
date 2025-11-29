package gr.livperiskar.livenotes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import gr.livperiskar.livenotes.data.NoteEntity
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

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

    var localText by remember(currentNote?.id) {
        mutableStateOf(currentNote?.content ?: "")
    }

    LaunchedEffect(startMode) {
        when (startMode) {
            StartMode.KEYBOARD -> {
                delay(150)
                focusRequester.requestFocus()
                keyboardController?.show()
            }
            StartMode.NONE -> {
                keyboardController?.hide()
            }
        }
    }

    LaunchedEffect(focusRequestKey) {
        if (focusRequestKey > 0) {
            delay(120)
            focusRequester.requestFocus()
            if (startMode == StartMode.KEYBOARD) {
                keyboardController?.show()
            } else {
                keyboardController?.hide()
            }
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

    val baseIndicatorColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(indicatorColorDark)
    } else {
        Color(0xFF10A37F)
    }

    val animatedColor by animateColorAsState(
        targetValue = baseIndicatorColor,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "indicator_color"
    )

    val indicatorAlpha = if (isTyping) 1f else blinkAlpha

    val headerBackground = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF111111)
    } else {
        Color(0xFFEFEFF0)
    }

    val capsuleBackground = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF202020)
    } else {
        Color(0xFFD7D7E0)
    }

    val pageBackground = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF050505)
    } else {
        Color.White
    }

    val editorTextColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.White
    } else {
        Color(0xFF202123)
    }

    val placeholderColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color.Gray
    } else {
        Color(0xFF8A8B93)
    }

    val editIconColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(0xFF00FFFF)
    } else {
        Color(0xFF10A37F)
    }

    val effectiveCursorColor = if (appTheme == AppTheme.LIVENOTES_DARK) {
        Color(cursorColorDark)
    } else {
        Color(0xFF10A37F)
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
                    localText = ""
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

            // Waveform στο κέντρο
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                TypingWaveform(
                    isTyping = isTyping,
                    waveformStyle = waveformStyle
                )
            }

            // Blinking dot capsule
            Box(
                modifier = Modifier
                    .background(
                        color = capsuleBackground.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(999.dp)
                    )
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
                value = localText,
                onValueChange = { newText ->
                    localText = newText
                    onNoteContentChange(newText)
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

@Composable
private fun TypingWaveform(
    isTyping: Boolean,
    waveformStyle: WaveformStyle
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_bars")

    // ΠΑΝΤΑ έντονο κίτρινο LED
    val barColor = Color(0xFFFFD600)

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val baseHeight = 4.dp
    val maxExtra = 10.dp
    val idleFactor = 0.2f

    val amp1 = if (isTyping) bar1 else idleFactor
    val amp2 = if (isTyping) bar2 else idleFactor * 0.8f
    val amp3 = if (isTyping) bar3 else idleFactor * 0.6f

    fun barHeight(a: Float) = baseHeight + maxExtra * a

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.height(24.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(barHeight(amp1))
                .background(barColor.copy(alpha = 0.95f), RoundedCornerShape(999.dp))
        )

        Spacer(modifier = Modifier.width(3.dp))

        Box(
            modifier = Modifier
                .width(3.dp)
                .height(barHeight(amp2))
                .background(barColor.copy(alpha = 0.95f), RoundedCornerShape(999.dp))
        )

        Spacer(modifier = Modifier.width(3.dp))

        Box(
            modifier = Modifier
                .width(3.dp)
                .height(barHeight(amp3))
                .background(barColor.copy(alpha = 0.95f), RoundedCornerShape(999.dp))
        )
    }
}

