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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.unsplashcalls.UnsplashPhoto
import com.rld.justlisten.datalayer.webservices.apis.unsplashcalls.getUnsplashPhotos
import com.rld.justlisten.datalayer.webservices.apis.authcalls.UserCoinModel
import com.rld.justlisten.util.rememberImagePicker
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

enum class ImageTarget { PROFILE_PIC, COVER_PHOTO }

// Custom Vector Logos
val XLogoIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "XLogoIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
            moveTo(18.244f, 2.25f)
            lineToRelative(3.308f, 0f)
            lineToRelative(-7.227f, 8.26f)
            lineToRelative(8.502f, 11.24f)
            lineToRelative(-6.657f, 0f)
            lineToRelative(-5.214f, -6.817f)
            lineToRelative(-5.966f, 6.817f)
            lineToRelative(-3.31f, 0f)
            lineToRelative(7.73f, -8.835f)
            lineToRelative(-8.124f, -10.665f)
            lineToRelative(6.828f, 0f)
            lineToRelative(4.716f, 6.236f)
            close()
        }
    }.build()
}

val InstagramLogoIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "InstagramLogoIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = androidx.compose.ui.graphics.SolidColor(Color.White),
            strokeLineWidth = 2f
        ) {
            moveTo(7f, 2f)
            lineTo(17f, 2f)
            arcTo(5f, 5f, 0f, false, true, 22f, 7f)
            lineTo(22f, 17f)
            arcTo(5f, 5f, 0f, false, true, 17f, 22f)
            lineTo(7f, 22f)
            arcTo(5f, 5f, 0f, false, true, 2f, 17f)
            lineTo(2f, 7f)
            arcTo(5f, 5f, 0f, false, true, 7f, 2f)
            close()
        }
        path(
            stroke = androidx.compose.ui.graphics.SolidColor(Color.White),
            strokeLineWidth = 2f
        ) {
            moveTo(12f, 8f)
            arcTo(4f, 4f, 0f, true, true, 12f, 16f)
            arcTo(4f, 4f, 0f, false, true, 12f, 8f)
            close()
        }
        path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
            moveTo(18f, 5.5f)
            arcTo(0.5f, 0.5f, 0f, true, true, 17.5f, 5f)
            arcTo(0.5f, 0.5f, 0f, false, true, 18f, 5.5f)
        }
    }.build()
}

val TikTokLogoIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "TikTokLogoIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
            moveTo(12.5f, 2f)
            verticalLineTo(14f)
            arcTo(3.5f, 3.5f, 0f, false, true, 9f, 17.5f)
            arcTo(3.5f, 3.5f, 0f, false, true, 5.5f, 14f)
            arcTo(3.5f, 3.5f, 0f, false, true, 9f, 10.5f)
            verticalLineTo(12.5f)
            arcTo(1.5f, 1.5f, 0f, false, false, 7.5f, 14f)
            arcTo(1.5f, 1.5f, 0f, false, false, 9f, 15.5f)
            arcTo(1.5f, 1.5f, 0f, false, false, 10.5f, 14f)
            verticalLineTo(2f)
            horizontalLineTo(12.5f)
            moveTo(12.5f, 2f)
            arcTo(5f, 5f, 0f, false, false, 17.5f, 7f)
            verticalLineTo(5f)
            arcTo(3f, 3f, 0f, false, true, 12.5f, 2f)
            close()
        }
    }.build()
}

