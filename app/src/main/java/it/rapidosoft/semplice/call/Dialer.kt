package it.rapidosoft.semplice.call

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.telecom.TelecomManager
import android.widget.Toast
import it.rapidosoft.semplice.R

object Dialer {
    private var lastCallAt = 0L

    /** Avvia subito la chiamata, in vivavoce. Ignora i doppi tocchi ravvicinati. */
    fun call(context: Context, number: String) {
        if (number.isBlank()) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastCallAt < 4000) return
        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, R.string.call_no_permission, Toast.LENGTH_LONG).show()
            return
        }
        lastCallAt = now
        val extras = Bundle().apply {
            putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
        }
        runCatching {
            context.getSystemService(TelecomManager::class.java)
                .placeCall(Uri.fromParts("tel", number, null), extras)
        }.onFailure {
            lastCallAt = 0
            Toast.makeText(context, R.string.call_failed, Toast.LENGTH_LONG).show()
        }
    }

    fun isDefaultDialer(context: Context) = roleHeld(context, RoleManager.ROLE_DIALER)
    fun isDefaultHome(context: Context) = roleHeld(context, RoleManager.ROLE_HOME)

    private fun roleHeld(context: Context, role: String): Boolean {
        val rm = context.getSystemService(RoleManager::class.java)
        return rm.isRoleAvailable(role) && rm.isRoleHeld(role)
    }
}
