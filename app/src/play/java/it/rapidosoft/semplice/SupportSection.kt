package it.rapidosoft.semplice

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Versione Play: "offrimi un caffè" come acquisto in-app, con i prezzi presi da Google Play. */
@Composable
internal fun SupportSection() {
    val context = LocalContext.current
    val jar = remember { TipJar(context) }
    DisposableEffect(jar) {
        jar.connect()
        onDispose { jar.close() }
    }
    val thanks = stringResource(R.string.tip_thanks)
    LaunchedEffect(jar) {
        jar.thanks.collect { Toast.makeText(context, thanks, Toast.LENGTH_LONG).show() }
    }
    val products by jar.products.collectAsState()
    if (products.isEmpty()) return

    Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(stringResource(R.string.buy_coffee), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            products.forEach { p ->
                Button(
                    onClick = { (context as? Activity)?.let { jar.buy(it, p) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDD00), contentColor = Color.Black),
                    modifier = Modifier.weight(1f),
                ) { Text(p.oneTimePurchaseOfferDetails?.formattedPrice ?: p.name, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
