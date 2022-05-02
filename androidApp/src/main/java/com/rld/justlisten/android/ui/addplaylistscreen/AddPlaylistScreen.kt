package com.rld.justlisten.android.ui.addplaylistscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.android.R
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.AddPlaylist
import com.rld.justlisten.viewmodel.screens.addplaylist.AddPlaylistState

@Composable
fun AddPlaylistScreen(
    addPlaylistState: AddPlaylistState,
    onAddPlaylistClicked: (String, String?) -> Unit,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit
) {
    val openDialog = remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxWidth()) {
        item { AddPlaylistRow(openDialog) }
        item { Divider(color = MaterialTheme.colors.primary, thickness = 0.5.dp) }
        item { AddPlaylistDialog(openDialog, onAddPlaylistClicked) }
        itemsIndexed(addPlaylistState.playlistsCreated) { index, playlist ->
            PlaylistViewItem(playlist, clickedToAddSongToPlaylist)
        }
    }
}

@Composable
fun AddPlaylistRow(openDialog: MutableState<Boolean>) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = { openDialog.value = true })
    ) {
        Icon(
            painterResource(id = R.drawable.ic_add_to_playlist_foreground),
            tint = MaterialTheme.colors.onSurface,
            contentDescription = null,
            modifier = Modifier.height(75.dp)
        )
        Text("New Playlist", modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun PlaylistViewItem(
    playlist: AddPlaylist,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = {
                clickedToAddSongToPlaylist(
                    playlist.playlistName,
                    playlist.playlistDescription,
                    playlist.songsList ?: emptyList()
                )
            })
    ) {
        Icon(
            painterResource(id = R.drawable.ic_playlist_icon),
            tint = MaterialTheme.colors.onSurface,
            contentDescription = null,
            modifier = Modifier.height(75.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(playlist.playlistName, modifier = Modifier.fillMaxWidth())
    }
}

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
            backgroundColor = MaterialTheme.colors.primaryVariant,
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
                        color = MaterialTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    TextField(
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
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Add")
                    }
                }
            }
        )
    }
}
