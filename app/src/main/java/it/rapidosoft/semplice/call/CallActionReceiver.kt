package it.rapidosoft.semplice.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DECLINE) CallManager.reject()
        IncomingCallNotification.cancel(context)
    }

    companion object {
        const val ACTION_DECLINE = "it.rapidosoft.semplice.DECLINE"
    }
}
