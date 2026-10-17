package it.rapidosoft.semplice.ui

import android.graphics.drawable.ColorDrawable
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import it.rapidosoft.semplice.data.Store
import it.rapidosoft.semplice.data.ThemeMode

// Verde e rosso restano uguali nei due temi: sono "chiama" e "butta giù".
val Green = Color(0xFF1E8E3E)
val Red = Color(0xFFC62828)
val Amber = Color(0xFFB26A00)

@Immutable
data class Palette(
    val background: Color,
    val tile: Color,   // riquadri: contatti, tasti, righe
    val ink: Color,    // testo principale
    val muted: Color,  // testo secondario
    val accent: Color, // titoli di sezione
)

private val LightPalette = Palette(
    background = Color(0xFFF7F4EE), tile = Color.White, ink = Color(0xFF151515),
    muted = Color(0xFF6B6B6B), accent = Color(0xFF1B5E20),
)
private val DarkPalette = Palette(
    background = Color(0xFF101010), tile = Color(0xFF262626), ink = Color(0xFFF2F2F2),
    muted = Color(0xFFADADAD), accent = Color(0xFF81C784),
)

private val LocalPalette = staticCompositionLocalOf { LightPalette }

val Background: Color @Composable get() = LocalPalette.current.background
val Tile: Color @Composable get() = LocalPalette.current.tile
val Ink: Color @Composable get() = LocalPalette.current.ink
val Muted: Color @Composable get() = LocalPalette.current.muted
val Accent: Color @Composable get() = LocalPalette.current.accent

/** Tema dell'app; senza [dark] esplicito segue la preferenza salvata (chiaro/scuro). */
@Composable
fun SempliceTheme(dark: Boolean? = null, content: @Composable () -> Unit) {
    val context = LocalContext.current
    // dark esplicito = anteprime/test: niente preferenze né finestra da configurare
    val settings = if (dark == null) remember { Store.get(context) }.settings.collectAsState().value else null
    val isDark = dark ?: (settings?.theme == ThemeMode.DARK)
    val palette = if (isDark) DarkPalette else LightPalette
    val scheme = if (isDark) {
        darkColorScheme(
            primary = Color(0xFF81C784), onPrimary = Color(0xFF0B2E10), secondary = Color(0xFFB0BEC5),
            background = palette.background, onBackground = palette.ink,
            surface = Color(0xFF1C1C1C), onSurface = palette.ink, surfaceVariant = Color(0xFF2E2E2E),
            error = Color(0xFFEF5350),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF1B5E20), onPrimary = Color.White, secondary = Color(0xFF37474F),
            background = palette.background, onBackground = palette.ink,
            surface = Color.White, onSurface = palette.ink, surfaceVariant = Color(0xFFECE7DD),
            error = Red,
        )
    }

    // Barre di sistema e sfondo della finestra coerenti con il tema scelto.
    val activity = context as? ComponentActivity
    LaunchedEffect(isDark, settings?.keepAwake, settings?.brightness, activity) {
        activity ?: return@LaunchedEffect
        val transparent = android.graphics.Color.TRANSPARENT
        val style = if (isDark) SystemBarStyle.dark(transparent) else SystemBarStyle.light(transparent, transparent)
        activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        activity.window.setBackgroundDrawable(ColorDrawable(palette.background.toArgb()))
        if (activity is SempliceActivity && settings != null) {
            activity.hideNavigationBar()
            activity.applyScreenSettings(settings)
        }
    }

    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(colorScheme = scheme) {
            Surface(color = palette.background, contentColor = palette.ink, modifier = Modifier.fillMaxSize(), content = content)
        }
    }
}
