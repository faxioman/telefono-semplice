package it.rapidosoft.semplice

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.rapidosoft.semplice.call.Dialer
import it.rapidosoft.semplice.ui.BigButton
import it.rapidosoft.semplice.ui.Green
import it.rapidosoft.semplice.ui.Ink
import it.rapidosoft.semplice.ui.SempliceTheme
import it.rapidosoft.semplice.ui.Tile

/** Tastierone numerico. Riceve anche le richieste ACTION_DIAL di altre app. */
class KeypadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initial = intent?.data?.schemeSpecificPart.orEmpty()
        setContent { SempliceTheme { KeypadScreen(initial, onBack = { finish() }) } }
    }
}

@Composable
private fun KeypadScreen(initial: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var number by rememberSaveable { mutableStateOf(initial) }
    val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("*", "0", "#"))

    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(64.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), Modifier.size(40.dp), tint = Ink)
            }
            Text(
                number.ifEmpty { " " }, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Ink,
                textAlign = TextAlign.Center, maxLines = 1, modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { number = number.dropLast(1) },
                modifier = Modifier.size(64.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Backspace, stringResource(R.string.keypad_delete), Modifier.size(40.dp), tint = Ink)
            }
        }
        Column(Modifier.weight(1f).padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { row ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { key ->
                        Key(key, Modifier.weight(1f).fillMaxHeight(),
                            onLongClick = if (key == "0") ({ number += "+" }) else null) {
                            if (number.length < 20) number += key
                        }
                    }
                }
            }
        }
        BigButton(
            text = stringResource(R.string.keypad_call),
            icon = Icons.Filled.Call,
            color = Green,
            modifier = Modifier.fillMaxWidth().height(130.dp),
        ) { Dialer.call(context, number) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Key(label: String, modifier: Modifier, onLongClick: (() -> Unit)?, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(20.dp)).background(Tile)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Ink)
    }
}
