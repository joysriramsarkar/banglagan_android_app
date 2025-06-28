package com.example.banglagan.vi.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.banglagan.utils.toBanglaString
import com.example.banglagan.vi.song.SongUiState
import com.example.banglagan.vi.song.SongViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    songViewModel: SongViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by songViewModel.songUiState.collectAsState()
    val artists by songViewModel.artists.collectAsState()
    val lyricists by songViewModel.lyricists.collectAsState()
    val composers by songViewModel.composers.collectAsState()
    val eras by songViewModel.eras.collectAsState()
    val genres by songViewModel.genres.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("পরিসংখ্যান") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "সংগ্রহের পরিসংখ্যান",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            item {
                StatsOverviewCard(uiState = uiState)
            }

            item {
                Text(
                    "শীর্ষ শিল্পীগণ",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(artists.take(10)) { artist ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = artist,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                Text(
                    "যুগভিত্তিক বিভাজন",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(eras) { era ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = era,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                Text(
                    "ধরণভিত্তিক বিভাজন",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(genres) { genre ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = genre,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun StatsOverviewCard(uiState: SongUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "সংগ্রহের সারসংক্ষেপ",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("মোট গান", uiState.totalSongs)
                StatItem("মোট শিল্পী", uiState.totalArtists)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("মোট গীতিকার", uiState.totalLyricists)
                StatItem("মোট সুরকার", uiState.totalComposers)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("মোট যুগ", uiState.totalEras)
                StatItem("মোট ধরণ", uiState.totalGenres)
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toBanglaString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}