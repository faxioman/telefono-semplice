package it.rapidosoft.semplice.ui

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.rapidosoft.semplice.R
import kotlin.math.min
import kotlin.math.roundToInt

private const val OUTPUT_SIDE = 900

/**
 * Ritaglio quadrato della foto di un contatto: si sposta trascinando e si
 * ingrandisce/riduce con due dita. Restituisce l'area visibile nel riquadro.
 */
@Composable
fun PhotoCropper(source: Bitmap, onCancel: () -> Unit, onDone: (Bitmap) -> Unit) {
    val image = remember(source) { source.asImageBitmap() }
    var sidePx by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(0f) } // 0 = non ancora inizializzato
    var offset by remember { mutableStateOf(Offset.Zero) }
    val minScale = if (sidePx > 0) sidePx / min(image.width, image.height) else 1f

    // La foto deve sempre coprire tutto il riquadro: niente bordi vuoti.
    fun clamp(o: Offset, s: Float): Offset {
        val maxX = (image.width * s - sidePx) / 2
        val maxY = (image.height * s - sidePx) / 2
        return Offset(o.x.coerceIn(-maxX, maxX), o.y.coerceIn(-maxY, maxY))
    }

    fun crop(): Bitmap {
        val left = (sidePx - image.width * scale) / 2 + offset.x
        val top = (sidePx - image.height * scale) / 2 + offset.y
        val x = (-left / scale).roundToInt().coerceIn(0, source.width - 1)
        val y = (-top / scale).roundToInt().coerceIn(0, source.height - 1)
        val side = (sidePx / scale).roundToInt().coerceAtMost(min(source.width - x, source.height - y)).coerceAtLeast(1)
        val square = Bitmap.createBitmap(source, x, y, side, side)
        return if (side > OUTPUT_SIDE) Bitmap.createScaledBitmap(square, OUTPUT_SIDE, OUTPUT_SIDE, true) else square
    }

    BackHandler(onBack = onCancel)
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.photo_adjust), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.crop_hint), color = Muted, fontSize = 15.sp, textAlign = TextAlign.Center)
        BoxWithConstraints(
            Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            val side = min(maxWidth.value, maxHeight.value).dp
            val px = with(LocalDensity.current) { side.toPx() }
            if (px != sidePx) {
                sidePx = px
                scale = px / min(image.width, image.height)
                offset = Offset.Zero
            }
            Canvas(
                Modifier.size(side).clip(RoundedCornerShape(24.dp)).background(Color.Black)
                    .pointerInput(image, px) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(minScale, minScale * 6)
                            offset = clamp(offset + pan, scale)
                        }
                    },
            ) {
                val w = image.width * scale
                val h = image.height * scale
                drawImage(
                    image,
                    dstOffset = IntOffset(((size.width - w) / 2 + offset.x).roundToInt(), ((size.height - h) / 2 + offset.y).roundToInt()),
                    dstSize = IntSize(w.roundToInt(), h.roundToInt()),
                    filterQuality = FilterQuality.High,
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(56.dp)) {
                Text(stringResource(R.string.cancel), fontSize = 18.sp)
            }
            Button(
                onClick = { if (sidePx > 0) onDone(crop()) },
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                modifier = Modifier.weight(1f).height(56.dp),
            ) { Text(stringResource(R.string.crop_use), fontSize = 18.sp) }
        }
    }
}
