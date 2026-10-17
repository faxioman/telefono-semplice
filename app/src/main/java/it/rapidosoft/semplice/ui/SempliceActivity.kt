package it.rapidosoft.semplice.ui

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import it.rapidosoft.semplice.data.Settings

/** Base di tutte le schermate: barra di navigazione nascosta, schermo e luminosità da preferenze. */
open class SempliceActivity : AppCompatActivity() {
    /** La schermata di chiamata resta accesa a prescindere dalla preferenza. */
    protected open val alwaysKeepScreenOn = false

    override fun onResume() {
        super.onResume()
        resumed++
    }

    override fun onPause() {
        resumed--
        super.onPause()
    }

    companion object {
        private var resumed = 0

        /** Una nostra schermata è in primo piano: possiamo aprire activity senza notifica. */
        val inForeground: Boolean get() = resumed > 0
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // tastiera, dialoghi e swipe possono farla ricomparire: la rinascondiamo
        if (hasFocus) hideNavigationBar()
    }

    fun hideNavigationBar() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    fun applyScreenSettings(settings: Settings) {
        // Con "ignora tasto accensione" l'app si mostra sopra il blocco schermo e lo riaccende.
        val overLock = settings.wakeOnPower || alwaysKeepScreenOn
        setShowWhenLocked(overLock)
        setTurnScreenOn(overLock)
        if (settings.keepAwake || alwaysKeepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        window.attributes = window.attributes.apply {
            screenBrightness = if (settings.brightness < 0) {
                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            } else {
                settings.brightness.coerceIn(0.05f, 1f)
            }
        }
    }
}
