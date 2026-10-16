package it.rapidosoft.semplice.call

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import it.rapidosoft.semplice.CallActivity
import it.rapidosoft.semplice.data.CallFilter

/** Il sistema si collega a questo servizio perché siamo l'app Telefono predefinita. */
class PhoneInCallService : InCallService() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCallAdded(call: Call) {
        CallManager.service = this
        val incoming = call.stateCompat == Call.STATE_RINGING
        // Rete di sicurezza: normalmente il blocco avviene già in ScreeningService.
        if (incoming && CallFilter.shouldBlock(this, call.number)) {
            call.reject(false, null)
            return
        }
        CallManager.add(call)
        if (incoming) IncomingCallNotification.show(this, call)
        runCatching { startActivity(CallActivity.intent(this)) }
        CallManager.enforceSpeaker()
    }

    override fun onCallRemoved(call: Call) {
        CallManager.remove(call)
        if (CallManager.state.value.ringing == null) IncomingCallNotification.cancel(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        CallManager.enforceSpeaker()
    }

    fun onCallStateChanged(call: Call) {
        if (call.stateCompat != Call.STATE_RINGING && CallManager.state.value.ringing == null) {
            IncomingCallNotification.cancel(this)
        }
        CallManager.enforceSpeaker()
        // Alcuni dispositivi resettano l'instradamento audio subito dopo la connessione.
        if (call.stateCompat == Call.STATE_ACTIVE) {
            handler.postDelayed({ CallManager.enforceSpeaker() }, 600)
            handler.postDelayed({ CallManager.enforceSpeaker() }, 1500)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (CallManager.service === this) CallManager.service = null
        return super.onUnbind(intent)
    }
}
