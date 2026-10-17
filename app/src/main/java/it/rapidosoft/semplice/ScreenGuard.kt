package it.rapidosoft.semplice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import it.rapidosoft.semplice.call.CallManager
import it.rapidosoft.semplice.data.Store

/**
 * Il tasto di accensione non si può intercettare: quando spegne lo schermo lo
 * riaccendiamo subito, riportando in primo piano la Home (o la chiamata in corso).
 */
object ScreenGuard {
    private var installed = false
    private val handler = Handler(Looper.getMainLooper())

    fun install(context: Context) {
        if (installed) return
        installed = true
        val app = context.applicationContext
        app.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                if (!Store.get(c).settings.value.wakeOnPower) return
                handler.postDelayed({ wake(c) }, 500)
            }
        }, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    @Suppress("DEPRECATION")
    private fun wake(context: Context) {
        val pm = context.getSystemService(PowerManager::class.java)
        if (pm.isInteractive) return
        // Doppia strada: wake lock (vecchi Android) + activity con turnScreenOn (recenti).
        runCatching {
            pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "semplice:wake",
            ).acquire(3_000)
        }
        val calls = CallManager.state.value
        val target = if (calls.hasLiveCall || calls.ringing != null) {
            CallActivity.intent(context)
        } else {
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(target) }
    }
}
