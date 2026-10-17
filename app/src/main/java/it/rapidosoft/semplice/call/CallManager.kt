package it.rapidosoft.semplice.call

import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Suppress("DEPRECATION")
val Call.stateCompat: Int
    get() = if (Build.VERSION.SDK_INT >= 31) details.state else state

val Call.number: String?
    get() = details?.handle?.schemeSpecificPart

/** Fotografia immutabile delle chiamate correnti; [version] cambia a ogni evento. */
data class CallsSnapshot(val calls: List<Call> = emptyList(), val version: Int = 0) {
    val ringing: Call? get() = calls.firstOrNull { it.stateCompat == Call.STATE_RINGING }
    val primary: Call?
        get() = ringing
            ?: calls.firstOrNull { it.stateCompat == Call.STATE_ACTIVE }
            ?: calls.firstOrNull { it.stateCompat != Call.STATE_DISCONNECTED }
            ?: calls.firstOrNull()
    /** C'è già una chiamata (anche solo in squillo): un'altra in arrivo trova occupato. */
    val isBusy: Boolean get() = calls.any { it.stateCompat != Call.STATE_DISCONNECTED }
    val hasLiveCall: Boolean
        get() = calls.any {
            it.stateCompat in setOf(
                Call.STATE_ACTIVE, Call.STATE_DIALING, Call.STATE_CONNECTING,
                Call.STATE_HOLDING, Call.STATE_PULLING_CALL,
            )
        }
}

object CallManager {
    private val _state = MutableStateFlow(CallsSnapshot())
    val state: StateFlow<CallsSnapshot> = _state

    var service: PhoneInCallService? = null
    private var lastSpeakerRequest = 0L

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            bump()
            service?.onCallStateChanged(call)
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) = bump()
    }

    fun add(call: Call) {
        call.registerCallback(callback)
        _state.value = CallsSnapshot(_state.value.calls + call, _state.value.version + 1)
    }

    fun remove(call: Call) {
        call.unregisterCallback(callback)
        _state.value = CallsSnapshot(_state.value.calls - call, _state.value.version + 1)
    }

    private fun bump() {
        _state.value = _state.value.copy(version = _state.value.version + 1)
    }

    fun answer() {
        _state.value.ringing?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun reject() {
        _state.value.ringing?.reject(false, null)
    }

    fun hangup(call: Call? = _state.value.primary) {
        val c = call ?: return
        if (c.stateCompat == Call.STATE_RINGING) c.reject(false, null) else c.disconnect()
    }

    /** Vivavoce sempre: se l'audio non va sull'altoparlante, ce lo rimettiamo. */
    @Suppress("DEPRECATION")
    fun enforceSpeaker() {
        val svc = service ?: return
        if (!_state.value.hasLiveCall) return
        val audio = svc.callAudioState ?: return
        if (audio.isMuted) svc.setMuted(false)
        if (audio.route == CallAudioState.ROUTE_SPEAKER) return
        if (audio.supportedRouteMask and CallAudioState.ROUTE_SPEAKER == 0) return
        val now = System.currentTimeMillis()
        if (now - lastSpeakerRequest < 700) return // evita cicli se il sistema rifiuta
        lastSpeakerRequest = now
        svc.setAudioRoute(CallAudioState.ROUTE_SPEAKER)
    }
}
