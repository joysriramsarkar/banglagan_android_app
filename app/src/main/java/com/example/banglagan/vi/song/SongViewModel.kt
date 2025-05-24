package com.example.banglagan.ui.song // আপনার প্যাকেজের নামের সাথে মিলিয়ে নিন

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.banglagan.data.Song
import com.example.banglagan.data.SongRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// SongViewModel এর UI State ডাটা ক্লাস
data class SongUiState(
    val allSongs: List<Song> = emptyList(), // সব গানের তালিকা
    // val favoriteSongs: List<Song> = emptyList(), // পছন্দের গানের তালিকা (আলাদা StateFlow তে সরানো হয়েছে)
    // val searchResults: List<Song> = emptyList(), // সার্চের ফলাফল (আলাদা StateFlow তে সরানো হয়েছে)
    // val currentSearchQuery: String = "", // বর্তমান সার্চ কোয়েরি (আলাদা StateFlow তে সরানো হয়েছে)
    val isLoading: Boolean = false, // ডেটা লোড হচ্ছে কিনা
    val errorMessage: String? = null // কোনো সমস্যা হলে তার বার্তা
)

class SongViewModel(private val repository: SongRepository) : ViewModel() {

    // সব গানের তালিকা UI State হিসেবে
    val songUiState: StateFlow<SongUiState> =
        repository.allSongs
            .map { songs -> SongUiState(allSongs = songs, isLoading = false) } // গান পেলে isLoading false
            .catch { exception -> emit(SongUiState(isLoading = false, errorMessage = exception.message)) } // কোনো সমস্যা হলে
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = SongUiState(isLoading = true) // প্রাথমিক স্টেট, যখন ডেটা লোড হচ্ছে
            )

    // পছন্দের গানের তালিকা
    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // সার্চ কোয়েরি এবং ফলাফল
    private val _currentSearchQuery = MutableStateFlow("")
    val currentSearchQuery: StateFlow<String> = _currentSearchQuery.asStateFlow()

    // flatMapLatest ব্যবহার করে সার্চ কোয়েরি পরিবর্তন হলে নতুন করে সার্চ করা হবে
    val searchResults: StateFlow<List<Song>> = _currentSearchQuery
        .debounce(300) // ব্যবহারকারী টাইপ করা থামা পর্যন্ত অপেক্ষা (300ms)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList()) // কোয়েরি খালি থাকলে খালি তালিকা
            } else {
                repository.searchSongs(query) // Repositoty থেকে সার্চ করা
            }
        }
        .catch { emit(emptyList()) } // কোনো সমস্যা হলে খালি তালিকা
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubsubscribed(5000L),
            initialValue = emptyList()
        )


    // গান যোগ করার ফাংশন
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

    // একটি নির্দিষ্ট গান আইডি দিয়ে পাওয়ার জন্য (UI থেকে ব্যবহারের জন্য StateFlow তে রাখা ভালো)
    // এই ফাংশনটি সরাসরি UI থেকে কল না করে, NavHost এ produceState ব্যবহার করা হয়েছে।
    suspend fun getSongById(songId: Int): Song? {
        return repository.getSongById(songId).firstOrNull() // Flow থেকে প্রথম ভ্যালু নেয়
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
