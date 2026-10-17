package it.rapidosoft.semplice

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import it.rapidosoft.semplice.ui.SempliceActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import it.rapidosoft.semplice.call.Dialer
import it.rapidosoft.semplice.data.Contact
import it.rapidosoft.semplice.data.ContactImport
import it.rapidosoft.semplice.data.Prefill
import it.rapidosoft.semplice.data.FilterMode
import it.rapidosoft.semplice.data.LogEntry
import it.rapidosoft.semplice.data.LogType
import it.rapidosoft.semplice.data.Sms
import it.rapidosoft.semplice.data.SmsBox
import it.rapidosoft.semplice.data.Photos
import it.rapidosoft.semplice.data.Store
import it.rapidosoft.semplice.data.ThemeMode
import it.rapidosoft.semplice.ui.ContactPhoto
import it.rapidosoft.semplice.ui.Green
import it.rapidosoft.semplice.ui.Ink
import it.rapidosoft.semplice.ui.Muted
import it.rapidosoft.semplice.ui.Red
import it.rapidosoft.semplice.ui.SempliceTheme
import it.rapidosoft.semplice.ui.isLandscape
import it.rapidosoft.semplice.ui.Accent
import it.rapidosoft.semplice.ui.Amber
import it.rapidosoft.semplice.ui.Tile
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Preferenze per il familiare/caregiver, protette da codice PIN. */
class SettingsActivity : SempliceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val unlockedBefore = savedInstanceState?.getBoolean("unlocked") == true
        setContent { SempliceTheme { SettingsRoot(unlockedBefore, onClose = { finish() }, onUnlock = { unlocked = true }) } }
    }

    private var unlocked = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("unlocked", unlocked)
    }
}

/* ---------------------------------------------------------------- PIN */

@Composable
private fun SettingsRoot(unlockedBefore: Boolean, onClose: () -> Unit, onUnlock: () -> Unit) {
    val context = LocalContext.current
    val store = remember { Store.get(context) }
    val pin = store.settings.collectAsState().value.pin
    var unlocked by rememberSaveable { mutableStateOf(unlockedBefore || pin.isEmpty()) }
    if (unlocked) {
        onUnlock()
        SettingsNav(onClose)
    } else {
        PinPad(
            title = stringResource(R.string.pin_enter),
            onBack = onClose,
            validate = { it == pin },
            errorText = stringResource(R.string.pin_wrong),
        ) { unlocked = true }
    }
}

@Composable
internal fun PinPad(
    title: String,
    onBack: () -> Unit,
    validate: (String) -> Boolean,
    errorText: String,
    onOk: (String) -> Unit,
) {
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    BackHandler(onBack = onBack)
    val header: @Composable () -> Unit = {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(
            if (entered.isEmpty()) " " else "●".repeat(entered.length),
            fontSize = 36.sp, modifier = Modifier.padding(16.dp),
        )
        Text(if (error) errorText else " ", color = Red, fontSize = 18.sp)
    }
    val keypad: @Composable (Modifier) -> Unit = { modifier ->
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "OK")
        Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            keys.chunked(3).forEach { row ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { k ->
                        Box(
                            Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp))
                                .background(if (k == "OK") Green else Tile)
                                .clickable {
                                    error = false
                                    when (k) {
                                        "⌫" -> entered = entered.dropLast(1)
                                        "OK" -> if (validate(entered)) onOk(entered) else {
                                            error = true; entered = ""
                                        }
                                        else -> if (entered.length < 8) entered += k
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (k == "⌫") Icon(Icons.AutoMirrored.Filled.Backspace, null, Modifier.size(32.dp))
                            else Text(k, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if (k == "OK") Color.White else Ink)
                        }
                    }
                }
            }
        }
    }
    val back: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
        }
    }

    if (isLandscape()) {
        Row(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                back()
                Spacer(Modifier.weight(1f))
                header()
                Spacer(Modifier.weight(1f))
            }
            keypad(Modifier.weight(1.2f).fillMaxHeight())
        }
    } else {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            back()
            header()
            keypad(Modifier.weight(1f).padding(top = 16.dp))
        }
    }
}

/* ---------------------------------------------------------- navigazione */

