package it.rapidosoft.semplice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import it.rapidosoft.semplice.data.Contact
import it.rapidosoft.semplice.ui.Green
import it.rapidosoft.semplice.ui.SempliceTheme
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.LocalDateTime

/**
 * Grafica per la scheda del Play Store, in tutte le lingue.
 * Genera:  ./gradlew -PscreenshotSdk=35 recordPaparazziFullDebug --tests '*StoreAssetsTest*'
 * Copia:   python3 tools/copy_store_assets.py
 */
class StoreAssetsTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = PHONE, useDeviceResolution = true, supportsRtl = true)

    private class Locale(val app: String, val play: String, val names: List<String>)

    private val locales = listOf(
        Locale("it", "it-IT", listOf("Giulia", "Marco", "Dottor Rossi", "Nonna Lina")),
        Locale("en", "en-US", listOf("Emma", "James", "Dr. Smith", "Grandma Rose")),
        Locale("es", "es-ES", listOf("Lucía", "Javier", "Dra. García", "Abuela Carmen")),
        Locale("fr", "fr-FR", listOf("Camille", "Lucas", "Dr Martin", "Mamie Jeanne")),
        Locale("de", "de-DE", listOf("Anna", "Lukas", "Dr. Müller", "Oma Grete")),
        Locale("pt", "pt-PT", listOf("Maria", "João", "Dr. Silva", "Avó Rosa")),
        Locale("ru", "ru-RU", listOf("Анна", "Иван", "Доктор Петров", "Бабушка Валя")),
        Locale("ar", "ar", listOf("ليلى", "أحمد", "د. خالد", "جدتي فاطمة")),
        Locale("hi", "hi-IN", listOf("प्रिया", "राहुल", "डॉ. शर्मा", "दादी माँ")),
        Locale("zh", "zh-CN", listOf("小美", "小明", "王医生", "奶奶")),
    )
    private val now = LocalDateTime.of(2026, 10, 16, 10, 30)

    @Test
    fun phoneScreenshots() {
        locales.forEach { l ->
            paparazzi.unsafeUpdateConfig(PHONE.copy(locale = l.app))
            // 3 contatti + Tastiera = due file piene, niente riquadri tagliati
            val contacts = l.names.take(3).mapIndexed { i, n -> Contact(id = "$i", name = n, number = "$i") }
            shot(l, 1) {
                HomeContent(contacts, showKeypad = true, callInProgress = false, setupNeeded = false,
                    onContact = {}, onKeypad = {}, onReturnToCall = {}, onSettings = {}, now = now)
            }
            shot(l, 2) {
                CallContent(stringResource(R.string.call_incoming), true, contacts[0].name, null,
                    CallMode.RINGING, null, {}, {}, {})
            }
            shot(l, 3) {
                CallContent("2:17", false, contacts[0].name, null, CallMode.ACTIVE, null, {}, {}, {})
            }
            shot(l, 4) { KeypadScreen("") {} }
            shot(l, 5, dark = true) {
                HomeContent(contacts, showKeypad = true, callInProgress = false, setupNeeded = false,
                    onContact = {}, onKeypad = {}, onReturnToCall = {}, onSettings = {}, now = now)
            }
        }
    }

    private fun shot(l: Locale, n: Int, dark: Boolean = false, content: @Composable () -> Unit) {
        paparazzi.snapshot(name = "phone-${l.play}-$n") { SempliceTheme(dark = dark) { content() } }
    }

    @Test
    fun icon() {
        paparazzi.unsafeUpdateConfig(MDPI.copy(screenWidth = 512, screenHeight = 512))
        paparazzi.snapshot(name = "icon") {
            Box(Modifier.fillMaxSize().background(Green)) {
                // il disegno adattivo ha margini di sicurezza: per l'icona dello store lo ingrandiamo
                Image(painterResource(R.drawable.ic_launcher_fg), null, Modifier.fillMaxSize().scale(1.6f))
            }
        }
    }

    @Test
    fun featureGraphics() {
        locales.forEach { l ->
            paparazzi.unsafeUpdateConfig(
                MDPI.copy(screenWidth = 1024, screenHeight = 500, orientation = ScreenOrientation.LANDSCAPE, locale = l.app)
            )
            val tagline = File("src/play/play/listings/${l.play}/short-description.txt").readText().trim()
            paparazzi.snapshot(name = "feature-${l.play}") { FeatureGraphic(tagline) }
        }
    }

    @Composable
    private fun FeatureGraphic(tagline: String) {
        Row(
            Modifier.fillMaxSize()
                .background(Brush.linearGradient(listOf(Color(0xFF1E8E3E), Color(0xFF0D5C26))))
                .padding(horizontal = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(280.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.14f))) {
                Image(painterResource(R.drawable.ic_launcher_fg), null, Modifier.fillMaxSize())
            }
            Spacer(Modifier.size(48.dp))
            Column {
                Text(stringResource(R.string.app_name), color = Color.White, fontSize = 62.sp,
                    fontWeight = FontWeight.Bold, lineHeight = 68.sp)
                Spacer(Modifier.size(16.dp))
                Text(tagline, color = Color.White.copy(alpha = 0.92f), fontSize = 30.sp, lineHeight = 38.sp)
            }
        }
    }

    private companion object {
        // 1080x1920 (9:16): il Play Store non accetta screenshot più allungati di 2:1.
        val PHONE = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 1920)
        // mdpi: 1dp = 1px, per avere icona e grafica alle dimensioni esatte in pixel.
        val MDPI = DeviceConfig.PIXEL_5.copy(xdpi = 160, ydpi = 160, density = Density.MEDIUM)
    }
}
