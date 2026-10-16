package it.rapidosoft.semplice.call

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telecom.Call
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import it.rapidosoft.semplice.CallActivity
import it.rapidosoft.semplice.R
import it.rapidosoft.semplice.data.Photos
import it.rapidosoft.semplice.data.Store

/**
 * Notifica "a schermo intero": è il modo garantito da Android per mostrare la nostra
 * schermata di chiamata anche a telefono bloccato o con l'app in background.
 */
object IncomingCallNotification {
    private const val CHANNEL = "incoming_calls"
    private const val ID = 1001

    fun show(context: Context, call: Call) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL,
                context.getString(R.string.notif_channel_calls),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { setSound(null, null) } // la suoneria la gestisce il sistema
        )
        val contact = Store.get(context).findContact(call.number)
        val name = contact?.name
            ?: call.number?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.call_hidden)

        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val full = PendingIntent.getActivity(context, 0, CallActivity.intent(context), flags)
        val answer = PendingIntent.getActivity(
            context, 1,
            CallActivity.intent(context).setAction(CallActivity.ACTION_ANSWER), flags,
        )
        val decline = PendingIntent.getBroadcast(
            context, 2,
            Intent(context, CallActionReceiver::class.java).setAction(CallActionReceiver.ACTION_DECLINE),
            flags,
        )
        val person = Person.Builder().setName(name).setImportant(true).apply {
            contact?.photo?.let { Photos.load(it) }?.let { setIcon(IconCompat.createWithBitmap(it)) }
        }.build()

        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_call)
            .setContentTitle(name)
            .setContentText(context.getString(R.string.call_incoming))
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(full)
            .setFullScreenIntent(full, true)
            .setStyle(NotificationCompat.CallStyle.forIncomingCall(person, decline, answer))
            .build()
        try {
            nm.notify(ID, notification)
        } catch (_: SecurityException) {
            // permesso notifiche negato: resta comunque il tentativo di startActivity
        }
    }

    fun cancel(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(ID)
    }
}
