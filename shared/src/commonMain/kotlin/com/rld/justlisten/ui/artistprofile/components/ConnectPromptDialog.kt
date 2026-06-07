package com.rld.justlisten.ui.artistprofile.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConnectPromptDialog(
    onDismissRequest: () -> Unit,
    onConnectClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Connect Audius Account")
        },
        text = {
            Text(text = "Connecting your Audius account allows you to follow artists, sync your favorites and playlists, and access personalized cloud features.")
        },
        confirmButton = {
            TextButton(onClick = onConnectClick) {
                Text(text = "Connect Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Cancel")
            }
        }
    )
}
