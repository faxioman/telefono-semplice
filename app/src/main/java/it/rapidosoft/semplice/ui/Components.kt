package it.rapidosoft.semplice.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.rapidosoft.semplice.data.Photos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val avatarColors = listOf(
    // niente verde/rosso: sono i colori di "chiama" e "butta giù"
    Color(0xFF1565C0), Color(0xFF6A1B9A), Color(0xFFAD1457),
    Color(0xFFE65100), Color(0xFF4E342E), Color(0xFF37474F), Color(0xFF283593),
)

fun initials(name: String): String =
    name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifEmpty { "?" }

/** Foto del contatto o, se manca, le iniziali su sfondo colorato. */
@Composable
fun ContactPhoto(path: String?, name: String, modifier: Modifier = Modifier, corner: Dp = 24.dp) {
    val bitmap by produceState<ImageBitmap?>(null, path) {
        value = path?.let { withContext(Dispatchers.IO) { Photos.load(it)?.asImageBitmap() } }
    }
    val color = avatarColors[(name.hashCode() and 0x7fffffff) % avatarColors.size]
    BoxWithConstraints(
        modifier.clip(RoundedCornerShape(corner)).background(color),
        contentAlignment = Alignment.Center,
    ) {
        val b = bitmap
        if (b != null) {
            Image(b, contentDescription = name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            val size = with(LocalDensity.current) { (minOf(maxWidth, maxHeight) * 0.38f).toSp() }
            Text(initials(name), color = Color.White, fontSize = size, fontWeight = FontWeight.Bold)
        }
    }
}

/** Bottone grande con icona e scritta, pensato per dita poco precise. */
@Composable
fun BigButton(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp))
            Text(text, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/** Schermo più largo che alto (telefono in orizzontale). */
@Composable
fun isLandscape(): Boolean {
    val c = LocalConfiguration.current
    return c.screenWidthDp > c.screenHeightDp
}

@Composable
fun CenteredBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center) { content() }
}
