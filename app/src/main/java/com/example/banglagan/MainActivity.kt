@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.banglagan // আপনার প্যাকেজের নামের সাথে মিলিয়ে নিন

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.banglagan.data.Song // Song ডেটা ক্লাসের ইম্পোর্ট (সঠিক পাথ ব্যবহার করুন)
import com.example.banglagan.ui.song.SongUiState // SongUiState এর ইম্পোর্ট
import com.example.banglagan.ui.song.SongViewModel
import com.example.banglagan.ui.song.SongViewModelFactory
import com.example.banglagan.vi.theme.BanglaGanTheme // আপনার থিমের নামের সাথে মিলিয়ে নিন

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BanglaGanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // BanglaGanApplication থেকে repository অ্যাক্সেস করার চেষ্টা
                    // নিশ্চিত করুন আপনার একটি Application ক্লাস আছে এবং সেটি Manifest এ রেজিস্টার করা
                    val application = LocalContext.current.applicationContext as? BanglaGanApplication
                    if (application != null) {
                        val songViewModel: SongViewModel = viewModel(
                            factory = SongViewModelFactory(application.repository)
                        )
                        SongAppScreen(songViewModel = songViewModel)
                    } else {
                        // ফলব্যাক UI যদি Application ক্লাস কাস্ট করা না যায়
                        // এটি ডেভেলপমেন্টের সময় সমস্যা ডিবাগ করতে সাহায্য করবে
                        ErrorScreen("Application context could not be cast to BanglaGanApplication. Check your Application class and Manifest.")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongAppScreen(songViewModel: SongViewModel, modifier: Modifier = Modifier) {
    val songUiState by songViewModel.songUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("বাংলা গান") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (songUiState.isLoading && songUiState.allSongs.isEmpty()) {
            LoadingScreen(modifier = Modifier.padding(innerPadding))
        } else if (songUiState.allSongs.isEmpty() && !songUiState.isLoading) { // লোডিং শেষ এবং তালিকা খালি
            EmptySongListScreen(modifier = Modifier.padding(innerPadding))
        } else {
            SongList(
                songs = songUiState.allSongs,
                onFavoriteToggle = { Song -> songViewModel.toggleFavoriteStatus(Song) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text(
            text = "গান লোড হচ্ছে...",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun EmptySongListScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("কোনো গান পাওয়া যায়নি।", style = MaterialTheme.typography.headlineSmall)
        Text("কিছু গান যোগ করুন!", style = MaterialTheme.typography.bodyLarge)
        // TODO: গান যোগ করার জন্য একটি বাটন যোগ করা যেতে পারে
    }
}

@Composable
fun SongList(
    songs: List<Song>,
    onFavoriteToggle: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(songs, key = { song -> song.id }) { song ->
            SongItem(song = song, onFavoriteToggle = { onFavoriteToggle(song) })
        }
    }
}

@Composable
fun SongItem(song: Song, onFavoriteToggle: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxSize() // সম্ভবত .fillMaxWidth() হওয়া উচিত, পুরো স্ক্রিন নয়
            .padding(vertical = 4.dp), // আইটেমগুলোর মধ্যে একটু ফাঁকা জায়গা
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = song.title, style = MaterialTheme.typography.titleLarge)
            Text(text = "শিল্পী: ${song.artistName}", style = MaterialTheme.typography.bodyMedium)
            if (!song.albumName.isNullOrEmpty()) { // অ্যালবাম নাম থাকলে দেখাবে
                Text(text = "অ্যালবাম: ${song.albumName}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "ফেভারিট করুন",
                    tint = if (song.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("একটি সমস্যা হয়েছে:", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
        Text(message, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
    }
}

// প্রিভিউ ফাংশন (ডিফল্ট কিছু গান দিয়ে)
@Preview(showBackground = true)
@Composable
fun SongListPreview() {
    BanglaGanTheme {
        val previewSongs = listOf(
            Song(id = 1, title = "আমার সোনার বাংলা", artistName = "রবীন্দ্রনাথ ঠাকুর", isFavorite = true, albumName = "গীতবিতান", lyricist = "রবীন্দ্রনাথ ঠাকুর", composer = "রবীন্দ্রনাথ ঠাকুর", genre = "রবীন্দ্র", releaseYear = 1905, lyrics = "আমার সোনার বাংলা আমি তোমায় ভালবাসি"),
            Song(id = 2, title = "ধনধান্য পুষ্পভরা", artistName = "দ্বিজেন্দ্রলাল রায়", albumName = "Various", lyricist = "দ্বিজেন্দ্রলাল রায়", composer = "দ্বিজেন্দ্রলাল রায়", genre = "দেশাত্মবোধক", releaseYear = 1909, lyrics = "ধনধান্য পুষ্পভরা আমাদের এই বসুন্ধরা..."), // Added missing parameters
            Song(id = 3, title = "মোরা একটি ফুলকে বাঁচাবো বলে", artistName = "গোবিন্দ হালদার (কথা), আপেল মাহমুদ (সুর ও শিল্পী)", albumName = "Single", lyricist = "গোবিন্দ হালদার", composer = "আপেল মাহমুদ", genre = "মুক্তিযুদ্ধের গান", releaseYear = 1971, lyrics = "মোরা একটি ফুলকে বাঁচাবো বলে যুদ্ধ করি...") // Added missing parameters
        )

        // একটি ডামি UiState দিয়ে প্রিভিউ দেখানো হচ্ছে
        val uiState = SongUiState(allSongs = previewSongs, isLoading = false) // isLoading false করে দিন প্রিভিউতে

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("বাংলা গান (প্রিভিউ)") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            }
        ) { innerPadding ->
            if (uiState.isLoading && uiState.allSongs.isEmpty()) { // এই শর্ত প্রিভিউতে সাধারণত সত্য হবে না যদি isLoading false থাকে
                LoadingScreen(modifier = Modifier.padding(innerPadding))
            } else if (uiState.allSongs.isEmpty()) {
                EmptySongListScreen(modifier = Modifier.padding(innerPadding))
            } else {
                SongList(
                    songs = uiState.allSongs,
                    onFavoriteToggle = { /* প্রিভিউতে এই ফাংশনটি খালি থাকতে পারে */ },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}