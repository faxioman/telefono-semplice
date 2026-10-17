package it.rapidosoft.semplice.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.LruCache
import java.io.File
import java.nio.ByteBuffer

object Photos {
    private const val MAX_SIDE = 900
    private val cache = LruCache<String, Bitmap>(24)

    fun load(path: String): Bitmap? {
        val f = File(path)
        if (!f.exists()) return null
        val key = "$path:${f.lastModified()}"
        cache.get(key)?.let { return it }
        val bmp = BitmapFactory.decodeFile(path) ?: return null
        cache.put(key, bmp)
        return bmp
    }

    /** Copia (ridimensionata) un'immagine scelta dall'utente nella memoria privata dell'app. */
    fun import(context: Context, uri: Uri): String? =
        save(context, ImageDecoder.createSource(context.contentResolver, uri))

    fun importBytes(context: Context, bytes: ByteArray): String? =
        save(context, ImageDecoder.createSource(ByteBuffer.wrap(bytes)))

    private fun save(context: Context, source: ImageDecoder.Source): String? = runCatching {
        val bmp = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val w = info.size.width
            val h = info.size.height
            val scale = minOf(1f, MAX_SIDE.toFloat() / maxOf(w, h))
            decoder.setTargetSize((w * scale).toInt().coerceAtLeast(1), (h * scale).toInt().coerceAtLeast(1))
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val out = File(dir, "${System.currentTimeMillis()}.jpg")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        out.absolutePath
    }.getOrNull()
}
