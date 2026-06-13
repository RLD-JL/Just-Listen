package com.rld.justlisten.ui.artistprofile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.unsplashcalls.UnsplashPhoto
import com.rld.justlisten.datalayer.webservices.apis.unsplashcalls.getUnsplashPhotos
import com.rld.justlisten.util.rememberImagePicker
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

enum class ImageTarget { PROFILE_PIC, COVER_PHOTO }

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
    val coroutineScope = rememberCoroutineScope()
    val apiClient = koinInject<ApiClient>()

    var name by remember(initialName) { mutableStateOf(initialName) }
    var bio by remember(initialBio) { mutableStateOf(initialBio ?: "") }
    var profilePicUrl by remember(initialProfilePicUrl) { mutableStateOf(initialProfilePicUrl ?: "") }
    var coverPhotoUrl by remember(initialCoverPhotoUrl) { mutableStateOf(initialCoverPhotoUrl ?: "") }

    var activeTarget by remember { mutableStateOf<ImageTarget?>(null) }
    var showSourceSelector by remember { mutableStateOf(false) }
    var showUnsplashSearch by remember { mutableStateOf(false) }
    val presetTags = remember { listOf("neon", "space", "beach", "nature", "abstract", "lofi", "texture", "aesthetic", "pattern") }
    var unsplashQuery by remember { mutableStateOf(presetTags.random()) }
    var unsplashPhotos by remember { mutableStateOf<List<UnsplashPhoto>>(emptyList()) }
    var isLoadingPresets by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(1) }
    var canLoadMore by remember { mutableStateOf(true) }

    val fetchPhotos: suspend (query: String, isNewSearch: Boolean) -> Unit = { q, isNewSearch ->
        isLoadingPresets = true
        val targetPage = if (isNewSearch) 1 else currentPage + 1
        val results = apiClient.getUnsplashPhotos(q, page = targetPage, perPage = 18)
        if (results != null) {
            if (isNewSearch) {
                unsplashPhotos = results.distinctBy { it.id }
                currentPage = 1
            } else {
                unsplashPhotos = (unsplashPhotos + results).distinctBy { it.id }
                currentPage = targetPage
            }
            canLoadMore = results.size >= 18
        } else {
            if (isNewSearch) {
                unsplashPhotos = emptyList()
            }
            canLoadMore = false
        }
        isLoadingPresets = false
    }

    // Setup gallery image launchers
    val profilePicPicker = rememberImagePicker { path ->
        profilePicUrl = path
        showSourceSelector = false
    }
    val coverPhotoPicker = rememberImagePicker { path ->
        coverPhotoUrl = path
        showSourceSelector = false
    }

    val isValidProfilePic = profilePicUrl.isBlank() || 
            profilePicUrl.startsWith("https://", ignoreCase = true) || 
            profilePicUrl.startsWith("file://", ignoreCase = true) || 
            profilePicUrl.startsWith("/") ||
            profilePicUrl.startsWith("content://", ignoreCase = true)

    val isValidCoverPhoto = coverPhotoUrl.isBlank() || 
            coverPhotoUrl.startsWith("https://", ignoreCase = true) || 
            coverPhotoUrl.startsWith("file://", ignoreCase = true) || 
            coverPhotoUrl.startsWith("/") ||
            coverPhotoUrl.startsWith("content://", ignoreCase = true)

    // Trigger initial preset search when opening Unsplash preset panel
    LaunchedEffect(showUnsplashSearch) {
        if (showUnsplashSearch && unsplashPhotos.isEmpty()) {
            fetchPhotos(unsplashQuery, true)
        }
    }

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
                        .heightIn(max = 500.dp)
                        .then(
                            if (!showUnsplashSearch && !showSourceSelector) {
                                Modifier.verticalScroll(rememberScrollState())
                            } else {
                                Modifier
                            }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (showSourceSelector) {
                        // Image Source Selector Screen (Choose Gallery vs Presets)
                        val titleText = if (activeTarget == ImageTarget.PROFILE_PIC) "Profile Picture" else "Cover Banner"
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select $titleText",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { showSourceSelector = false }) {
                                Text("Back")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Gallery source button
                        Card(
                            onClick = {
                                if (activeTarget == ImageTarget.PROFILE_PIC) {
                                    profilePicPicker.launch()
                                } else {
                                    coverPhotoPicker.launch()
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text("Choose from Gallery", fontWeight = FontWeight.Bold)
                                    Text("Pick an image from your device storage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preset search button
                        Card(
                            onClick = {
                                showSourceSelector = false
                                showUnsplashSearch = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wallpaper,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text("Search Preset Artworks", fontWeight = FontWeight.Bold)
                                    Text("Browse high-quality photos from Unsplash", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else if (showUnsplashSearch) {
                        // Unsplash Preset Browser Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Search Presets",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { 
                                showUnsplashSearch = false
                                showSourceSelector = true 
                            }) {
                                Text("Back")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = unsplashQuery,
                            onValueChange = { unsplashQuery = it },
                            placeholder = { Text("Search artwork...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                coroutineScope.launch {
                                    fetchPhotos(unsplashQuery, true)
                                }
                            })
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Suggested Searches Section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "SUGGESTED SEARCHES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("neon", "space", "beach", "nature", "abstract").forEach { tag ->
                                    Text(
                                        text = tag,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                unsplashQuery = tag
                                                coroutineScope.launch {
                                                    fetchPhotos(tag, true)
                                                }
                                            }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Photos from Unsplash",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isLoadingPresets && unsplashPhotos.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            if (unsplashPhotos.isEmpty()) {
                                Text(
                                    text = "No presets found. Try searching above.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 32.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                ) {
                                    val gridState = rememberLazyGridState()

                                    LaunchedEffect(gridState) {
                                        snapshotFlow {
                                            val layoutInfo = gridState.layoutInfo
                                            val totalItems = layoutInfo.totalItemsCount
                                            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                            lastVisibleItemIndex >= totalItems - 3 && canLoadMore && !isLoadingPresets && unsplashPhotos.isNotEmpty()
                                        }
                                        .collect { shouldLoad ->
                                            if (shouldLoad) {
                                                fetchPhotos(unsplashQuery, false)
                                            }
                                        }
                                    }

                                    LazyVerticalGrid(
                                        state = gridState,
                                        columns = GridCells.Fixed(3),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        itemsIndexed(
                                            items = unsplashPhotos,
                                            key = { _, photo -> photo.id }
                                        ) { index, photo ->
                                            Box(
                                                modifier = Modifier
                                                    .aspectRatio(1.2f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .clickable {
                                                        if (activeTarget == ImageTarget.COVER_PHOTO) {
                                                            coverPhotoUrl = photo.urls.regular
                                                        } else {
                                                            profilePicUrl = photo.urls.regular
                                                        }
                                                        showUnsplashSearch = false
                                                    }
                                            ) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(photo.urls.thumb),
                                                    contentDescription = photo.description,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                        if (isLoadingPresets && unsplashPhotos.isNotEmpty()) {
                                            item(span = { GridItemSpan(maxLineSpan) }) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Main Profile Editor Screen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            // Cover Photo Preview & Choice Trigger
                            val coverPainter = rememberAsyncImagePainter(coverPhotoUrl.ifBlank { "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe" })
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        activeTarget = ImageTarget.COVER_PHOTO
                                        showSourceSelector = true
                                    }
                            ) {
                                Image(
                                    painter = coverPainter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                        Text(
                                            "Change Cover",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Profile Picture Preview & Choice Trigger (Centered/Overlapping)
                            val avatarPainter = rememberAsyncImagePainter(profilePicUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" })
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.BottomCenter)
                                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        activeTarget = ImageTarget.PROFILE_PIC
                                        showSourceSelector = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = avatarPainter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Customize Profile Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                if (!showUnsplashSearch && !showSourceSelector) {
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
            }
        )
    }
}
