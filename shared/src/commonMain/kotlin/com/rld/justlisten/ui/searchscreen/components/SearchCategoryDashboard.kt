package com.rld.justlisten.ui.searchscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun SearchCategoryDashboard(onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        "Electronic", "Rock", "Rap", "Alternative",
        "Experimental", "Punk", "Pop", "Folk",
        "Ambient", "Jazz", "Classical", "Country",
        "Kids", "Audiobooks"
    )

    val gradients = listOf(
        Brush.horizontalGradient(listOf(Color(0xFF8A2387), Color(0xFFE94057))),
        Brush.horizontalGradient(listOf(Color(0xFF00c6ff), Color(0xFF0072ff))),
        Brush.horizontalGradient(listOf(Color(0xFFfe8c00), Color(0xFFf83600))),
        Brush.horizontalGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d))),
        Brush.horizontalGradient(listOf(Color(0xFF159957), Color(0xFF155799))),
        Brush.horizontalGradient(listOf(Color(0xFF83a4d4), Color(0xFFb6fbff))),
        Brush.horizontalGradient(listOf(Color(0xFFff9966), Color(0xFFff5e62))),
        Brush.horizontalGradient(listOf(Color(0xFF4568dc), Color(0xFFb06ab3))),
        Brush.horizontalGradient(listOf(Color(0xFFEF3B36), Color(0xFF910A07))),
        Brush.horizontalGradient(listOf(Color(0xFF0052D4), Color(0xFF4364F7))),
        Brush.horizontalGradient(listOf(Color(0xFFFF007F), Color(0xFF7F00FF))),
        Brush.horizontalGradient(listOf(Color(0xFF3CA55C), Color(0xFFB5AC49))),
        Brush.horizontalGradient(listOf(Color(0xFFE55D87), Color(0xFF5FC3E4))),
        Brush.horizontalGradient(listOf(Color(0xFF1F1C2C), Color(0xFF928DAB)))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Browse Genres",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        for (i in categories.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CategoryCard(
                        name = categories[i],
                        gradient = gradients[i],
                        onClick = { onCategoryClick(categories[i]) }
                    )
                }
                if (i + 1 < categories.size) {
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryCard(
                            name = categories[i + 1],
                            gradient = gradients[i + 1],
                            onClick = { onCategoryClick(categories[i + 1]) }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    name: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}
