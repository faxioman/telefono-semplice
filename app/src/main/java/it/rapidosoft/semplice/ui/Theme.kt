package it.rapidosoft.semplice.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

val Background = Color(0xFFF7F4EE)
val Ink = Color(0xFF151515)
val Green = Color(0xFF1E8E3E)
val Red = Color(0xFFC62828)
val Muted = Color(0xFF6B6B6B)

private val Colors = lightColorScheme(
    primary = Color(0xFF1B5E20),
    onPrimary = Color.White,
    secondary = Color(0xFF37474F),
    background = Background,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFECE7DD),
    error = Red,
)

@Composable
fun SempliceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors) {
        Surface(color = Background, modifier = Modifier.fillMaxSize(), content = content)
    }
}
