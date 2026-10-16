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

data class Settings(
    val pin: String = "",
    val showKeypad: Boolean = true,
    val filterMode: FilterMode = FilterMode.OFF,
    val blockHidden: Boolean = false,
    val blacklist: List<String> = emptyList(),
    val whitelist: List<String> = emptyList(),
)

/** Persistenza minimale su SharedPreferences (JSON). Unica istanza per processo. */
class Store private constructor(context: Context) {
    private val sp = context.getSharedPreferences("semplice", Context.MODE_PRIVATE)

    private val _contacts = MutableStateFlow(readContacts())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<Settings> = _settings

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
        )
    }.getOrDefault(Settings())

    private fun JSONArray?.toStringList(): List<String> =
        if (this == null) emptyList() else (0 until length()).map { getString(it) }

    companion object {
        private const val KEY_CONTACTS = "contacts"
        private const val KEY_SETTINGS = "settings"

        @Volatile private var instance: Store? = null

        fun get(context: Context): Store = instance ?: synchronized(this) {
            instance ?: Store(context.applicationContext).also { instance = it }
        }
    }
}
