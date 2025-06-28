package com.example.banglagan.vi.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.banglagan.vi.song.SongViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongScreen(
    songViewModel: SongViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    var albumName by remember { mutableStateOf("") }
    var lyricist by remember { mutableStateOf("") }
    var composer by remember { mutableStateOf("") }
    var era by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var releaseYear by remember { mutableStateOf("") }
    var lyrics by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var audioUrl by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("নতুন গান যোগ করুন") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("গানের নাম *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = artistName,
                onValueChange = { artistName = it },
                label = { Text("শিল্পীর নাম") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = albumName,
                onValueChange = { albumName = it },
                label = { Text("অ্যালবামের নাম") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lyricist,
                onValueChange = { lyricist = it },
                label = { Text("গীতিকার") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = composer,
                onValueChange = { composer = it },
                label = { Text("সুরকার") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = era,
                onValueChange = { era = it },
                label = { Text("যুগ") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = genre,
                onValueChange = { genre = it },
                label = { Text("ধরণ") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = releaseYear,
                onValueChange = { releaseYear = it },
                label = { Text("প্রকাশের বছর") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = audioUrl,
                onValueChange = { audioUrl = it },
                label = { Text("অডিও লিঙ্ক") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = videoUrl,
                onValueChange = { videoUrl = it },
                label = { Text("ভিডিও লিঙ্ক") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lyrics,
                onValueChange = { lyrics = it },
                label = { Text("গানের কথা") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("বিশেষ তথ্য") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        songViewModel.addSong(
                            title = title,
                            artistName = artistName.takeIf { it.isNotBlank() },
                            albumName = albumName.takeIf { it.isNotBlank() },
                            lyricist = lyricist.takeIf { it.isNotBlank() },
                            composer = composer.takeIf { it.isNotBlank() },
                            era = era.takeIf { it.isNotBlank() },
                            genre = genre.takeIf { it.isNotBlank() },
                            releaseYear = releaseYear.toIntOrNull(),
                            lyrics = lyrics.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() },
                            audioUrl = audioUrl.takeIf { it.isNotBlank() },
                            videoUrl = videoUrl.takeIf { it.isNotBlank() }
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("গান যোগ করুন")
            }
        }
    }
}