private sealed interface Screen {
    data object Main : Screen
    data class Edit(val contactId: String?, val prefill: Prefill? = null) : Screen
    data object NewPin : Screen
}

@Composable
private fun SettingsNav(onClose: () -> Unit) {
    var screen by remember { mutableStateOf<Screen>(Screen.Main) }
    when (val s = screen) {
        Screen.Main -> MainSettings(
            onClose = onClose,
            onEditContact = { screen = Screen.Edit(it) },
            onNewFromBook = { screen = Screen.Edit(null, it) },
            onNewPin = { screen = Screen.NewPin },
        )
        is Screen.Edit -> EditContact(s.contactId, s.prefill) { screen = Screen.Main }
        Screen.NewPin -> NewPinFlow { screen = Screen.Main }
    }
}

@Composable
private fun NewPinFlow(onDone: () -> Unit) {
    val context = LocalContext.current
    var first by remember { mutableStateOf<String?>(null) }
    val f = first
    if (f == null) {
        PinPad(stringResource(R.string.pin_new), onDone, { it.length in 4..8 }, stringResource(R.string.pin_too_short)) {
            first = it
        }
    } else {
        PinPad(stringResource(R.string.pin_confirm), onDone, { it == f }, stringResource(R.string.pin_mismatch)) {
            Store.get(context).updateSettings { s -> s.copy(pin = it) }
            onDone()
        }
    }
}

