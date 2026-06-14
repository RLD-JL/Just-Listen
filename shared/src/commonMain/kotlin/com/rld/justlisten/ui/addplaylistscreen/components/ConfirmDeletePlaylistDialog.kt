package com.rld.justlisten.ui.addplaylistscreen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.rld.justlisten.ui.components.ConfirmDialog

@Composable
fun ConfirmDeletePlaylistDialog(
    playlistName: String,
    openDialog: MutableState<Boolean>,
    onConfirmDelete: () -> Unit
) {
    ConfirmDialog(
        title = "Delete Playlist",
        description = "Are you sure you want to delete \"$playlistName\"?\nThis action cannot be undone.",
        confirmText = "Yes, Delete",
        cancelText = "Cancel",
        openDialog = openDialog,
        onConfirm = onConfirmDelete
    )
}
