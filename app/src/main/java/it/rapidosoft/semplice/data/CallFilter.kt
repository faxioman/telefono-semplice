package it.rapidosoft.semplice.data

import android.content.Context

object CallFilter {
    /** true se la chiamata in arrivo da [number] va rifiutata secondo le preferenze. */
    fun shouldBlock(context: Context, number: String?): Boolean {
        val store = Store.get(context)
        val s = store.settings.value
        if (Numbers.digits(number).isEmpty()) {
            return s.blockHidden || s.filterMode == FilterMode.WHITELIST
        }
        return when (s.filterMode) {
            FilterMode.OFF -> false
            FilterMode.BLACKLIST -> s.blacklist.any { Numbers.same(it, number) }
            FilterMode.WHITELIST -> store.findContact(number) == null &&
                s.whitelist.none { Numbers.same(it, number) }
        }
    }
}