/* ------------------------------------------------------ schermata principale */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainSettings(
    onClose: () -> Unit,
    onEditContact: (String?) -> Unit,
    onNewFromBook: (Prefill) -> Unit,
    onNewPin: () -> Unit,
) {
    val context = LocalContext.current
    val store = remember { Store.get(context) }
    val contacts by store.contacts.collectAsState()
    val settings by store.settings.collectAsState()

    var refresh by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        refresh++
        onPauseOrDispose {}
    }
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { refresh++ }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { refresh++ }
    var smsDenied by remember { mutableStateOf(false) }
    val smsPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        smsDenied = !ok
        refresh++
    }
    val log by store.log.collectAsState()
    val pickFromBook = rememberContactPicker(onNewFromBook)
    val sms = remember(refresh) { SmsBox.latest(context) }

    var addNumberTo by remember { mutableStateOf<String?>(null) } // "black" | "white"
    var confirmDelete by remember { mutableStateOf<Contact?>(null) }
    var showLanguages by remember { mutableStateOf(false) }

    BackHandler(onBack = onClose)

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
            Text(stringResource(R.string.settings_title), fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        LazyColumn(Modifier.fillMaxSize().wrapContentWidth().widthIn(max = 720.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 0.dp, 16.dp, 32.dp)) {
            /* --- chiamate perse --- */
            section(R.string.section_log)
            if (log.isEmpty()) item { Text(stringResource(R.string.log_empty), color = Muted) }
            items(log.take(30), key = { "log-${it.time}-${it.number}" }) { e ->
                LogRow(e, store.findContact(e.number)?.name) { Dialer.call(context, e.number) }
            }
            if (log.isNotEmpty()) item {
                TextButton(onClick = { store.clearLog() }) { Text(stringResource(R.string.log_clear)) }
            }

            /* --- SMS --- */
            section(R.string.section_sms)
            item(key = "sms-$refresh") {
                Button(onClick = { SmsBox.openApp(context) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Sms, null); Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.sms_open_app), fontSize = 18.sp)
                }
                if (!SmsBox.canRead(context)) {
                    OutlinedButton(
                        onClick = { smsPermLauncher.launch(Manifest.permission.READ_SMS) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.sms_show_here)) }
                    if (smsDenied) Text(stringResource(R.string.sms_unavailable), color = Muted, fontSize = 14.sp)
                }
            }
            if (SmsBox.canRead(context)) {
                if (sms.isEmpty()) item { Text(stringResource(R.string.sms_empty), color = Muted) }
                items(sms, key = { "sms-${it.date}-${it.address}-$refresh" }) { m ->
                    SmsRow(m, store.findContact(m.address)?.name) { SmsBox.openConversation(context, m.address) }
                }
            }

            /* --- configurazione --- */
            section(R.string.section_setup)
            item(key = "setup-$refresh") {
                val dialerOk = Dialer.isDefaultDialer(context)
                val homeOk = Dialer.isDefaultHome(context)
                val permsOk = missingPermissions(context).isEmpty()
                SetupRow(stringResource(R.string.setup_dialer), dialerOk) {
                    requestRole(context, RoleManager.ROLE_DIALER, roleLauncher, AndroidSettings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                }
                SetupRow(stringResource(R.string.setup_home), homeOk) {
                    requestRole(context, RoleManager.ROLE_HOME, roleLauncher, AndroidSettings.ACTION_HOME_SETTINGS)
                }
                SetupRow(stringResource(R.string.setup_permissions), permsOk) {
                    permLauncher.launch(missingPermissions(context).toTypedArray())
                }
                Text(stringResource(R.string.about_speaker), color = Muted, fontSize = 15.sp, modifier = Modifier.padding(vertical = 8.dp))
            }

            /* --- contatti --- */
            section(R.string.section_contacts)
            items(contacts, key = { it.id }) { c ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(16.dp))
                        .background(Tile).clickable { onEditContact(c.id) }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ContactPhoto(c.photo, c.name, Modifier.size(56.dp), corner = 12.dp)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(c.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(c.number, color = Muted, maxLines = 1)
                    }
                    IconButton(onClick = { store.moveContact(c, -1) }) { Icon(Icons.Filled.KeyboardArrowUp, stringResource(R.string.move_up)) }
                    IconButton(onClick = { store.moveContact(c, 1) }) { Icon(Icons.Filled.KeyboardArrowDown, stringResource(R.string.move_down)) }
                    IconButton(onClick = { confirmDelete = c }) { Icon(Icons.Filled.Delete, stringResource(R.string.delete), tint = Red) }
                }
            }
            item {
                Button(onClick = pickFromBook, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Icon(Icons.Filled.Contacts, null); Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.contact_add_from_book), fontSize = 18.sp)
                }
                OutlinedButton(onClick = { onEditContact(null) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, null); Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.contact_add_manual))
                }
            }

            /* --- chiamate --- */
            section(R.string.section_calls)
            item {
                SwitchRow(stringResource(R.string.pref_show_keypad), settings.showKeypad) { v ->
                    store.updateSettings { it.copy(showKeypad = v) }
                }
                Text(stringResource(R.string.filter_title), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 12.dp))
                listOf(
                    FilterMode.OFF to R.string.filter_off,
                    FilterMode.BLACKLIST to R.string.filter_blacklist,
                    FilterMode.WHITELIST to R.string.filter_whitelist,
                ).forEach { (mode, label) ->
                    Row(
                        Modifier.fillMaxWidth().selectable(settings.filterMode == mode, role = Role.RadioButton) {
                            store.updateSettings { it.copy(filterMode = mode) }
                        }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = settings.filterMode == mode, onClick = null)
                        Text(stringResource(label), fontSize = 17.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Text(stringResource(R.string.busy_info), color = Muted, fontSize = 15.sp, modifier = Modifier.padding(top = 8.dp))
                TextButton(onClick = { Dialer.disableCallWaiting(context) }) {
                    Text(stringResource(R.string.busy_disable_waiting))
                }
                SwitchRow(stringResource(R.string.pref_block_hidden), settings.blockHidden || settings.filterMode == FilterMode.WHITELIST,
                    enabled = settings.filterMode != FilterMode.WHITELIST) { v ->
                    store.updateSettings { it.copy(blockHidden = v) }
                }
            }
            numberList(
                title = R.string.blacklist_title,
                numbers = settings.blacklist,
                active = settings.filterMode == FilterMode.BLACKLIST,
                onAdd = { addNumberTo = "black" },
                onRemove = { n -> store.updateSettings { it.copy(blacklist = it.blacklist - n) } },
            )
            numberList(
                title = R.string.whitelist_title,
                numbers = settings.whitelist,
                active = settings.filterMode == FilterMode.WHITELIST,
                onAdd = { addNumberTo = "white" },
                onRemove = { n -> store.updateSettings { it.copy(whitelist = it.whitelist - n) } },
            )

            /* --- sicurezza --- */
            section(R.string.section_security)
            item {
                Text(
                    stringResource(if (settings.pin.isEmpty()) R.string.pin_status_off else R.string.pin_status_on),
                    color = if (settings.pin.isEmpty()) Red else Muted, modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onNewPin) {
                        Text(stringResource(if (settings.pin.isEmpty()) R.string.pin_set else R.string.pin_change))
                    }
                    if (settings.pin.isNotEmpty()) {
                        OutlinedButton(onClick = { store.updateSettings { it.copy(pin = "") } }) {
                            Text(stringResource(R.string.pin_remove))
                        }
                    }
                }
            }

            /* --- aspetto --- */
            section(R.string.section_theme)
            item {
                listOf(ThemeMode.LIGHT to R.string.theme_light, ThemeMode.DARK to R.string.theme_dark).forEach { (mode, label) ->
                    Row(
                        Modifier.fillMaxWidth().selectable(settings.theme == mode, role = Role.RadioButton) {
                            store.updateSettings { it.copy(theme = mode) }
                        }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = settings.theme == mode, onClick = null)
                        Text(stringResource(label), fontSize = 17.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            /* --- schermo --- */
            section(R.string.section_screen)
            item {
                SwitchRow(stringResource(R.string.pref_keep_awake), settings.keepAwake) { v ->
                    store.updateSettings { it.copy(keepAwake = v) }
                }
                Text(stringResource(R.string.pref_keep_awake_hint), color = Muted, fontSize = 14.sp)
                SwitchRow(stringResource(R.string.pref_wake_on_power), settings.wakeOnPower) { v ->
                    store.updateSettings { it.copy(wakeOnPower = v) }
                }
                Text(stringResource(R.string.pref_wake_on_power_hint), color = Muted, fontSize = 14.sp)
                SwitchRow(stringResource(R.string.brightness_auto), settings.brightness < 0) { auto ->
                    store.updateSettings { it.copy(brightness = if (auto) -1f else 0.7f) }
                }
                if (settings.brightness >= 0) {
                    Text(stringResource(R.string.brightness_title), fontSize = 17.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.BrightnessLow, null)
                        Slider(
                            value = settings.brightness,
                            onValueChange = { v -> store.updateSettings { it.copy(brightness = v) } },
                            valueRange = 0.05f..1f,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        )
                        Icon(Icons.Filled.BrightnessHigh, null)
                    }
                }
            }

            /* --- lingua --- */
            section(R.string.section_language)
            item {
                val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                val label = LANGUAGES.firstOrNull { it.first.isNotEmpty() && current.startsWith(it.first) }?.second
                    ?: stringResource(R.string.language_system)
                OutlinedButton(onClick = { showLanguages = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(label, fontSize = 18.sp)
                }
            }

            /* --- altro --- */
            section(R.string.section_other)
            item {
                OutlinedButton(
                    onClick = { context.startActivity(Intent(AndroidSettings.ACTION_HOME_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.open_home_chooser)) }
                OutlinedButton(
                    onClick = { context.startActivity(Intent(AndroidSettings.ACTION_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.open_android_settings)) }
                Text(
                    "v" + BuildConfigVersion.name(context),
                    color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }

    addNumberTo?.let { target ->
        AddNumberDialog(onDismiss = { addNumberTo = null }) { n ->
            store.updateSettings {
                if (target == "black") it.copy(blacklist = (it.blacklist + n).distinct())
                else it.copy(whitelist = (it.whitelist + n).distinct())
            }
            addNumberTo = null
        }
    }
    confirmDelete?.let { c ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.contact_delete_confirm, c.name)) },
            confirmButton = {
                TextButton(onClick = { store.deleteContact(c); confirmDelete = null }) {
                    Text(stringResource(R.string.delete), color = Red)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    if (showLanguages) {
        AlertDialog(
            onDismissRequest = { showLanguages = false },
            title = { Text(stringResource(R.string.section_language)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    LANGUAGES.forEach { (tag, name) ->
                        Text(
                            if (tag.isEmpty()) stringResource(R.string.language_system) else name,
                            fontSize = 20.sp,
                            modifier = Modifier.fillMaxWidth().clickable {
                                showLanguages = false
                                AppCompatDelegate.setApplicationLocales(
                                    if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
                                    else LocaleListCompat.forLanguageTags(tag)
                                )
                            }.padding(vertical = 12.dp),
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLanguages = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

private val LANGUAGES = listOf(
    "" to "",
    "it" to "Italiano",
    "en" to "English",
    "es" to "Español",
    "fr" to "Français",
    "de" to "Deutsch",
    "pt" to "Português",
    "ru" to "Русский",
    "ar" to "العربية",
    "hi" to "हिन्दी",
    "zh" to "中文",
)

private object BuildConfigVersion {
    fun name(context: Context): String =
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "?"
}

private fun missingPermissions(context: Context): List<String> = buildList {
    add(Manifest.permission.CALL_PHONE)
    if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
}.filter { context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }

private fun requestRole(context: Context, role: String, launcher: ActivityResultLauncher<Intent>, fallback: String) {
    val rm = context.getSystemService(RoleManager::class.java)
    if (rm.isRoleAvailable(role) && !rm.isRoleHeld(role)) {
        runCatching { launcher.launch(rm.createRequestRoleIntent(role)) }
            .onFailure { context.startActivity(Intent(fallback)) }
    } else {
        // Già assegnato oppure non richiedibile: apriamo le impostazioni di sistema.
        runCatching { context.startActivity(Intent(fallback)) }
    }
}

private fun LazyListScope.section(title: Int) {
    item {
        Column(Modifier.padding(top = 24.dp, bottom = 8.dp)) {
            Text(stringResource(title).uppercase(), color = Accent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            HorizontalDivider(Modifier.padding(top = 4.dp))
        }
    }
}

private fun LazyListScope.numberList(
    title: Int,
    numbers: List<String>,
    active: Boolean,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
) {
    item {
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(title), fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = if (active) Ink else Muted, modifier = Modifier.weight(1f))
            TextButton(onClick = onAdd) { Icon(Icons.Filled.Add, null); Text(stringResource(R.string.number_add)) }
        }
        if (numbers.isEmpty()) Text(stringResource(R.string.list_empty), color = Muted)
    }
    items(numbers, key = { "$title-$it" }) { n ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(n, fontSize = 18.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { onRemove(n) }) { Icon(Icons.Filled.Delete, stringResource(R.string.delete), tint = Red) }
        }
    }
}

@Composable
private fun formatTime(millis: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale))
}

@Composable
internal fun LogRow(e: LogEntry, contactName: String?, onCallBack: () -> Unit) {
    val (icon, color, label) = when (e.type) {
        LogType.MISSED -> Triple(Icons.AutoMirrored.Filled.CallMissed, Red, R.string.log_missed)
        LogType.REJECTED -> Triple(Icons.Filled.CallEnd, Muted, R.string.log_rejected)
        LogType.BLOCKED -> Triple(Icons.Filled.Block, Muted, R.string.log_blocked)
        LogType.BUSY -> Triple(Icons.Filled.PhoneDisabled, Amber, R.string.log_busy)
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                contactName ?: e.number.ifBlank { stringResource(R.string.call_hidden) },
                fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1,
            )
            Text(stringResource(label) + " · " + formatTime(e.time), color = Muted, fontSize = 14.sp)
        }
        if (e.number.any { it.isDigit() }) {
            IconButton(onClick = onCallBack) { Icon(Icons.Filled.Call, stringResource(R.string.log_call_back), tint = Green) }
        }
    }
}

@Composable
internal fun SmsRow(m: Sms, contactName: String?, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp))
            .background(Tile).clickable(onClick = onClick).padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                contactName ?: m.address, fontSize = 17.sp, maxLines = 1,
                fontWeight = if (m.read) FontWeight.Normal else FontWeight.Bold, modifier = Modifier.weight(1f),
            )
            Text(formatTime(m.date), color = Muted, fontSize = 13.sp)
        }
        Text(m.body, maxLines = 3, fontSize = 15.sp, color = Ink)
    }
}

@Composable
private fun SetupRow(label: String, ok: Boolean, onFix: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(if (ok) Green else Red))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(if (ok) R.string.status_ok else R.string.status_todo), color = if (ok) Green else Red)
        }
        if (!ok) Button(onClick = onFix) { Text(stringResource(R.string.action_activate)) }
        else TextButton(onClick = onFix) { Text(stringResource(R.string.action_change)) }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 17.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

