package it.rapidosoft.semplice

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Versione GitHub: link a Buy Me a Coffee. */
@Composable
internal fun SupportSection() {
    val context = LocalContext.current
    Button(
        onClick = {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/faxioman")))
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDD00), contentColor = Color.Black),
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
    ) { Text(stringResource(R.string.buy_coffee), fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}
