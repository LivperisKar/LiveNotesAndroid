package gr.livperiskar.livenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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

    // Όποτε αλλάζει το κείμενο → θεωρούμε ότι πληκτρολογεί
    LaunchedEffect(text) {
        if (isFocused) {
            isTyping = true
            val currentText = text
            // περιμένουμε 0.8s – αν το κείμενο δεν έχει αλλάξει, σταμάτησε να γράφει
            delay(800)
            if (text == currentText) {
                isTyping = false
            }
        } else {
            isTyping = false
        }
    }

    // animation για αναβόσβημα όταν δεν έχει focus
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

    // χρώμα:
    //  - μπλε/γαλάζιο μόνο όταν γράφει
    //  - πράσινο όταν δεν γράφει (είτε πριν το tap, είτε μετά το σταμάτημα)
    val indicatorColor = if (isTyping) {
        Color(0xFF00FFFF) // typing: γαλάζιο
    } else {
        Color(0xFF00FF00) // idle: πράσινο
    }

    // alpha:
    //  - αναβοσβήνει όταν δεν έχει focus (περιμένει πρώτο tap)
    //  - σταθερό όταν έχει focus (είτε γράφει είτε όχι)
    val indicatorAlpha = if (!isFocused) blinkAlpha else 1f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)   // μαύρο φόντο
            .imePadding()
            .padding(16.dp)
    ) {
        TextField(
            value = text,
            onValueChange = { newText ->
                text = newText
            },
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp
            ),
            singleLine = false,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black,
                disabledContainerColor = Color.Black,
                cursorColor = Color.Red,                     // κόκκινος κέρσορας
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Text(
                    text = "",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        )

        // φωτάκι κατάστασης – πάντα ορατό
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(10.dp)
                .alpha(indicatorAlpha)
                .background(indicatorColor, CircleShape)
        )
    }
}
