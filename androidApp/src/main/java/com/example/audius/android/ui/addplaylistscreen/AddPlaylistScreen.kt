package com.example.audius.android.ui.addplaylistscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audius.viewmodel.screens.addplaylist.AddPlaylistState

@Composable
fun AddPlaylistScreen(
    addPlaylistState: AddPlaylistState
) {
    Column {
        val openDialog = remember { mutableStateOf(false) }

        Button(onClick = {
            openDialog.value = true
        }) {
            Text("Click me")
        }

        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDialog.value = false
                },
                title = {
                    Text(
                        text = "Add New Playlist",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(Modifier.padding(5.dp)) {
                        TextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(text = "Title") },
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Blue,
                            )
                        )
                        TextField(
                            modifier = Modifier.padding(top = 5.dp),
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(text = "Description") }
                        )
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.padding(all = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { openDialog.value = false }
                        ) {
                            Text("Add")
                        }
                    }
                }
            )
        }
    }

}
