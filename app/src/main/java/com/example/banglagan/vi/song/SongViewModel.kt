// SongViewModel.kt

package com.example.banglagan.vi.song

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.banglagan.data.Song
import com.example.banglagan.data.SongRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SongUiState(
    val allSongs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalSongs: Int = 0,
    val totalArtists: Int = 0,
    val totalLyricists: Int = 0,
    val totalComposers: Int = 0,
    val totalEras: Int = 0,
    val totalGenres: Int = 0
)

class SongViewModel(private val repository: SongRepository) : ViewModel() {

    // songUiState তৈরি করার জন্য combine ফাংশনের সঠিক ব্যবহার
    val songUiState: StateFlow<SongUiState> =
        combine(
            repository.allSongs,        // Flow<List<Song>>
            repository.songCount,       // Flow<Int>
            repository.artistCount,     // Flow<Int>
            repository.lyricistCount,   // Flow<Int>
            repository.composerCount,   // Flow<Int>
            repository.eraCount,        // Flow<Int> - এটি repository তে থাকতে হবে
            repository.genreCount       // Flow<Int> - এটি repository তে থাকতে হবে
        ) { tộiPhạm -> // tộiPhạm একটি Array<Any> হিসেবে আসছে, এখানে প্রতিটি Flow এর ভ্যালু থাকবে
            // combine থেকে আসা ভ্যালুগুলো সঠিকভাবে access করতে হবে
            val songs = tộiPhạm[0] as List<Song>
            val songCount = tộiPhạm[1] as Int
            val artistCount = tộiPhạm[2] as Int
            val lyricistCount = tộiPhạm[3] as Int
            val composerCount = tộiPhạm[4] as Int
            val eraCount = tộiPhạm[5] as Int
            val genreCount = tộiPhạm[6] as Int

            SongUiState(
                allSongs = songs,
                isLoading = false,
                totalSongs = songCount,
                totalArtists = artistCount,
                totalLyricists = lyricistCount,
                totalComposers = composerCount,
                totalEras = eraCount,
                totalGenres = genreCount
            )
        }
            .onStart { emit(SongUiState(isLoading = true)) }
            .catch { exception ->
                emit(SongUiState(isLoading = false, errorMessage = exception.message))
                Log.e("SongViewModel", "Error in songUiState flow: ${exception.message}", exception)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = SongUiState(isLoading = true)
            )

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val artists: StateFlow<List<String>> = repository.getAllArtists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val lyricists: StateFlow<List<String>> = repository.getAllLyricists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val composers: StateFlow<List<String>> = repository.getAllComposers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val eras: StateFlow<List<String>> = repository.getAllEras()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val genres: StateFlow<List<String>> = repository.getAllGenres()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    private val _currentSearchQuery = MutableStateFlow("")
    val currentSearchQuery: StateFlow<String> = _currentSearchQuery.asStateFlow() // এটি SearchScreen এ ব্যবহৃত হতে পারে

    val searchResults: StateFlow<List<Song>> = _currentSearchQuery
        .debounce(300)
        .distinctUntilChanged()
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

    private val _selectedCategoryType = MutableStateFlow<String?>(null)
    private val _selectedCategoryName = MutableStateFlow<String?>(null)

    val songsBySelectedCategory: StateFlow<List<Song>> =
        combine(_selectedCategoryType, _selectedCategoryName) { type, name ->
            if (type != null && name != null) {
                when (type) {
                    "artist" -> repository.getSongsByArtist(name).firstOrNull() ?: emptyList()
                    "lyricist" -> repository.getSongsByLyricist(name).firstOrNull() ?: emptyList()
                    "composer" -> repository.getSongsByComposer(name).firstOrNull() ?: emptyList()
                    "era" -> repository.getSongsByEra(name).firstOrNull() ?: emptyList()
                    "genre" -> repository.getSongsByGenre(name).firstOrNull() ?: emptyList()
                    else -> emptyList()
                }
            } else {
                emptyList()
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun addSong(
        title: String,
        artistName: String?,
        albumName: String?,
        lyricist: String?,
        composer: String?,
        era: String?,
        genre: String?,
        releaseYear: Int?,
        lyrics: String?,
        notes: String?,
        audioUrl: String?,
        videoUrl: String?,
        isFavorite: Boolean = false
    ) {
        val newSong = Song(
            title = title,
            artistName = artistName,
            albumName = albumName,
            lyricist = lyricist,
            composer = composer,
            era = era,
            genre = genre,
            releaseYear = releaseYear,
            lyrics = lyrics,
            notes = notes,
            audioUrl = audioUrl,
            videoUrl = videoUrl,
            isFavorite = isFavorite
        )
        viewModelScope.launch {
            repository.insertSong(newSong)
        }
    }

    fun toggleFavoriteStatus(song: Song) {
        val updatedSong = song.copy(isFavorite = !song.isFavorite)
        viewModelScope.launch {
            repository.updateSong(updatedSong)
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            repository.deleteSong(song)
        }
    }

    suspend fun getSongById(songId: Int): Song? {
        return repository.getSongById(songId).firstOrNull()
    }

    fun searchSongs(query: String) {
        _currentSearchQuery.value = query
    }

    fun loadSongsForCategory(categoryType: String, categoryName: String) {
        _selectedCategoryType.value = categoryType
        _selectedCategoryName.value = categoryName
    }
}

class SongViewModelFactory(private val repository: SongRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