@Composable
fun CustomDarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, color = Color.Gray, fontSize = 14.sp) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF121212),
            unfocusedContainerColor = Color(0xFF121212),
            disabledContainerColor = Color(0xFF121212),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    showDialog: MutableState<Boolean>,
    initialName: String,
    initialBio: String?,
    initialProfilePicUrl: String?,
    initialCoverPhotoUrl: String?,
    initialLocation: String?,
    initialXHandle: String?,
    initialInstagramHandle: String?,
    initialTikTokHandle: String?,
    initialWebsite: String?,
    initialFanClubFlair: String?,
    userCoins: List<UserCoinModel>,
    onSaveClicked: (
        name: String,
        bio: String?,
        profilePicUrl: String?,
        coverPhotoUrl: String?,
        location: String?,
        xHandle: String?,
        instagramHandle: String?,
        tiktokHandle: String?,
        website: String?,
        fanClubFlair: String?
    ) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val apiClient = koinInject<ApiClient>()

    var name by remember(initialName) { mutableStateOf(initialName) }
    var bio by remember(initialBio) { mutableStateOf(initialBio ?: "") }
    var profilePicUrl by remember(initialProfilePicUrl) { mutableStateOf(initialProfilePicUrl ?: "") }
    var coverPhotoUrl by remember(initialCoverPhotoUrl) { mutableStateOf(initialCoverPhotoUrl ?: "") }
    var location by remember(initialLocation) { mutableStateOf(initialLocation ?: "") }
    var xHandle by remember(initialXHandle) { mutableStateOf(initialXHandle ?: "") }
    var instagramHandle by remember(initialInstagramHandle) { mutableStateOf(initialInstagramHandle ?: "") }
    var tiktokHandle by remember(initialTikTokHandle) { mutableStateOf(initialTikTokHandle ?: "") }
    var website by remember(initialWebsite) { mutableStateOf(initialWebsite ?: "") }
    var fanClubFlair by remember(initialFanClubFlair) { mutableStateOf(initialFanClubFlair ?: "None") }

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
            containerColor = Color(0xFFBA68C8), // Vibrant lavender/purple background
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
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
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { showSourceSelector = false }) {
                                Text("Back", color = Color.Black)
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
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text("Choose from Gallery", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Pick an image from your device storage", fontSize = 12.sp, color = Color.LightGray)
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
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wallpaper,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text("Search Preset Artworks", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Browse high-quality photos from Unsplash", fontSize = 12.sp, color = Color.LightGray)
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
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { 
                                showUnsplashSearch = false
                                showSourceSelector = true 
                            }) {
                                Text("Back", color = Color.Black)
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
                                color = Color.DarkGray
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
                                        color = Color.Black,
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
                                color = Color.DarkGray
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
                                CircularProgressIndicator(color = Color.Black)
                            }
                        } else {
                            if (unsplashPhotos.isEmpty()) {
                                Text(
                                    text = "No presets found. Try searching above.",
                                    color = Color.Black,
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
                                                    .background(Color.DarkGray)
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
                                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
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
                                    .background(Color.DarkGray)
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
                                    .border(3.dp, Color(0xFFBA68C8), CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray)
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

                        // Display Name
                        Text(
                            text = "Display Name",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                        )
                        CustomDarkTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Your name",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- ABOUT YOU SECTION ---
                        Text(
                            text = "About You",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                        )

                        // Description (Bio)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CustomDarkTextField(
                                value = bio,
                                onValueChange = { if (it.length <= 256) bio = it },
                                placeholder = "Description",
                                singleLine = false,
                                maxLines = 4,
                                modifier = Modifier.height(100.dp)
                            )
                            Text(
                                text = "${bio.length}/256",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Location
                        CustomDarkTextField(
                            value = location,
                            onValueChange = { location = it },
                            placeholder = "Location",
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = Color.Gray) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- SOCIAL HANDLES SECTION ---
                        Text(
                            text = "Social Handles",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                        )

                        // X Handle
                        CustomDarkTextField(
                            value = xHandle,
                            onValueChange = { xHandle = it },
                            placeholder = "X Handle",
                            singleLine = true,
                            leadingIcon = { Icon(XLogoIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Instagram Handle
                        CustomDarkTextField(
                            value = instagramHandle,
                            onValueChange = { instagramHandle = it },
                            placeholder = "Instagram Handle",
                            singleLine = true,
                            leadingIcon = { Icon(InstagramLogoIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // TikTok Handle
                        CustomDarkTextField(
                            value = tiktokHandle,
                            onValueChange = { tiktokHandle = it },
                            placeholder = "TikTok Handle",
                            singleLine = true,
                            leadingIcon = { Icon(TikTokLogoIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- FAN CLUB FLAIR SECTION ---
                        Text(
                            text = "Fan Club Flair",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                        )

                        // Dynamic Dropdown Box
                        var showFlairDropdown by remember { mutableStateOf(false) }
                        var selectedFlairText by remember(fanClubFlair) {
                            mutableStateOf(
                                when (fanClubFlair) {
                                    "Highest Balance" -> "Highest Balance"
                                    "None" -> "None"
                                    else -> userCoins.find { it.mint == fanClubFlair }?.ticker ?: fanClubFlair
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF121212))
                                .clickable { showFlairDropdown = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedFlairText,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = if (showFlairDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Dropdown",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = showFlairDropdown,
                                onDismissRequest = { showFlairDropdown = false },
                                modifier = Modifier.background(Color(0xFF1E1E1E))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None", color = Color.White) },
                                    onClick = {
                                        fanClubFlair = "None"
                                        selectedFlairText = "None"
                                        showFlairDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Highest Balance", color = Color.White) },
                                    onClick = {
                                        fanClubFlair = "Highest Balance"
                                        selectedFlairText = "Highest Balance"
                                        showFlairDropdown = false
                                    }
                                )
                                userCoins.forEach { coin ->
                                    DropdownMenuItem(
                                        text = { Text(coin.ticker, color = Color.White) },
                                        onClick = {
                                            fanClubFlair = coin.mint
                                            selectedFlairText = coin.ticker
                                            showFlairDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- WEBSITE SECTION ---
                        Text(
                            text = "Website",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                        )

                        // Website URL Input
                        CustomDarkTextField(
                            value = website,
                            onValueChange = { website = it },
                            placeholder = "Website",
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color.Gray) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
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
                                        coverPhotoUrl.ifBlank { null },
                                        location.ifBlank { null },
                                        xHandle.ifBlank { null },
                                        instagramHandle.ifBlank { null },
                                        tiktokHandle.ifBlank { null },
                                        website.ifBlank { null },
                                        fanClubFlair
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
                                            colors = listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
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
                                disabledContainerColor = Color.Black.copy(alpha = 0.2f)
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Save Changes",
                                fontWeight = FontWeight.Bold,
                                color = if (isEnabled) Color.White else Color.Black.copy(alpha = 0.4f)
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
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        )
    }
}
