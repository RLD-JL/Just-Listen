package com.rld.justlisten.android.ui.donationscreen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DonationScreen() {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
    ) {
        Text(
            "If you like the app and would like more features and bug fixes, " +
                    "please consider supporting me",
            modifier = Modifier.padding(20.dp)
        )
        Divider(thickness = 2.dp, modifier = Modifier.fillMaxWidth())

        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            TextField(
                "bc1qcsuapkvhpy3tlfrmmxhmf2cru9f2ar8cs4605w", modifier = Modifier.clickable(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(("bc1qcsuapkvhpy3tlfrmmxhmf2cru9f2ar8cs4605w")))
                        Toast.makeText(context, "Copied BTC Address", Toast.LENGTH_SHORT)
                            .show()
                    }),
                onValueChange = {},
                enabled = false,
                label = { Text(text = "BTC Address") },
                shape = CircleShape,
                colors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = Color.Transparent)
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            TextField(
                "0x3A9b38ba07D4E9263c5595C2DbF1dD13a43b577C", modifier = Modifier.clickable(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(("0x3A9b38ba07D4E9263c5595C2DbF1dD13a43b577C")))
                        Toast.makeText(context, "Copied ETH Address", Toast.LENGTH_SHORT)
                            .show()
                    }),
                onValueChange = {},
                enabled = false,
                label = { Text(text = "ETH Address") },
                shape = CircleShape,
                colors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = Color.Transparent)
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            TextField(
                "GjfvqY9ophJZ7r475Wka5GH8HafDj5kFirE86g1jpDYe", modifier = Modifier.clickable(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(("GjfvqY9ophJZ7r475Wka5GH8HafDj5kFirE86g1jpDYe")))
                        Toast.makeText(context, "Copied SOL Address", Toast.LENGTH_SHORT)
                            .show()
                    }),
                onValueChange = {},
                enabled = false,
                label = { Text(text = "SOL Address") },
                shape = CircleShape,
                colors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = Color.Transparent)
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            TextField(
                "0x3A9b38ba07D4E9263c5595C2DbF1dD13a43b577C", modifier = Modifier.clickable(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(("0x3A9b38ba07D4E9263c5595C2DbF1dD13a43b577C")))
                        Toast.makeText(context, "Copied Audius Address", Toast.LENGTH_SHORT)
                            .show()
                    }),
                onValueChange = {},
                enabled = false,
                label = { Text(text = "Audius Address") },
                shape = CircleShape,
                colors = TextFieldDefaults.textFieldColors(unfocusedIndicatorColor = Color.Transparent)
            )
        }
        val uriHandler = LocalUriHandler.current
        Text(
            text = "Ko-Fi", modifier = Modifier
                .clickable {
                    uriHandler.openUri("https://ko-fi.com/rldjl")
                }
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color.Cyan,
            style = TextStyle(textDecoration = TextDecoration.Underline),
            fontSize = 18.sp
        )
    }
}