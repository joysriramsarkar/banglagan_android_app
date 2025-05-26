package com.example.banglagan.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    // সব গান পাওয়ার জন্য (গানের নাম অনুযায়ী সাজানো)
    @Query("SELECT * FROM songs ORDER BY song_title ASC")
    fun getAllSongs(): Flow<List<Song>>

    // একটি নির্দিষ্ট গান তার আইডি দিয়ে পাওয়ার জন্য
    @Query("SELECT * FROM songs WHERE id = :songId")
    fun getSongById(songId: Int): Flow<Song?>

    // একটি নতুন গান যোগ করার জন্য
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    // একাধিক গান একসাথে যোগ করার জন্য
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSongs(songs: List<Song>)

    // একটি বিদ্যমান গান আপডেট করার জন্য
    @Update
    suspend fun updateSong(song: Song)

    // একটি গান ডিলিট করার জন্য
    @Delete
    suspend fun deleteSong(song: Song)

    // গান সার্চ করার জন্য (গীতিকার ও সুরকার সহ)
    @Query("SELECT * FROM songs WHERE " +
            "LOWER(song_title) LIKE '%' || LOWER(:query) || '%' OR " +
            "LOWER(artist_name) LIKE '%' || LOWER(:query) || '%' OR " +
            "LOWER(album_name) LIKE '%' || LOWER(:query) || '%' OR " +
            "LOWER(lyricist_name) LIKE '%' || LOWER(:query) || '%' OR " +
            "LOWER(composer_name) LIKE '%' || LOWER(:query) || '%' OR " +
            "LOWER(genre) LIKE '%' || LOWER(:query) || '%' OR " + // <-- গানের ধরণ যোগ করা হলো
            "LOWER(era) LIKE '%' || LOWER(:query) || '%' " +      // <-- গানের যুগ যোগ করা হলো
            "ORDER BY song_title ASC")
    fun searchSongs(query: String): Flow<List<Song>>

    // প্রিয় গানগুলো পাওয়ার জন্য
    @Query("SELECT * FROM songs WHERE is_favorite = 1 ORDER BY song_title ASC")
    fun getFavoriteSongs(): Flow<List<Song>>

    // শিল্পী অনুযায়ী গান ফিল্টার করার জন্য
    @Query("SELECT * FROM songs WHERE LOWER(artist_name) = LOWER(:artistName) ORDER BY song_title ASC")
    fun getSongsByArtist(artistName: String): Flow<List<Song>>

    // গীতিকার অনুযায়ী গান ফিল্টার করার জন্য
    @Query("SELECT * FROM songs WHERE LOWER(lyricist_name) = LOWER(:lyricistName) ORDER BY song_title ASC")
    fun getSongsByLyricist(lyricistName: String): Flow<List<Song>>

    // সুরকার অনুযায়ী গান ফিল্টার করার জন্য
    @Query("SELECT * FROM songs WHERE LOWER(composer_name) = LOWER(:composerName) ORDER BY song_title ASC")
    fun getSongsByComposer(composerName: String): Flow<List<Song>>

    // গানের ধরণ (Genre) অনুযায়ী ফিল্টার করার জন্য
    @Query("SELECT * FROM songs WHERE LOWER(genre) = LOWER(:genreName) ORDER BY song_title ASC")
    fun getSongsByGenre(genreName: String): Flow<List<Song>>

    // গানের যুগ (Era) অনুযায়ী ফিল্টার করার জন্য
    @Query("SELECT * FROM songs WHERE LOWER(era) = LOWER(:eraName) ORDER BY song_title ASC")
    fun getSongsByEra(eraName: String): Flow<List<Song>>

    // মোট গানের সংখ্যা পাওয়ার জন্য
    @Query("SELECT COUNT(*) FROM songs")
    fun getSongCount(): Flow<Int>

    // মোট স্বতন্ত্র শিল্পীর সংখ্যা পাওয়ার জন্য
    @Query("SELECT COUNT(DISTINCT artist_name) FROM songs WHERE artist_name IS NOT NULL AND artist_name != ''")
    fun getArtistCount(): Flow<Int>

    // মোট স্বতন্ত্র গীতিকারের সংখ্যা পাওয়ার জন্য
    @Query("SELECT COUNT(DISTINCT lyricist_name) FROM songs WHERE lyricist_name IS NOT NULL AND lyricist_name != ''")
    fun getLyricistCount(): Flow<Int>

    // মোট স্বতন্ত্র সুরকারের সংখ্যা পাওয়ার জন্য
    @Query("SELECT COUNT(DISTINCT composer_name) FROM songs WHERE composer_name IS NOT NULL AND composer_name != ''")
    fun getComposerCount(): Flow<Int>

    // সব স্বতন্ত্র শিল্পীর নাম পাওয়ার জন্য (সাজানো)
    @Query("SELECT DISTINCT artist_name FROM songs WHERE artist_name IS NOT NULL AND artist_name != '' ORDER BY artist_name ASC")
    fun getAllArtists(): Flow<List<String>>

    // সব স্বতন্ত্র গীতিকারের নাম পাওয়ার জন্য (সাজানো)
    @Query("SELECT DISTINCT lyricist_name FROM songs WHERE lyricist_name IS NOT NULL AND lyricist_name != '' ORDER BY lyricist_name ASC")
    fun getAllLyricists(): Flow<List<String>>

    // সব স্বতন্ত্র সুরকারের নাম পাওয়ার জন্য (সাজানো)
    @Query("SELECT DISTINCT composer_name FROM songs WHERE composer_name IS NOT NULL AND composer_name != '' ORDER BY composer_name ASC")
    fun getAllComposers(): Flow<List<String>>

    // সব স্বতন্ত্র গানের ধরণ পাওয়ার জন্য (সাজানো)
    @Query("SELECT DISTINCT genre FROM songs WHERE genre IS NOT NULL AND genre != '' ORDER BY genre ASC")
    fun getAllGenres(): Flow<List<String>>

    // সব স্বতন্ত্র গানের যুগ পাওয়ার জন্য (সাজানো)
    @Query("SELECT DISTINCT era FROM songs WHERE era IS NOT NULL AND era != '' ORDER BY era ASC")
    fun getAllEras(): Flow<List<String>>

    @Query("SELECT COUNT(DISTINCT era) FROM songs WHERE era IS NOT NULL AND era != ''")
    fun getEraCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT genre) FROM songs WHERE genre IS NOT NULL AND genre != ''")
    fun getGenreCount(): Flow<Int>

}