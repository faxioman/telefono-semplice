package it.rapidosoft.semplice.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts

/** Dati letti da un contatto della rubrica, pronti per l'editor. */
data class Prefill(val name: String, val number: String, val photo: String?)

object ContactImport {
    /** La foto richiede READ_CONTACTS: il selettore concede solo nome e numero. */
    fun canReadPhotos(context: Context) =
        context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    /** [phoneUri] è il risultato di ACTION_PICK su Phone.CONTENT_URI. */
    fun read(context: Context, phoneUri: Uri): Prefill? = runCatching {
        context.contentResolver.query(
            phoneUri, arrayOf(Phone.NUMBER, Phone.DISPLAY_NAME, Phone.CONTACT_ID), null, null, null,
        )?.use { c ->
            if (!c.moveToFirst()) return null
            Prefill(
                name = c.getString(1).orEmpty(),
                number = c.getString(0).orEmpty(),
                photo = photoOf(context, c.getLong(2)),
            )
        }
    }.getOrNull()

    private fun photoOf(context: Context, contactId: Long): String? {
        if (!canReadPhotos(context)) return null
        val uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId)
        return runCatching {
            Contacts.openContactPhotoInputStream(context.contentResolver, uri, true) // alta risoluzione
                ?.use { Photos.importBytes(context, it.readBytes()) }
        }.getOrNull()
    }
}
