package it.rapidosoft.semplice

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import it.rapidosoft.semplice.data.Contact
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
        SempliceTheme {
            HomeContent(contacts, showKeypad = true, callInProgress = false, setupNeeded = false,
                onContact = {}, onKeypad = {}, onReturnToCall = {}, onSettings = {}, now = now)
        }
    }

    @Test fun homeCallInProgress() = paparazzi.snapshot {
        SempliceTheme {
            HomeContent(contacts, showKeypad = false, callInProgress = true, setupNeeded = false,
                onContact = {}, onKeypad = {}, onReturnToCall = {}, onSettings = {}, now = now)
        }
    }

    @Test fun incomingCall() = paparazzi.snapshot {
        SempliceTheme {
            CallContent("Ti sta chiamando", true, "Giulia", null, CallMode.RINGING, null, {}, {}, {})
        }
    }

    @Test fun activeCall() = paparazzi.snapshot {
        SempliceTheme {
            CallContent("2:17", false, "Giulia", null, CallMode.ACTIVE, null, {}, {}, {})
        }
    }

    @Test fun homeArabic() {
        paparazzi.unsafeUpdateConfig(DeviceConfig.PIXEL_5.copy(locale = "ar"))
        paparazzi.snapshot {
            SempliceTheme {
                HomeContent(contacts, showKeypad = true, callInProgress = false, setupNeeded = true,
                    onContact = {}, onKeypad = {}, onReturnToCall = {}, onSettings = {}, now = now)
            }
        }
    }

    @Test fun pin() = paparazzi.snapshot {
        SempliceTheme { PinPad("Inserisci il codice", {}, { false }, "Codice errato") {} }
    }
}
