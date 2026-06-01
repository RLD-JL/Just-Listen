package com.rld.justlisten.ui.addplaylistscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddPlaylistDialog(
    openDialog: MutableState<Boolean>,
    onAddPlaylistClicked: (String, String?) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            shape = RoundedCornerShape(24.dp),
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Decoration Badge
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF9C27B0), Color(0xFF00BCD4))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Create Playlist",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Curate your own musical soundtrack",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title Input Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            if (it.length <= 15) {
                                title = it
                            }
                        },
                        label = { Text("Title") },
                        placeholder = { Text("e.g. Chill Vibes 🌊") },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onPreviewKeyEvent {
                                if (it.key == Key.Tab) {
                                    focusManager.moveFocus(FocusDirection.Down)
                                    true
                                } else {
                                    false
                                }
                            },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        supportingText = {
                            Text(
                                text = "${title.length}/15",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                                fontSize = 10.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00BCD4),
                            focusedLabelColor = Color(0xFF00BCD4),
                            cursorColor = Color(0xFF00BCD4)
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Description Input Field
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            if (it.length <= 144) {
                                description = it
                            }
                        },
                        label = { Text("Description") },
                        placeholder = { Text("e.g. The perfect collection of lo-fi and ambient beats...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (title.isNotBlank()) {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    openDialog.value = false
                                    onAddPlaylistClicked(title, description.takeIf { it.isNotBlank() })
                                    description = ""
                                    title = ""
                                }
                            }
                        ),
                        supportingText = {
                            Text(
                                text = "${description.length}/144",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                                fontSize = 10.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9C27B0),
                            focusedLabelColor = Color(0xFF9C27B0),
                            cursorColor = Color(0xFF9C27B0)
                        )
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Gradient Action Button
                    val isEnabled = title.isNotBlank()
                    Button(
                        onClick = {
                            if (isEnabled) {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                openDialog.value = false
                                onAddPlaylistClicked(title, description.takeIf { it.isNotBlank() })
                                description = ""
                                title = ""
                            }
                        },
                        enabled = isEnabled,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .then(
                                if (isEnabled) {
                                    Modifier.background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF9C27B0), Color(0xFF00BCD4))
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                } else Modifier
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Add Collection",
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }

                    // Cancel Text Button
                    TextButton(
                        onClick = {
                            description = ""
                            title = ""
                            openDialog.value = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        )
    }
}
