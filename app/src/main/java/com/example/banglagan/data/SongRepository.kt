package com.example.banglagan.data

import kotlinx.coroutines.flow.Flow

class SongRepository(private val songDao: SongDao) {

    // সরাসরি Flow হিসেবে ডেটা এক্সপোজ করা হচ্ছে
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()
    val songCount: Flow<Int> = songDao.getSongCount()
    val artistCount: Flow<Int> = songDao.getArtistCount()
    val lyricistCount: Flow<Int> = songDao.getLyricistCount()
    val composerCount: Flow<Int> = songDao.getComposerCount()
    val eraCount: Flow<Int> = songDao.getEraCount() // SongDao তে getEraCount() যোগ করা হয়েছে
    val genreCount: Flow<Int> = songDao.getGenreCount() // SongDao তে getGenreCount() যোগ করা হয়েছে

    // বিভিন্ন তালিকা পাওয়ার জন্য ফাংশন
    fun getAllArtists(): Flow<List<String>> = songDao.getAllArtists()
    fun getAllLyricists(): Flow<List<String>> = songDao.getAllLyricists()
    fun getAllComposers(): Flow<List<String>> = songDao.getAllComposers()
    fun getAllEras(): Flow<List<String>> = songDao.getAllEras()
    fun getAllGenres(): Flow<List<String>> = songDao.getAllGenres()

    // নির্দিষ্ট ক্যাটেগরি অনুযায়ী গান পাওয়ার ফাংশন
    fun getSongsByArtist(name: String): Flow<List<Song>> = songDao.getSongsByArtist(name)
    fun getSongsByLyricist(name: String): Flow<List<Song>> = songDao.getSongsByLyricist(name)
    fun getSongsByComposer(name: String): Flow<List<Song>> = songDao.getSongsByComposer(name)
    fun getSongsByEra(name: String): Flow<List<Song>> = songDao.getSongsByEra(name)
    fun getSongsByGenre(name: String): Flow<List<Song>> = songDao.getSongsByGenre(name)

    // সার্চ করার ফাংশন
    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)

    // একটি নির্দিষ্ট গান আইডি দিয়ে পাওয়ার ফাংশন
    fun getSongById(songId: Int): Flow<Song?> = songDao.getSongById(songId)

    // ডেটাবেসে গান যোগ, আপডেট ও ডিলিট করার সাসপেন্ড ফাংশন
    suspend fun insertSong(song: Song) {
        songDao.insertSong(song)
    }

    suspend fun insertMultipleSongs(songs: List<Song>) { // এই ফাংশনটি ViewModel এ ব্যবহার করা হয়নি, তবে রাখা হলো
        songDao.insertAllSongs(songs)
    }

    suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }

    suspend fun deleteSong(song: Song) {
        songDao.deleteSong(song)
    }
}
