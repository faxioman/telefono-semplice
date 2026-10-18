package it.rapidosoft.semplice.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class Contact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val number: String,
    val photo: String? = null,
)

enum class FilterMode { OFF, BLACKLIST, WHITELIST }

enum class ThemeMode { LIGHT, DARK }

/** MISSED: nessuno ha risposto · REJECTED: "Butta giù" · BLOCKED: liste · BUSY: era già al telefono */
enum class LogType { MISSED, REJECTED, BLOCKED, BUSY }

data class LogEntry(val number: String, val time: Long, val type: LogType)

data class Settings(
    val pin: String = "",
    val showKeypad: Boolean = true,
    val filterMode: FilterMode = FilterMode.OFF,
    val blockHidden: Boolean = false,
    val blacklist: List<String> = emptyList(),
    val whitelist: List<String> = emptyList(),
    val theme: ThemeMode = ThemeMode.LIGHT,
    val keepAwake: Boolean = false,
    /** Riaccende subito lo schermo se viene premuto il tasto di accensione. */
    val wakeOnPower: Boolean = false,
    /** 0 = mai; altrimenti risponde da solo dopo questi secondi di squillo. */
    val autoAnswerSeconds: Int = 0,
    val autoAnswerOnlyContacts: Boolean = true,
    /** 0.05–1 = luminosità fissa dell'app; negativo = quella di sistema. */
    val brightness: Float = -1f,
)

/** Persistenza minimale su SharedPreferences (JSON). Unica istanza per processo. */
class Store private constructor(context: Context) {
    private val sp = context.getSharedPreferences("semplice", Context.MODE_PRIVATE)

    private val _contacts = MutableStateFlow(readContacts())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<Settings> = _settings

    private val _log = MutableStateFlow(readLog())
    /** Chiamate perse/bloccate/rifiutate, la più recente per prima. */
    val log: StateFlow<List<LogEntry>> = _log

    @Synchronized
    fun addLog(number: String?, type: LogType) {
        saveLog((listOf(LogEntry(number.orEmpty(), System.currentTimeMillis(), type)) + _log.value).take(MAX_LOG))
    }

    fun clearLog() = saveLog(emptyList())

    private fun saveLog(list: List<LogEntry>) {
        _log.value = list
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("n", it.number).put("t", it.time).put("k", it.type.name)) }
        sp.edit().putString(KEY_LOG, arr.toString()).apply()
    }

    private fun readLog(): List<LogEntry> = runCatching {
        val arr = JSONArray(sp.getString(KEY_LOG, "[]"))
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.getJSONObject(i)
            val type = runCatching { LogType.valueOf(o.getString("k")) }.getOrNull() ?: return@mapNotNull null
            LogEntry(o.optString("n"), o.getLong("t"), type)
        }
    }.getOrDefault(emptyList())

    fun findContact(number: String?): Contact? =
        _contacts.value.firstOrNull { Numbers.same(it.number, number) }

    fun upsertContact(contact: Contact) {
        val list = _contacts.value.toMutableList()
        val i = list.indexOfFirst { it.id == contact.id }
        if (i >= 0) list[i] = contact else list.add(contact)
        saveContacts(list)
    }

    fun deleteContact(contact: Contact) {
        contact.photo?.let { File(it).delete() }
        saveContacts(_contacts.value.filterNot { it.id == contact.id })
    }

    fun moveContact(contact: Contact, delta: Int) {
        val list = _contacts.value.toMutableList()
        val i = list.indexOfFirst { it.id == contact.id }
        val j = i + delta
        if (i < 0 || j !in list.indices) return
        list.add(j, list.removeAt(i))
        saveContacts(list)
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        val s = transform(_settings.value)
        _settings.value = s
        sp.edit().putString(KEY_SETTINGS, JSONObject().apply {
            put("pin", s.pin)
            put("showKeypad", s.showKeypad)
            put("filterMode", s.filterMode.name)
            put("blockHidden", s.blockHidden)
            put("blacklist", JSONArray(s.blacklist))
            put("whitelist", JSONArray(s.whitelist))
            put("theme", s.theme.name)
            put("keepAwake", s.keepAwake)
            put("wakeOnPower", s.wakeOnPower)
            put("autoAnswerSeconds", s.autoAnswerSeconds)
            put("autoAnswerOnlyContacts", s.autoAnswerOnlyContacts)
            put("brightness", s.brightness.toDouble())
        }.toString()).apply()
    }

    private fun saveContacts(list: List<Contact>) {
        _contacts.value = list
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id); put("name", it.name); put("number", it.number)
                put("photo", it.photo ?: "")
            })
        }
        sp.edit().putString(KEY_CONTACTS, arr.toString()).apply()
    }

    private fun readContacts(): List<Contact> = runCatching {
        val arr = JSONArray(sp.getString(KEY_CONTACTS, "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Contact(
                id = o.getString("id"),
                name = o.getString("name"),
                number = o.getString("number"),
                photo = o.optString("photo").ifBlank { null },
            )
        }
    }.getOrDefault(emptyList())

    private fun readSettings(): Settings = runCatching {
        val o = JSONObject(sp.getString(KEY_SETTINGS, null) ?: return Settings())
        Settings(
            pin = o.optString("pin"),
            showKeypad = o.optBoolean("showKeypad", true),
            filterMode = runCatching { FilterMode.valueOf(o.optString("filterMode")) }
                .getOrDefault(FilterMode.OFF),
            blockHidden = o.optBoolean("blockHidden", false),
            blacklist = o.optJSONArray("blacklist").toStringList(),
            whitelist = o.optJSONArray("whitelist").toStringList(),
            theme = runCatching { ThemeMode.valueOf(o.optString("theme")) }.getOrDefault(ThemeMode.LIGHT),
            keepAwake = o.optBoolean("keepAwake", false),
            wakeOnPower = o.optBoolean("wakeOnPower", false),
            autoAnswerSeconds = o.optInt("autoAnswerSeconds", 0),
            autoAnswerOnlyContacts = o.optBoolean("autoAnswerOnlyContacts", true),
            brightness = o.optDouble("brightness", -1.0).toFloat(),
        )
    }.getOrDefault(Settings())

    private fun JSONArray?.toStringList(): List<String> =
        if (this == null) emptyList() else (0 until length()).map { getString(it) }

    companion object {
        private const val KEY_CONTACTS = "contacts"
        private const val KEY_SETTINGS = "settings"
        private const val KEY_LOG = "call_log"
        private const val MAX_LOG = 100

        @Volatile private var instance: Store? = null

        fun get(context: Context): Store = instance ?: synchronized(this) {
            instance ?: Store(context.applicationContext).also { instance = it }
        }
    }
}
