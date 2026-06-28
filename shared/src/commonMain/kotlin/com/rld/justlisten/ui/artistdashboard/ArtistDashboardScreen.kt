package com.rld.justlisten.ui.artistdashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.datalayer.models.MonthlyAggregatePlay
import com.rld.justlisten.viewmodel.screens.artistdashboard.ArtistDashboardState
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDashboardScreen(
    state: ArtistDashboardState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = "Artist Portal",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00B4DB))
                    }
                }
                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                else -> {
                    DashboardContent(state)
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(state: ArtistDashboardState) {
    // 1. Calculations
    val totalPlays = remember(state.monthlyListens) {
        state.monthlyListens.values.sumOf { it.totalListens }
    }
    
    val totalSales = remember(state.salesAggregate) {
        state.salesAggregate.sumOf { it.purchaseCount }
    }

    val trackPlays = remember(state.monthlyListens) {
        state.monthlyListens.values
            .flatMap { it.listenCounts }
            .groupBy { it.trackId }
            .mapValues { entry -> entry.value.sumOf { it.listens } }
            .toList()
            .sortedByDescending { it.second }
    }

    val activeTracksCount = remember(trackPlays) {
        trackPlays.size
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- SECTION: WELCOME & SUMMARY ---
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Welcome back, Creator",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Here is how your audio assets and catalog are performing.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // --- SECTION: GRID METRICS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Plays",
                        value = totalPlays.toString(),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        gradientColors = listOf(Color(0xFF6B11CB), Color(0xFF2575FC)),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Downloads",
                        value = state.downloadsCount.toString(),
                        icon = Icons.Default.GetApp,
                        gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "USDC Sales",
                        value = "${totalSales} items",
                        icon = Icons.Default.AttachMoney,
                        gradientColors = listOf(Color(0xFFFF8C00), Color(0xFFF12711)),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Active Tracks",
                        value = activeTracksCount.toString(),
                        icon = Icons.Default.MusicNote,
                        gradientColors = listOf(Color(0xFF00B4DB), Color(0xFF0083B0)),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- SECTION: TREND CHART ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly Listens Growth",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (state.monthlyListens.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No trend data available",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        MonthlyTrendChart(monthlyListens = state.monthlyListens)
                    }
                }
            }
        }

        // --- SECTION: TOP TRACKS HEADER ---
        item {
            Text(
                text = "Track Analytics Breakdown",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // --- SECTION: TRACK STATS LIST ---
        if (trackPlays.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No track data recorded yet.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            items(trackPlays) { (trackId, plays) ->
                TrackStatRow(trackId = trackId, plays = plays)
            }
        }

        // --- SECTION: SALES BREAKDOWN HEADER ---
        if (state.salesAggregate.isNotEmpty()) {
            item {
                Text(
                    text = "Sales Breakdown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(state.salesAggregate) { sale ->
                SalesStatRow(sale = sale)
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors = gradientColors))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MonthlyTrendChart(monthlyListens: Map<String, MonthlyAggregatePlay>) {
    val sortedData = remember(monthlyListens) {
        monthlyListens.toList().sortedBy { it.first } // Sorted chronologically
    }
    
    val maxListens = remember(sortedData) {
        val maxVal = sortedData.maxOfOrNull { it.second.totalListens } ?: 0
        if (maxVal == 0) 100 else maxVal
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val width = size.width
            val height = size.height
            
            // Draw horizontal grid lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height * (i.toFloat() / gridLines)
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (sortedData.size > 1) {
                val points = sortedData.mapIndexed { index, pair ->
                    val x = width * (index.toFloat() / (sortedData.size - 1))
                    val y = height - (height * (pair.second.totalListens.toFloat() / maxListens))
                    Offset(x, y)
                }

                // Draw filled area gradient
                val fillPath = Path().apply {
                    moveTo(points.first().x, height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF00B4DB).copy(alpha = 0.35f), Color.Transparent)
                    )
                )

                // Draw line
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(
                    path = strokePath,
                    color = Color(0xFF00E5FF),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw indicators on data points
                points.forEach { point ->
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = Color(0xFF00B4DB),
                        radius = 2.dp.toPx(),
                        center = point
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Month Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            sortedData.forEach { (month, data) ->
                val monthLabel = try {
                    val parts = month.split("-")
                    val monthNum = parts[1]
                    when (monthNum) {
                        "01" -> "Jan"
                        "02" -> "Feb"
                        "03" -> "Mar"
                        "04" -> "Apr"
                        "05" -> "May"
                        "06" -> "Jun"
                        "07" -> "Jul"
                        "08" -> "Aug"
                        "09" -> "Sep"
                        "10" -> "Oct"
                        "11" -> "Nov"
                        "12" -> "Dec"
                        else -> monthNum
                    }
                } catch (e: Exception) {
                    month
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = data.totalListens.toString(),
                        fontSize = 10.sp,
                        color = Color(0xFF00B4DB),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackStatRow(trackId: Int, plays: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF00B4DB).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFF00B4DB),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Track #$trackId",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "ID: $trackId",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$plays plays",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color(0xFF00B4DB)
                )
                Text(
                    text = "Aggregated listens",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun SalesStatRow(sale: com.rld.justlisten.datalayer.models.SalesAggregate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFF8C00).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = Color(0xFFFF8C00),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${sale.contentType.replaceFirstChar { it.uppercase() }} #${sale.contentId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Content Type: ${sale.contentType}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${sale.purchaseCount} purchases",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color(0xFFFF8C00)
                )
            }
        }
    }
}
