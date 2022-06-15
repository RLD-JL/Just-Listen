package com.rld.justlisten.android.ui.addplaylistscreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddPlaylistDialog(
    openDialog: MutableState<Boolean>,
    onAddPlaylistClicked: (String, String?) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf<String?>("") }
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = null,
            text = {
                Column {
                    Text(
                        text = "Add New Playlist",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    TextField(
                        singleLine = true,
                        modifier = Modifier.onPreviewKeyEvent {
                            if (it.key == Key.Tab) {
                                focusManager.moveFocus(FocusDirection.Down)
                                true
                            } else {
                                false
                            }
                        },
                        maxLines = 1,
                        value = title,
                        onValueChange = {
                            if (it.length <= 15)
                                title = it
                        },
                        label = { Text(text = "Title") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    TextField(
                        maxLines = 2,
                        modifier = Modifier.padding(top = 5.dp),
                        value = description.toString(),
                        onValueChange = {
                            if (it.length <= 144)
                                description = it
                        },
                        label = { Text(text = "Description") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                openDialog.value = false
                                onAddPlaylistClicked(title, description)
                                description = ""
                                title = ""
                            }
                        )
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
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            openDialog.value = false
                            onAddPlaylistClicked(title, description)
                            description = ""
                            title = ""
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        )
    }
}