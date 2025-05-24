package com.example.banglagan.ui.song // আপনার প্যাকেজের নামের সাথে মিলিয়ে নিন

import androidx.compose.animation.core.copy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.banglagan.data.Song
import com.example.banglagan.data.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// SongViewModel এর UI State ডাটা ক্লাস
// এখানে UI-তে দেখানোর জন্য প্রয়োজনীয় সব ডাটা থাকবে
data class SongUiState(
    val allSongs: List<Song> = emptyList(),
    val favoriteSongs: List<Song> = emptyList(),
    val searchResults: List<Song> = emptyList(),
    val currentSearchQuery: String = "",
    val isLoading: Boolean = false
    // আপনি চাইলে আরও স্টেট এখানে যোগ করতে পারেন, যেমন: error messages
)

class SongViewModel(private val repository: SongRepository) : ViewModel() {

    // সব গানের তালিকা UI State হিসেবে
    // .stateIn ব্যবহার করে Flow-কে StateFlow-তে রূপান্তর করা হয়েছে,
    // যা UI থেকে observe করার জন্য উপযুক্ত এবং সর্বশেষ ভ্যালু ধরে রাখে।
    val songUiState: StateFlow<SongUiState> =
        repository.allSongs.map { songs -> SongUiState(allSongs = songs) }
            .stateIn(
                scope = viewModelScope, // ViewModel-এর নিজস্ব CoroutineScope
                started = SharingStarted.WhileSubscribed(5000L), // UI সাবস্ক্রাইব থাকা পর্যন্ত একটিভ থাকবে (5 সেকেন্ড গ্রেস পিরিয়ড)
                initialValue = SongUiState(isLoading = true) // প্রাথমিক স্টেট, যখন ডাটা লোড হচ্ছে
            )

    // এখানে আপনি সার্চ, ফিল্টার, ফেভারিট ইত্যাদি অপারেশনের জন্য আরও StateFlow বা ফাংশন যোগ করতে পারেন
    // উদাহরণস্বরূপ, সার্চ করা গানগুলো দেখানোর জন্য:
    // private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    // val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    // গান যোগ করার ফাংশন
    fun addSong(title: String, artist: String, album: String?, genre: String?, year: Int?, lyrics: String?, isFavorite: Boolean = false) {
        val newSong = Song(
            title = title,
            artistName = artist,
            albumName = album,
            genre = genre,
            releaseYear = year,
            lyrics = lyrics,
            isFavorite = isFavorite
        )
        // ViewModelScope এ Coroutine লঞ্চ করে Repository এর মাধ্যমে গান যোগ করা হচ্ছে
        viewModelScope.launch {
            repository.insertSong(newSong)
        }
    }

    // গান আপডেট করার ফাংশন (যেমন isFavorite স্ট্যাটাস টগল করা)
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

    // TODO: সার্চ এবং ফিল্টারের জন্য ফাংশন যোগ করুন
    // fun searchSongs(query: String) { ... }
    // fun getFavoriteSongs() { ... } // অথবা একটি আলাদা StateFlow হিসেবে favoriteSongs পরিচালনা করুন
}

// ViewModel Factory তৈরি করা হচ্ছে, কারণ SongViewModel constructor-এ SongRepository প্যারামিটার নেয়
// এই Factory জানাবে কিভাবে SongViewModel তৈরি করতে হবে
class SongViewModelFactory(private val repository: SongRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}