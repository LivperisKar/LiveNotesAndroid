package gr.livperiskar.livenotes

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gr.livperiskar.livenotes.ui.theme.LivenotesTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LivenotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LiveNotesEditorScreen()
                }
            }
        }
    }
}

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

    // Στόχος χρώματος φωτακίου (πράσινο / γαλάζιο)
    val targetColor = if (isTyping) {
        Color(0xFF00FFFF) // typing: γαλάζιο
    } else {
        Color(0xFF00FF00) // idle: πράσινο
    }

    // Ομαλή μετάβαση χρώματος
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "indicator_color"
    )

    // Alpha: σταθερό όταν γράφει, αναβοσβήνει όταν idle
    val indicatorAlpha = if (isTyping) 1f else blinkAlpha

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)   // κύριο φόντο
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // HEADER: μόνο μολύβι αριστερά και φωτάκι δεξιά
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF111111))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Μολύβι νέας σημείωσης (πλάγιο, click → καθαρισμός editor)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            text = ""      // καθαρίζει τον text editor
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

                // Μπορούμε να αφήσουμε κενό ή μελλοντικά να μπει τίτλος
                Spacer(modifier = Modifier.weight(1f))

                // Μικρό status chip ΜΟΝΟ με φωτάκι
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

            // Μικρό κενό ανάμεσα στο header και το “φύλλο” της σημείωσης
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
                        cursorColor = Color.Red,                     // κόκκινος κέρσορας
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    placeholder = {
                        Text(
                            // αν το θες ελληνικά: "Γράψε νέα σημείωση..."
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
}
