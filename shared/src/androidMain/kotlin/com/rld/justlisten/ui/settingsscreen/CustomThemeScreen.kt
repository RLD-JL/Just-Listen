package com.rld.justlisten.ui.settingsscreen

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.viewmodel.screens.settings.SettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThemeScreen(
    settings: SettingsState,
    onBackPressed: () -> Unit,
    onCustomColorsApplied: (primary: String, secondary: String, background: String, surface: String) -> Unit,
    onPaletteSelected: (color: String) -> Unit
) {
    val scrollState = rememberScrollState()

    // Bespoke customizer local states (backed by loaded state or initial premium defaults)
    val customPrimary = remember { mutableStateOf(settings.customPrimary ?: "388E67") }
    val customSecondary = remember { mutableStateOf(settings.customSecondary ?: "50CDD7") }
    val customBackground = remember { mutableStateOf(settings.customBackground ?: "000000") }
    val customSurface = remember { mutableStateOf(settings.customSurface ?: "1D1D1D") }

    val hasDynamicThemeSupport = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // Preset options
    val premiumPresets = listOf(
        ThemePreset("Midnight Dark", "Dark", "ECE8E8", "cccccc", "000000", "272323"),
        ThemePreset("Emerald Breeze", "Green", "388E67", "50CDD7", "0A1C15", "112D21"),
        ThemePreset("Sapphire Glow", "Blue", "2750cc", "27a3cc", "0A1128", "121D3F"),
        ThemePreset("Volcano Red", "Orange", "FE3122", "effe22", "1F0705", "35110E"),
        ThemePreset("Orchid Sky", "Purple", "502da8", "eb881f", "0F0A1C", "1B1233"),
        ThemePreset("Rose Quartz", "Pink", "ff98a9", "ff98dd", "1C0E12", "301820")
    )

    // Palette Options for Interactive Click
    val primaryOptions = listOf("388E67", "2750cc", "FE3122", "502da8", "ff98a9", "FFAB00", "00E5FF", "E040FB")
    val secondaryOptions = listOf("50CDD7", "27a3cc", "effe22", "eb881f", "ff98dd", "FF6D00", "00B0FF", "FF4081")
    val backgroundOptions = listOf("000000", "121212", "0A1128", "0B2016", "0F0A1C", "1F1F1F", "263238")
    val surfaceOptions = listOf("1D1D1D", "272323", "1C1C1E", "121D3F", "112D21", "1B1233", "37474F")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Theme Customizer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Live Real-Time Miniature UI Preview
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LIVE THEME PREVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                // Miniature screen mockups to preview colors
                MiniPreviewCard(
                    settings = settings,
                    primaryHex = customPrimary.value,
                    secondaryHex = customSecondary.value,
                    backgroundHex = customBackground.value,
                    surfaceHex = customSurface.value
                )
            }

            // Mode Selector: Expressive vs Preset vs Bespoke
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "THEME CONFIGURATION MODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )

                if (hasDynamicThemeSupport) {
                    // Expressive Design Card (Android dynamic wallpaper)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (settings.palletColor == "Expressive") 2.dp else 0.dp,
                                color = if (settings.palletColor == "Expressive") MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onPaletteSelected("Expressive") },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Expressive Design",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Following dynamic system wallpaper palette",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            RadioButton(
                                selected = settings.palletColor == "Expressive",
                                onClick = { onPaletteSelected("Expressive") }
                            )
                        }
                    }
                }

                // Bespoke Customizer Mode trigger
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (settings.palletColor == "Custom") 2.dp else 0.dp,
                            color = if (settings.palletColor == "Custom") MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            onCustomColorsApplied(
                                customPrimary.value,
                                customSecondary.value,
                                customBackground.value,
                                customSurface.value
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Brush,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bespoke Customizer",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Fully customize primary, secondary, background, and surface colors",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        RadioButton(
                            selected = settings.palletColor == "Custom",
                            onClick = {
                                onCustomColorsApplied(
                                    customPrimary.value,
                                    customSecondary.value,
                                    customBackground.value,
                                    customSurface.value
                                )
                            }
                        )
                    }
                }
            }

            // Standard Presets Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "PRESET PALETTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )

                // Grid layout for standard presets
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    premiumPresets.chunked(2).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunk.forEach { preset ->
                                val isSelected = settings.palletColor == preset.id
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            onPaletteSelected(preset.id)
                                            // sync bespoke values to this preset so bespoke customizer starts from here
                                            customPrimary.value = preset.primaryHex
                                            customSecondary.value = preset.secondaryHex
                                            customBackground.value = preset.backgroundHex
                                            customSurface.value = preset.surfaceHex
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = preset.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            ColorCircle(hex = preset.primaryHex, size = 16)
                                            ColorCircle(hex = preset.secondaryHex, size = 16)
                                            ColorCircle(hex = preset.backgroundHex, size = 16)
                                            ColorCircle(hex = preset.surfaceHex, size = 16)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Detailed Bespoke Color Adjuster
            AnimatedVisibility(
                visible = settings.palletColor == "Custom" || settings.palletColor != "Expressive",
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "COLOR OVERRIDES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // 1. Primary Color
                        ColorPickerSection(
                            title = "Primary Accent",
                            subtitle = "Buttons, active markers, brand color",
                            selectedHex = customPrimary.value,
                            options = primaryOptions,
                            onColorSelected = {
                                customPrimary.value = it
                                onCustomColorsApplied(it, customSecondary.value, customBackground.value, customSurface.value)
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // 2. Secondary Color
                        ColorPickerSection(
                            title = "Secondary Accent",
                            subtitle = "Complementary badges and highlights",
                            selectedHex = customSecondary.value,
                            options = secondaryOptions,
                            onColorSelected = {
                                customSecondary.value = it
                                onCustomColorsApplied(customPrimary.value, it, customBackground.value, customSurface.value)
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // 3. Background Color
                        ColorPickerSection(
                            title = "Base Background",
                            subtitle = "Overall layout backing (e.g. OLED pure black)",
                            selectedHex = customBackground.value,
                            options = backgroundOptions,
                            onColorSelected = {
                                customBackground.value = it
                                onCustomColorsApplied(customPrimary.value, customSecondary.value, it, customSurface.value)
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // 4. Surface Color
                        ColorPickerSection(
                            title = "Card Surface",
                            subtitle = "Floating sheets, dialogs, item cards",
                            selectedHex = customSurface.value,
                            options = surfaceOptions,
                            onColorSelected = {
                                customSurface.value = it
                                onCustomColorsApplied(customPrimary.value, customSecondary.value, customBackground.value, it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerSection(
    title: String,
    subtitle: String,
    selectedHex: String,
    options: List<String>,
    onColorSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    "#${selectedHex.uppercase()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { hex ->
                val isSelected = selectedHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(parseComposeColor(hex))
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(hex) }
                )
            }
        }
    }
}

@Composable
fun MiniPreviewCard(
    settings: SettingsState,
    primaryHex: String,
    secondaryHex: String,
    backgroundHex: String,
    surfaceHex: String
) {
    val prim = parseComposeColor(primaryHex)
    val sec = parseComposeColor(secondaryHex)
    val bg = parseComposeColor(backgroundHex)
    val surf = parseComposeColor(surfaceHex)

    val textColor = if (bg.luminance() > 0.5f) Color.Black else Color.White
    val textOnPrimary = if (prim.luminance() > 0.5f) Color.Black else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mock Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(prim))
                    Text("JustListen Mock Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                }
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = prim,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Mock Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = surf),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Stellar Tune", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Text("Mocking Artist", fontSize = 8.sp, color = textColor.copy(alpha = 0.6f))
                    }
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(60.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(prim),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PLAY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = textOnPrimary)
                    }
                }
            }

            // Mock Bottom Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                repeat(4) { idx ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (idx == 0) prim else textColor.copy(alpha = 0.15f))
                    )
                }
            }
        }
    }
}

@Composable
fun ColorCircle(hex: String, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(parseComposeColor(hex))
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
    )
}

fun parseComposeColor(hex: String): Color {
    val cleaned = hex.trim().replace("#", "")
    return try {
        val longVal = cleaned.toLong(16)
        if (cleaned.length == 6) {
            Color(longVal or 0xFF000000)
        } else {
            Color(longVal)
        }
    } catch (_: Exception) {
        Color.DarkGray
    }
}

data class ThemePreset(
    val name: String,
    val id: String,
    val primaryHex: String,
    val secondaryHex: String,
    val backgroundHex: String,
    val surfaceHex: String
)