@Composable
private fun AddNumberDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.number_add)) },
        text = {
            OutlinedTextField(
                value = text, onValueChange = { text = it }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                label = { Text(stringResource(R.string.contact_number)) },
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onAdd(text.trim()) }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/* ------------------------------------------------------ modifica contatto */

private class PickPhoneContact : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit) =
        Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? = intent?.data
}

/**
 * Apre il selettore della rubrica; la prima volta chiede l'accesso ai contatti, che serve
 * solo per la foto (senza, arrivano comunque nome e numero).
 */
@Composable
private fun rememberContactPicker(onPicked: (Prefill) -> Unit): () -> Unit {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(PickPhoneContact()) { uri ->
        uri?.let { ContactImport.read(context, it) }?.let(onPicked)
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        runCatching { picker.launch(Unit) }
    }
    return {
        if (ContactImport.canReadPhotos(context)) runCatching { picker.launch(Unit) }
        else permission.launch(Manifest.permission.READ_CONTACTS)
    }
}

@Composable
private fun EditContact(contactId: String?, prefill: Prefill?, onDone: () -> Unit) {
    val context = LocalContext.current
    val store = remember { Store.get(context) }
    val original = remember(contactId) { store.contacts.value.firstOrNull { it.id == contactId } }

    var name by rememberSaveable { mutableStateOf(original?.name ?: prefill?.name.orEmpty()) }
    var number by rememberSaveable { mutableStateOf(original?.number ?: prefill?.number.orEmpty()) }
    var photo by rememberSaveable { mutableStateOf(original?.photo ?: prefill?.photo) }

    fun replacePhoto(newPath: String?) {
        // elimina la foto temporanea precedente (mai quella originale: serve se si annulla)
        photo?.takeIf { it != original?.photo }?.let { File(it).delete() }
        photo = newPath
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) Photos.import(context, uri)?.let { replacePhoto(it) }
    }
    val pickContact = rememberContactPicker { p ->
        number = p.number
        if (name.isBlank()) name = p.name
        p.photo?.let { replacePhoto(it) }
    }

    fun cancel() {
        replacePhoto(original?.photo)
        onDone()
    }
    BackHandler { cancel() }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { cancel() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
            Text(
                stringResource(if (original == null) R.string.contact_add else R.string.contact_edit),
                fontSize = 24.sp, fontWeight = FontWeight.Bold,
            )
        }
        ContactPhoto(
            photo, name,
            Modifier.fillMaxWidth(0.6f).widthIn(max = 240.dp).aspectRatio(1f).align(Alignment.CenterHorizontally),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.weight(1f),
            ) { Icon(Icons.Filled.Photo, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.contact_photo_choose)) }
            if (photo != null) {
                OutlinedButton(onClick = { replacePhoto(null) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.contact_photo_remove))
                }
            }
        }
        OutlinedButton(onClick = pickContact, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Contacts, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.contact_import))
        }
        OutlinedTextField(
            value = name, onValueChange = { name = it }, singleLine = true,
            label = { Text(stringResource(R.string.contact_name)) },
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = number, onValueChange = { number = it }, singleLine = true,
            label = { Text(stringResource(R.string.contact_number)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { cancel() }, modifier = Modifier.weight(1f).height(56.dp)) {
                Text(stringResource(R.string.cancel), fontSize = 18.sp)
            }
            Button(
                onClick = {
                    if (original?.photo != null && original.photo != photo) File(original.photo).delete()
                    val c = (original ?: Contact(name = "", number = "")).copy(
                        name = name.trim(), number = number.trim(), photo = photo,
                    )
                    store.upsertContact(c)
                    onDone()
                },
                enabled = name.isNotBlank() && number.any { it.isDigit() },
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                modifier = Modifier.weight(1f).height(56.dp),
            ) { Text(stringResource(R.string.save), fontSize = 18.sp) }
        }
    }
}
