package com.example.banglagan.data // আপনার প্যাকেজের নামের সাথে মিলিয়ে নিন

import kotlinx.coroutines.flow.Flow // Flow ব্যবহার করার জন্য ইমপোর্ট

// SongDao এখানে constructor parameter হিসেবে পাস করা হচ্ছে
class SongRepository(private val songDao: SongDao) {

    // সব গান পাওয়ার জন্য Flow (DAO থেকে সরাসরি)
    // ViewModel এই Flow-কে observe করবে
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()

    // প্রিয় গানগুলো পাওয়ার জন্য Flow
    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()

    // একটি নির্দিষ্ট গান আইডি দিয়ে পাওয়ার জন্য
    fun getSongById(songId: Int): Flow<Song?> {
        return songDao.getSongById(songId)
    }

    // গান সার্চ করার জন্য
    fun searchSongs(query: String): Flow<List<Song>> {
        // যদি সার্চ স্ট্রিং খালি হয়, তাহলে সব গান দেখানো যেতে পারে
        // অথবা একটি খালি তালিকা রিটার্ন করা যেতে পারে, আপনার চাহিদার উপর নির্ভর করে
        if (query.isBlank()) {
            return allSongs // অথবা flowOf(emptyList())
        }
        return songDao.searchSongs(query)
    }

    // শিল্পী অনুযায়ী গান ফিল্টার করার জন্য
    fun getSongsByArtist(artistName: String): Flow<List<Song>> {
        return songDao.getSongsByArtist(artistName)
    }

    // গানের ধরণ (Genre) অনুযায়ী ফিল্টার করার জন্য
    fun getSongsByGenre(genreName: String): Flow<List<Song>> {
        return songDao.getSongsByGenre(genreName)
    }

    // একটি নতুন গান যোগ করার জন্য (suspend ফাংশন, কারণ DAO এর ফাংশনটি suspend)
    // এই ফাংশনগুলো ViewModel থেকে Coroutine এর মাধ্যমে কল করা হবে
    suspend fun insertSong(song: Song) {
        songDao.insertSong(song)
    }

    // একাধিক গান যোগ করার জন্য (যদি অ্যাপের বাইরে থেকে গান যোগ করার ফিচার থাকে)
    suspend fun insertMultipleSongs(songs: List<Song>) {
        songDao.insertAllSongs(songs)
    }

    // একটি গান আপডেট করার জন্য (যেমন, isFavorite স্ট্যাটাস পরিবর্তন করা)
    suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }

    // একটি গান ডিলিট করার জন্য (যদি এই ফিচার রাখতে চান)
    suspend fun deleteSong(song: Song) {
        songDao.deleteSong(song)
    }
}