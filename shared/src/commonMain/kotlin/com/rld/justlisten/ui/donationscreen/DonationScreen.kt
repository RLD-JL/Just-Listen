package com.rld.justlisten.ui.donationscreen

import com.rld.justlisten.ui.utils.showToast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import com.rld.justlisten.util.clipEntryOf
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun DonationScreen() {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
    ) {
        Text(
            "If you like the app and would like more features and bug fixes, " +
                    "please consider supporting me",
            modifier = Modifier.padding(20.dp)
        )
        HorizontalDivider(thickness = 2.dp, modifier = Modifier.fillMaxWidth())

        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            TextField(
                "bc1qcsuapkvhpy3tlfrmmxhmf2cru9f2ar8cs4605w", modifier = Modifier.clickable(
                    onClick = {
                        coroutineScope.launch {
                            clipboard.setClipEntry(clipEntryOf("bc1qcsuapkvhpy3tlfrmmxhmf2cru9f2ar8cs4605w"))
                        }
                        showToast("Copied BTC Address")
                    }),
                onValueChange = {},
                enabled = false,
                label = { Text(text = "BTC Address") },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(unfocusedIndicatorColor = Color.Transparent)
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
                        coroutineScope.launch {
                            clipboard.setClipEntry(clipEntryOf("0x3A9b38ba07D4E9263c5595C2DbF1dD13a43b577C"))
                        }
                        showToast("Copied ETH Address")
                    }),
                onValueChange = {},
                enabled = false,
                label = { Text(text = "ETH Address") },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(unfocusedIndicatorColor = Color.Transparent)
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
                        coroutineScope.launch {
                            clipboard.setClipEntry(clipEntryOf("GjfvqY9ophJZ7r475Wka5GH8HafDj5kFirE86g1jpDYe"))
                        }
                        showToast("Copied SOL Address")
                    }),
                onValueChange = {},
                enabled = false,
                label = { Text(text = "SOL Address") },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(unfocusedIndicatorColor = Color.Transparent)
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
                        coroutineScope.launch {
                            clipboard.setClipEntry(clipEntryOf("0x3A9b38ba07D4E9263c5595C2DbF1dD13a43b577C"))
                        }
                        showToast("Copied Audius Address")
                    }),
                onValueChange = {},
                enabled = false,
                label = { Text(text = "Audius Address") },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(unfocusedIndicatorColor = Color.Transparent)
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
