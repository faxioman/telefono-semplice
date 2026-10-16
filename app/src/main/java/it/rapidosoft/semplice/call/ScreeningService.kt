package it.rapidosoft.semplice.call

import android.telecom.Call
import android.telecom.CallScreeningService
import it.rapidosoft.semplice.data.CallFilter

/** Applica lista nera / lista bianca prima che il telefono squilli. */
class ScreeningService : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        val incoming = details.callDirection == Call.Details.DIRECTION_INCOMING
        val block = incoming && CallFilter.shouldBlock(this, details.handle?.schemeSpecificPart)
        val response = CallResponse.Builder()
            .setDisallowCall(block)
            .setRejectCall(block)
            .setSkipNotification(block)
            .setSkipCallLog(false)
            .build()
        respondToCall(details, response)
    }
}
