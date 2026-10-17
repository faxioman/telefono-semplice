package it.rapidosoft.semplice.call

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.DisconnectCause
import android.telecom.InCallService
import it.rapidosoft.semplice.CallActivity
import it.rapidosoft.semplice.data.CallFilter
import it.rapidosoft.semplice.data.LogType
import it.rapidosoft.semplice.data.Store
import it.rapidosoft.semplice.ui.SempliceActivity

/** Il sistema si collega a questo servizio perché siamo l'app Telefono predefinita. */
class PhoneInCallService : InCallService() {
    private val handler = Handler(Looper.getMainLooper())
    private val incomingCalls = mutableSetOf<Call>()
    private val answeredCalls = mutableSetOf<Call>()

    override fun onCallAdded(call: Call) {
        CallManager.service = this
        val incoming = call.stateCompat == Call.STATE_RINGING
        if (incoming) {
            // Già al telefono: la seconda chiamata trova occupato.
            if (CallManager.state.value.isBusy) {
                rejectAndLog(call, LogType.BUSY)
                return
            }
            // Rete di sicurezza: normalmente il blocco avviene già in ScreeningService.
            if (CallFilter.shouldBlock(this, call.number)) {
                rejectAndLog(call, LogType.BLOCKED)
                return
            }
            incomingCalls.add(call)
        }
        CallManager.add(call)
        // Con l'app in primo piano basta aprire la schermata di chiamata. Altrimenti (telefono
        // bloccato, altra app davanti) serve la notifica a schermo intero: è lei ad aprirla.
        if (incoming && !SempliceActivity.inForeground) IncomingCallNotification.show(this, call)
        runCatching { startActivity(CallActivity.intent(this)) }
        CallManager.enforceSpeaker()
    }

    private fun rejectAndLog(call: Call, type: LogType) {
        Store.get(this).addLog(call.number, type)
        call.reject(false, null)
    }

    override fun onCallRemoved(call: Call) {
        if (incomingCalls.remove(call) && call !in answeredCalls) {
            val cause = call.details.disconnectCause?.code
            Store.get(this).addLog(
                call.number,
                if (cause == DisconnectCause.REJECTED) LogType.REJECTED else LogType.MISSED,
            )
        }
        answeredCalls.remove(call)
        CallManager.remove(call)
        if (CallManager.state.value.ringing == null) IncomingCallNotification.cancel(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        CallManager.enforceSpeaker()
    }

    fun onCallStateChanged(call: Call) {
        if (call.stateCompat == Call.STATE_ACTIVE) answeredCalls.add(call)
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
