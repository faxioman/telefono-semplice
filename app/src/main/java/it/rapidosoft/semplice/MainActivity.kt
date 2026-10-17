package it.rapidosoft.semplice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import it.rapidosoft.semplice.ui.SempliceActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import it.rapidosoft.semplice.call.CallManager
import it.rapidosoft.semplice.call.Dialer
import it.rapidosoft.semplice.data.Contact
import it.rapidosoft.semplice.data.Store
import it.rapidosoft.semplice.ui.ContactPhoto
import it.rapidosoft.semplice.ui.Green
import it.rapidosoft.semplice.ui.Ink
import it.rapidosoft.semplice.ui.Muted
import it.rapidosoft.semplice.ui.SempliceTheme
import it.rapidosoft.semplice.ui.isLandscape
import it.rapidosoft.semplice.ui.Amber
import it.rapidosoft.semplice.ui.Tile
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Schermata Home (launcher): orologio, contatti con foto, tastiera, ingranaggio. */
class MainActivity : SempliceActivity() {

    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // È la Home: il tasto Indietro non porta da nessuna parte.
        onBackPressedDispatcher.addCallback(this) {}
        askMissingPermissions()
        ScreenGuard.install(this)
        setContent { SempliceTheme { HomeScreen() } }
    }

    private fun askMissingPermissions() {
        val wanted = buildList {
            add(Manifest.permission.CALL_PHONE)
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
        }.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (wanted.isNotEmpty()) permissions.launch(wanted.toTypedArray())
    }
}

@Composable
private fun HomeScreen() {
    val context = LocalContext.current
    val store = remember { Store.get(context) }
    val contacts by store.contacts.collectAsState()
    val settings by store.settings.collectAsState()
    val calls by CallManager.state.collectAsState()
    var setupOk by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        setupOk = Dialer.isDefaultDialer(context)
        onPauseOrDispose {}
    }

    HomeContent(
        contacts = contacts,
        showKeypad = settings.showKeypad,
        callInProgress = calls.hasLiveCall || calls.ringing != null,
        setupNeeded = !setupOk,
        onContact = { Dialer.call(context, it.number) },
        onKeypad = { context.startActivity(Intent(context, KeypadActivity::class.java)) },
        onReturnToCall = { context.startActivity(CallActivity.intent(context)) },
        onSettings = { context.startActivity(Intent(context, SettingsActivity::class.java)) },
    )
}

/** UI della Home senza dipendenze dal sistema (usata anche per gli screenshot di test). */
@Composable
internal fun HomeContent(
    contacts: List<Contact>,
    showKeypad: Boolean,
    callInProgress: Boolean,
    setupNeeded: Boolean,
    onContact: (Contact) -> Unit,
    onKeypad: () -> Unit,
    onReturnToCall: () -> Unit,
    onSettings: () -> Unit,
    now: LocalDateTime? = null,
) {
    val banner: @Composable () -> Unit = {
        if (callInProgress) {
            Banner(stringResource(R.string.home_call_in_progress), Green, onReturnToCall)
        } else if (setupNeeded) {
            Banner(stringResource(R.string.home_setup_needed), Amber, onSettings)
        }
    }
    val landscape = isLandscape()
    val grid: @Composable (Modifier) -> Unit = { modifier ->
        Box(modifier) {
            if (contacts.isEmpty() && !showKeypad) {
                Text(
                    stringResource(R.string.home_no_contacts),
                    fontSize = 24.sp, color = Muted, textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (landscape) 3 else 2),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(contacts, key = { it.id }) { c ->
                        ContactTile(c, compact = landscape) { onContact(c) }
                    }
                    if (showKeypad) {
                        item(key = "keypad") { KeypadTile(compact = landscape, onClick = onKeypad) }
                    }
                }
            }
        }
    }

    if (landscape) {
        // Orizzontale: orologio e avvisi a sinistra, contatti a destra.
        Row(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp)) {
            Column(Modifier.weight(0.38f).fillMaxHeight().padding(end = 16.dp), verticalArrangement = Arrangement.Center) {
                Clock(now)
                banner()
                Spacer(Modifier.weight(1f))
                SettingsButton(onSettings)
            }
            grid(Modifier.weight(0.62f).fillMaxHeight())
        }
    } else {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp)) {
            Clock(now)
            banner()
            grid(Modifier.weight(1f).fillMaxWidth())
            SettingsButton(onSettings)
        }
    }
}

/** Ingranaggio piccolo e semitrasparente: per il familiare, non per l'anziano. */
@Composable
private fun SettingsButton(onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        IconButton(onClick = onSettings, modifier = Modifier.alpha(0.35f)) {
            Icon(Icons.Filled.Settings, stringResource(R.string.settings_title), Modifier.size(26.dp), tint = Ink)
        }
    }
}

@Composable
private fun Clock(fixed: LocalDateTime?) {
    val ticking by produceState(fixed ?: LocalDateTime.now()) {
        while (fixed == null) {
            value = LocalDateTime.now()
            delay(1000)
        }
    }
    val now = fixed ?: ticking
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val time = now.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
    val date = now.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))
        .replaceFirstChar { it.titlecase(locale) }
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(time, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text(date, fontSize = 24.sp, color = Ink, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Banner(text: String, color: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(16.dp))
            .background(color).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(if (color == Green) Icons.Filled.Call else Icons.Filled.Warning, null, tint = Color.White, modifier = Modifier.size(32.dp))
        Spacer(Modifier.size(12.dp))
        Text(text, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

// In orizzontale riquadri più bassi, così due file stanno nello schermo senza scorrere.
private fun tileRatio(compact: Boolean) = if (compact) 4f / 3f else 1f
private fun tileFont(compact: Boolean) = if (compact) 21.sp else 26.sp

@Composable
private fun ContactTile(contact: Contact, compact: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Tile),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ContactPhoto(contact.photo, contact.name, Modifier.fillMaxWidth().aspectRatio(tileRatio(compact)), corner = 18.dp)
            Text(
                contact.name, fontSize = tileFont(compact), fontWeight = FontWeight.Bold, color = Ink,
                maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
            )
        }
    }
}

@Composable
private fun KeypadTile(compact: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Green, contentColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().aspectRatio(tileRatio(compact)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Dialpad, null, Modifier.fillMaxSize(0.6f))
            }
            Text(
                stringResource(R.string.home_keypad), fontSize = tileFont(compact), fontWeight = FontWeight.Bold,
                maxLines = 1, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
            )
        }
    }
}
