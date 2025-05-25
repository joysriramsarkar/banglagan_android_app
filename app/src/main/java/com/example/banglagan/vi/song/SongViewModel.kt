package com.example.banglagan.vi.song // প্যাকেজের নাম ui দিয়ে হবে

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.banglagan.data.Song
import com.example.banglagan.data.SongRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine

// SongViewModel এর UI State ডাটা ক্লাস
data class SongUiState(
    val allSongs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalSongs: Int = 0, // নতুন: মোট গান
    val totalArtists: Int = 0, // নতুন: মোট শিল্পী
    val totalLyricists: Int = 0, // নতুন: মোট গীতিকার
    val totalComposers: Int = 0 // নতুন: মোট সুরকার
)

class SongViewModel(private val repository: SongRepository) : ViewModel() {

    // সব গানের তালিকা UI State হিসেবে
     val songUiState: StateFlow<SongUiState> =
        combine(
            repository.allSongs,
            repository.songCount,
            repository.artistCount,
            repository.lyricistCount,
            repository.composerCount
        ) { songs, songCount, artistCount, lyricistCount, composerCount ->
            // সব Flow থেকে পাওয়া ডেটা দিয়ে SongUiState তৈরি হচ্ছে
            SongUiState(
                allSongs = songs,
                isLoading = false,
                totalSongs = songCount,
                totalArtists = artistCount,
                totalLyricists = lyricistCount,
                totalComposers = composerCount
            )
        }

            .catch { exception -> emit(SongUiState(isLoading = false, errorMessage = exception.message)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = SongUiState(isLoading = true) // প্রাথমিক অবস্থায় লোডিং দেখাবে
            )

    // পছন্দের গানের তালিকা
    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // সব শিল্পীর তালিকা UI State হিসেবে
    val allArtists: StateFlow<List<String>> = repository.allArtists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // সার্চ কোয়েরি এবং ফলাফল
    private val _currentSearchQuery = MutableStateFlow("")
    val currentSearchQuery: StateFlow<String> = _currentSearchQuery.asStateFlow()

    val searchResults: StateFlow<List<Song>> = _currentSearchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchSongs(query)
            }
        }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // গান যোগ করার ফাংশন (Song.kt এর সব ফিল্ড সহ)
    fun addSong(title: String, artist: String, album: String?, lyricist: String?, composer: String?, era: String?, genre: String?, year: Int?, lyrics: String?, notes: String?, isFavorite: Boolean = false) {
        val newSong = Song(
            title = title,
            artistName = artist,
            albumName = album,
            lyricist = lyricist,
            composer = composer,
            era = era,
            genre = genre,
            releaseYear = year,
            lyrics = lyrics,
            notes = notes,
            isFavorite = isFavorite
        )
        viewModelScope.launch {
            repository.insertSong(newSong)
        }
    }

    // গান আপডেট করার ফাংশন
    fun toggleFavoriteStatus(song: Song) {
        val updatedSong = song.copy(isFavorite = !song.isFavorite)
        viewModelScope.launch {
            repository.updateSong(updatedSong)
        }
    }

    // একটি গান ডিলিট করার ফাংশন
    fun deleteSong(song: Song) {
        viewModelScope.launch {
            repository.deleteSong(song)
        }
    }

    // একটি নির্দিষ্ট গান আইডি দিয়ে পাওয়ার জন্য
    suspend fun getSongById(songId: Int): Song? {
        return repository.getSongById(songId).firstOrNull()
    }

    // সার্চ কোয়েরি আপডেট করার ফাংশন
    fun searchSongs(query: String) {
        _currentSearchQuery.value = query
    }
}

// ViewModel Factory
class SongViewModelFactory(private val repository: SongRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}