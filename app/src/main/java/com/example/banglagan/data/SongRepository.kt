package com.example.banglagan.data

import kotlinx.coroutines.flow.Flow

// SongDao
class SongRepository(private val songDao: SongDao) {
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()

    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()

    // মোট গানের সংখ্যার Flow
    val songCount: Flow<Int> = songDao.getSongCount()

    // মোট শিল্পীর সংখ্যার Flow
    val artistCount: Flow<Int> = songDao.getArtistCount()

    // মোট গীতিকারের সংখ্যার Flow
    val lyricistCount: Flow<Int> = songDao.getLyricistCount()

    // মোট সুরকারের সংখ্যার Flow
    val composerCount: Flow<Int> = songDao.getComposerCount()

    val allArtists: Flow<List<String>> = songDao.getAllArtists()

    fun getSongById(songId: Int): Flow<Song?> {
        return songDao.getSongById(songId)

    }

    fun searchSongs(query: String): Flow<List<Song>> {
        if (query.isBlank()) {
            return allSongs
        }
        return songDao.searchSongs(query)
    }

    fun getSongsByArtist(artistName: String): Flow<List<Song>> {
        return songDao.getSongsByArtist(artistName)
    }

    fun getSongsByGenre(genreName: String): Flow<List<Song>> {
        return songDao.getSongsByGenre(genreName)
    }

    suspend fun insertSong(song: Song) {
        songDao.insertSong(song)
    }

    suspend fun insertMultipleSongs(songs: List<Song>) {
        songDao.insertAllSongs(songs)
    }

    suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }

    suspend fun deleteSong(song: Song) {
        songDao.deleteSong(song)
    }
}