package it.rapidosoft.semplice.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony

data class Sms(val address: String, val body: String, val date: Long, val read: Boolean)

/** Lettura SMS (solo se il permesso è stato concesso) e apertura dell'app Messaggi ufficiale. */
object SmsBox {
    fun canRead(context: Context) =
        context.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED

    fun latest(context: Context, limit: Int = 20): List<Sms> {
        if (!canRead(context)) return emptyList()
        return runCatching {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.READ),
                null, null, "${Telephony.Sms.DATE} DESC",
            )?.use { c ->
                buildList {
                    while (size < limit && c.moveToNext()) {
                        add(Sms(c.getString(0).orEmpty(), c.getString(1).orEmpty(), c.getLong(2), c.getInt(3) != 0))
                    }
                }
            }
        }.getOrNull().orEmpty()
    }

    fun openApp(context: Context) {
        val selector = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING)
        val fallback = Telephony.Sms.getDefaultSmsPackage(context)
            ?.let { context.packageManager.getLaunchIntentForPackage(it) }
        for (intent in listOfNotNull(selector, fallback)) {
            if (runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess) return
        }
    }

    fun openConversation(context: Context, number: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }.onFailure { openApp(context) }
    }
}
