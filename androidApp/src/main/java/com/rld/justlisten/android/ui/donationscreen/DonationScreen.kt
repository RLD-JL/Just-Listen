package com.rld.justlisten.android.ui.donationscreen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun DonationScreen() {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        Text(
            "If you would like to support the app and get more features for it, please donate to" +
                    "the following addresses", color = MaterialTheme.colors.primary
        )
        Text("Bitcoin", modifier = Modifier.clickable(
            onClick = {
                clipboardManager.setText(AnnotatedString(("BTC address")))
                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT)
                    .show()
            }
        ))

        TextButton(
            border = BorderStroke(1.dp, MaterialTheme.colors.primary),
            onClick = {
            clipboardManager.setText(AnnotatedString(("BTC address")))
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT)
                .show()
        }) {
            Text("yolooo")
        }
        Text("Ethereum")
        BasicText(text = "Ethereum address")
        Text("Audius")
        BasicText(text = "Audius address")
        Text("Solana")
        BasicText(text = "Solana address")
    }
}