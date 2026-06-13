package com.rld.justlisten.ui.artistprofile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    showDialog: MutableState<Boolean>,
    initialName: String,
    initialBio: String?,
    initialProfilePicUrl: String?,
    initialCoverPhotoUrl: String?,
    onSaveClicked: (name: String, bio: String?, profilePicUrl: String?, coverPhotoUrl: String?) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var name by remember(initialName) { mutableStateOf(initialName) }
    var bio by remember(initialBio) { mutableStateOf(initialBio ?: "") }
    var profilePicUrl by remember(initialProfilePicUrl) { mutableStateOf(initialProfilePicUrl ?: "") }
    var coverPhotoUrl by remember(initialCoverPhotoUrl) { mutableStateOf(initialCoverPhotoUrl ?: "") }

    val isValidProfilePic = profilePicUrl.isBlank() || profilePicUrl.startsWith("https://", ignoreCase = true)
    val isValidCoverPhoto = coverPhotoUrl.isBlank() || coverPhotoUrl.startsWith("https://", ignoreCase = true)

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            shape = RoundedCornerShape(24.dp),
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Decoration Badge
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Edit Profile",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Customize your profile details",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Name Input Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("Your name") },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bio Input Field
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        placeholder = { Text("Tell us about yourself...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Profile Picture URL Input Field
                    OutlinedTextField(
                        value = profilePicUrl,
                        onValueChange = { profilePicUrl = it },
                        label = { Text("Profile Image URL") },
                        placeholder = { Text("https://example.com/avatar.jpg") },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isValidProfilePic,
                        supportingText = {
                            if (!isValidProfilePic) {
                                Text("URL must start with https://", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cover Photo URL Input Field
                    OutlinedTextField(
                        value = coverPhotoUrl,
                        onValueChange = { coverPhotoUrl = it },
                        label = { Text("Cover Banner URL") },
                        placeholder = { Text("https://example.com/banner.jpg") },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isValidCoverPhoto,
                        supportingText = {
                            if (!isValidCoverPhoto) {
                                Text("URL must start with https://", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (name.isNotBlank() && isValidProfilePic && isValidCoverPhoto) {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    showDialog.value = false
                                    onSaveClicked(
                                        name,
                                        bio.ifBlank { null },
                                        profilePicUrl.ifBlank { null },
                                        coverPhotoUrl.ifBlank { null }
                                    )
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
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
                    val isEnabled = name.isNotBlank() && isValidProfilePic && isValidCoverPhoto
                    Button(
                        onClick = {
                            if (isEnabled) {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                showDialog.value = false
                                onSaveClicked(
                                    name,
                                    bio.ifBlank { null },
                                    profilePicUrl.ifBlank { null },
                                    coverPhotoUrl.ifBlank { null }
                                )
                            }
                        },
                        enabled = isEnabled,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                brush = if (isEnabled) {
                                    Brush.linearGradient(
                                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(Color.Transparent, Color.Transparent)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Save Changes",
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }

                    // Cancel Text Button
                    TextButton(
                        onClick = {
                            showDialog.value = false
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
