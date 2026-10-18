package it.rapidosoft.semplice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.telecom.Call
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import it.rapidosoft.semplice.ui.SempliceActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.rapidosoft.semplice.call.CallManager
import it.rapidosoft.semplice.call.IncomingCallNotification
import it.rapidosoft.semplice.call.number
import it.rapidosoft.semplice.call.stateCompat
import it.rapidosoft.semplice.data.Numbers
import it.rapidosoft.semplice.data.Store
import it.rapidosoft.semplice.ui.Accent
import it.rapidosoft.semplice.ui.BigButton
import it.rapidosoft.semplice.ui.ContactPhoto
import it.rapidosoft.semplice.ui.Green
import it.rapidosoft.semplice.ui.Ink
import it.rapidosoft.semplice.ui.Muted
import it.rapidosoft.semplice.ui.Red
import it.rapidosoft.semplice.ui.SempliceTheme
import it.rapidosoft.semplice.ui.isLandscape
import kotlinx.coroutines.delay

/** Chiamata in arrivo (foto grande + Butta giù / Rispondi) e chiamata in corso. */
class CallActivity : SempliceActivity() {
    override val alwaysKeepScreenOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        onBackPressedDispatcher.addCallback(this) {} // non si esce per sbaglio
        handleAction(intent)
        setContent { SempliceTheme { CallScreen(onFinished = { finish() }) } }
    }

    override fun onResume() {
        super.onResume()
        // La schermata è visibile: il banner della notifica sarebbe solo un doppione.
        IncomingCallNotification.cancel(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAction(intent)
    }

    private fun handleAction(intent: Intent?) {
        if (intent?.action == ACTION_ANSWER) {
            CallManager.answer()
            IncomingCallNotification.cancel(this)
        }
    }

    companion object {
        const val ACTION_ANSWER = "it.rapidosoft.semplice.ANSWER"

        fun intent(context: Context): Intent =
            Intent(context, CallActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}

private data class Shown(val name: String, val photo: String?)

@Composable
private fun CallScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val store = remember { Store.get(context) }
    val snapshot by CallManager.state.collectAsState()
    val contacts by store.contacts.collectAsState()
    val call = snapshot.primary
    val state = call?.stateCompat ?: Call.STATE_DISCONNECTED

    val hiddenLabel = stringResource(R.string.call_hidden)
    var shown by remember { mutableStateOf<Shown?>(null) }
    if (call != null) {
        val number = call.number
        val contact = contacts.firstOrNull { Numbers.same(it.number, number) }
        shown = Shown(contact?.name ?: number?.takeIf { it.isNotBlank() } ?: hiddenLabel, contact?.photo)
    }
    val info = shown

    // Nessuna chiamata: mostriamo "Chiamata terminata" per un attimo e chiudiamo.
    if (call == null || state == Call.STATE_DISCONNECTED) {
        LaunchedEffect(call) {
            delay(if (info == null) 0 else 1500)
            if (CallManager.state.value.primary == null) onFinished()
        }
    }

    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(call, state) {
        while (call != null && state == Call.STATE_ACTIVE) {
            val start = call.details.connectTimeMillis
            elapsed = if (start > 0) (System.currentTimeMillis() - start) / 1000 else 0
            delay(1000)
        }
    }

    // Conto alla rovescia della risposta automatica, se programmata.
    var secondsToAutoAnswer by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(call, state) {
        while (call != null && state == Call.STATE_RINGING) {
            secondsToAutoAnswer = CallManager.autoAnswerAt[call]
                ?.let { ((it - SystemClock.elapsedRealtime() + 999) / 1000).toInt().coerceAtLeast(0) }
            delay(250)
        }
        secondsToAutoAnswer = null
    }
    val autoNote = secondsToAutoAnswer?.let { stringResource(R.string.auto_answer_countdown, it) }

    val status = when (state) {
        Call.STATE_RINGING -> stringResource(R.string.call_incoming)
        Call.STATE_DIALING, Call.STATE_CONNECTING, Call.STATE_NEW -> stringResource(R.string.call_dialing)
        Call.STATE_ACTIVE -> "%d:%02d".format(elapsed / 60, elapsed % 60)
        Call.STATE_HOLDING -> stringResource(R.string.call_on_hold)
        else -> stringResource(R.string.call_ended)
    }

    CallContent(
        status = status,
        statusHighlighted = state == Call.STATE_RINGING,
        name = info?.name ?: "",
        photo = info?.photo,
        mode = when (state) {
            Call.STATE_RINGING -> CallMode.RINGING
            Call.STATE_DISCONNECTED -> CallMode.ENDED
            else -> CallMode.ACTIVE
        },
        callKey = call,
        onAnswer = {
            CallManager.answer()
            IncomingCallNotification.cancel(context)
        },
        onReject = { CallManager.reject() },
        onHangup = { CallManager.hangup(call) },
        note = autoNote,
    )
}

internal enum class CallMode { RINGING, ACTIVE, ENDED }

/** UI della chiamata senza dipendenze da Telecom (usata anche per gli screenshot di test). */
@Composable
internal fun CallContent(
    status: String,
    statusHighlighted: Boolean,
    name: String,
    photo: String?,
    mode: CallMode,
    callKey: Any?,
    onAnswer: () -> Unit,
    onReject: () -> Unit,
    onHangup: () -> Unit,
    note: String? = null,
) {
    val statusText: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(status, fontSize = 30.sp, color = if (statusHighlighted) Accent else Muted, fontWeight = FontWeight.Bold)
            if (note != null) Text(note, fontSize = 20.sp, color = Muted)
        }
    }
    val nameText: @Composable () -> Unit = {
        Text(
            name, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Ink,
            textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 48.sp,
        )
    }
    val buttons: @Composable (Modifier) -> Unit = { buttonsModifier ->
        when (mode) {
            CallMode.RINGING -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BigButton(stringResource(R.string.call_hangup), Icons.Filled.CallEnd, Red, buttonsModifier.weight(1f)) {
                    onReject()
                }
                BigButton(stringResource(R.string.call_answer), Icons.Filled.Call, Green, buttonsModifier.weight(1f)) {
                    onAnswer()
                }
            }
            CallMode.ENDED -> Row(buttonsModifier) {}
            CallMode.ACTIVE -> {
                // Evita che il tocco usato per "Rispondi" chiuda subito la chiamata.
                val shownAt = remember(callKey) { SystemClock.elapsedRealtime() }
                BigButton(stringResource(R.string.call_hangup), Icons.Filled.CallEnd, Red, buttonsModifier.fillMaxWidth()) {
                    if (SystemClock.elapsedRealtime() - shownAt > 800) onHangup()
                }
            }
        }
    }

    // Mentre squilla, anche toccare la foto grande risponde.
    val answerByPhoto = if (mode == CallMode.RINGING) {
        Modifier.clip(RoundedCornerShape(32.dp)).clickable(onClick = onAnswer)
    } else {
        Modifier
    }

    if (isLandscape()) {
        // Orizzontale: foto a sinistra, stato/nome/bottoni a destra.
        Row(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            ContactPhoto(photo, name, Modifier.weight(1f).fillMaxHeight().then(answerByPhoto), corner = 32.dp)
            Column(
                Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                statusText()
                nameText()
                buttons(Modifier.height(130.dp))
            }
        }
    } else {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            statusText()
            ContactPhoto(photo, name, Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp).then(answerByPhoto), corner = 32.dp)
            nameText()
            buttons(Modifier.padding(top = 16.dp).height(150.dp))
        }
    }
}
