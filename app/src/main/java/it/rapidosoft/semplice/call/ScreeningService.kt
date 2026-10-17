package it.rapidosoft.semplice.call

import android.telecom.Call
import android.telecom.CallScreeningService
import it.rapidosoft.semplice.data.CallFilter
import it.rapidosoft.semplice.data.LogType
import it.rapidosoft.semplice.data.Store

/** Applica lista nera / lista bianca prima che il telefono squilli. */
class ScreeningService : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        val incoming = details.callDirection == Call.Details.DIRECTION_INCOMING
        val number = details.handle?.schemeSpecificPart
        val logType = when {
            !incoming -> null
            CallManager.state.value.isBusy -> LogType.BUSY
            CallFilter.shouldBlock(this, number) -> LogType.BLOCKED
            else -> null
        }
        val block = logType != null
        if (logType != null) Store.get(this).addLog(number, logType)
        val response = CallResponse.Builder()
            .setDisallowCall(block)
            .setRejectCall(block)
            .setSkipNotification(block)
            .setSkipCallLog(false)
            .build()
        respondToCall(details, response)
    }
}
