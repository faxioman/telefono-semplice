package it.rapidosoft.semplice

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.ScreenOrientation
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import it.rapidosoft.semplice.data.Contact
import it.rapidosoft.semplice.data.LogEntry
import it.rapidosoft.semplice.data.LogType
import it.rapidosoft.semplice.data.Sms
import it.rapidosoft.semplice.ui.SempliceTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/** Screenshot delle schermate principali: ./gradlew recordPaparazziDebug */
class ScreenshotsTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5.copy(locale = "it"))

    private val contacts = listOf(
        Contact(name = "Giulia", number = "1"),
        Contact(name = "Marco", number = "2"),
        Contact(name = "Dottor Rossi", number = "3"),
    )
    private val now = LocalDateTime.of(2026, 10, 16, 10, 30)

    @Test fun home() = paparazzi.snapshot {
        SempliceTheme(dark = false) {
            HomeContent(contacts, showKeypad = true, callInProgress = false, setupNeeded = false,
                onContact = {}, onKeypad = {}, onReturnToCall = {}, onSettings = {}, now = now)
        }
    }

    @Test fun homeCallInProgress() = paparazzi.snapshot {
        SempliceTheme(dark = false) {
            HomeContent(contacts, showKeypad = false, callInProgress = true, setupNeeded = false,
                onContact = {}, onKeypad = {}, onReturnToCall = {}, onSettings = {}, now = now)
        }
    }

    @Test fun incomingCall() = paparazzi.snapshot {
        SempliceTheme(dark = false) {
            CallContent("Ti sta chiamando", true, "Giulia", null, CallMode.RINGING, null, {}, {}, {})
        }
    }

    @Test fun activeCall() = paparazzi.snapshot {
        SempliceTheme(dark = false) {
            CallContent("2:17", false, "Giulia", null, CallMode.ACTIVE, null, {}, {}, {})
        }
    }

    @Test fun homeArabic() {
        paparazzi.unsafeUpdateConfig(DeviceConfig.PIXEL_5.copy(locale = "ar"))
        paparazzi.snapshot {
            SempliceTheme(dark = false) {
                HomeContent(contacts, showKeypad = true, callInProgress = false, setupNeeded = true,
                    onContact = {}, onKeypad = {}, onReturnToCall = {}, onSettings = {}, now = now)
            }
        }
    }

    @Test fun missedCallsAndSms() = paparazzi.snapshot {
        val t = 1_792_150_000_000L
        SempliceTheme(dark = false) {
            androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.padding(16.dp)) {
                LogRow(LogEntry("3331234567", t, LogType.MISSED), "Giulia") {}
                LogRow(LogEntry("0612345678", t - 3_600_000, LogType.BUSY), null) {}
                LogRow(LogEntry("", t - 7_200_000, LogType.BLOCKED), null) {}
                LogRow(LogEntry("3339876543", t - 86_400_000, LogType.REJECTED), "Marco") {}
                SmsRow(Sms("3331234567", "Ciao mamma, passo da te alle 17. Un bacio!", t, false), "Giulia") {}
                SmsRow(Sms("TIM", "Il tuo credito residuo è di 12,40 euro.", t - 90_000_000, true), null) {}
            }
        }
    }

    @Test fun homeDark() = paparazzi.snapshot {
        SempliceTheme(dark = true) {
            HomeContent(contacts, showKeypad = true, callInProgress = false, setupNeeded = false,
                onContact = {}, onKeypad = {}, onReturnToCall = {}, onSettings = {}, now = now)
        }
    }

    @Test fun incomingCallDark() = paparazzi.snapshot {
        SempliceTheme(dark = true) {
            CallContent("Ti sta chiamando", true, "Giulia", null, CallMode.RINGING, null, {}, {}, {})
        }
    }

    private fun landscape() {
        val p = DeviceConfig.PIXEL_5
        paparazzi.unsafeUpdateConfig(
            p.copy(screenWidth = p.screenHeight, screenHeight = p.screenWidth,
                orientation = ScreenOrientation.LANDSCAPE, locale = "it")
        )
    }

    @Test fun homeLandscape() {
        landscape()
        paparazzi.snapshot {
            SempliceTheme(dark = false) {
                HomeContent(contacts, showKeypad = true, callInProgress = false, setupNeeded = false,
                    onContact = {}, onKeypad = {}, onReturnToCall = {}, onSettings = {}, now = now)
            }
        }
    }

    @Test fun incomingCallLandscape() {
        landscape()
        paparazzi.snapshot {
            SempliceTheme(dark = false) {
                CallContent("Ti sta chiamando", true, "Giulia", null, CallMode.RINGING, null, {}, {}, {})
            }
        }
    }

    @Test fun keypadLandscape() {
        landscape()
        paparazzi.snapshot { SempliceTheme(dark = false) { KeypadScreen("0612345678") {} } }
    }

    @Test fun pinLandscape() {
        landscape()
        paparazzi.snapshot { SempliceTheme(dark = false) { PinPad("Inserisci il codice", {}, { false }, "Codice errato") {} } }
    }

    @Test fun pin() = paparazzi.snapshot {
        SempliceTheme(dark = false) { PinPad("Inserisci il codice", {}, { false }, "Codice errato") {} }
    }
}
