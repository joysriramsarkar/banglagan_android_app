package com.example.banglagan.data // আপনার প্যাকেজের নামের সাথে মিলিয়ে নিন

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.banglagan.data.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    // সব গান পাওয়ার জন্য (গানের নাম অনুযায়ী সাজানো)
    // Flow ব্যবহার করা হয়েছে যাতে ডাটাবেসে কোনো পরিবর্তন হলে UI স্বয়ংক্রিয়ভাবে আপডেট হয়
    @Query("SELECT * FROM songs ORDER BY song_title ASC")
    fun getAllSongs(): Flow<List<Song>>

    // একটি নির্দিষ্ট গান তার আইডি দিয়ে পাওয়ার জন্য
    @Query("SELECT * FROM songs WHERE id = :songId")
    fun getSongById(songId: Int): Flow<Song?> // গানটি নাও থাকতে পারে, তাই Song? (nullable)

    // একটি নতুন গান যোগ করার জন্য
    // OnConflictStrategy.REPLACE মানে হলো যদি একই id-র গান আগে থেকে থাকে, নতুনটা পুরোনোটাকে রিপ্লেস করবে
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

    // গান সার্চ করার জন্য
    // এখানে গান, শিল্পী অথবা অ্যালবামের নামের অংশবিশেষ দিয়ে সার্চ করা যাবে
    // 'LOWER(:query)' ব্যবহার করা হয়েছে যাতে সার্চ কেস-ইনসেনসিটিভ হয় (ছোট/বড় হাতের অক্ষর যাই হোক না কেন)
    @Query("SELECT * FROM songs WHERE " +
            "LOWER(song_title) LIKE '%' || LOWER(:query) || '%' OR " +
            "LOWER(artist_name) LIKE '%' || LOWER(:query) || '%' OR " +
            "LOWER(album_name) LIKE '%' || LOWER(:query) || '%' " +
            "ORDER BY song_title ASC")
    fun searchSongs(query: String): Flow<List<Song>>

    // প্রিয় গানগুলো পাওয়ার জন্য
    @Query("SELECT * FROM songs WHERE is_favorite = 1 ORDER BY song_title ASC")
    fun getFavoriteSongs(): Flow<List<Song>>

    // শিল্পী অনুযায়ী গান ফিল্টার করার জন্য
    @Query("SELECT * FROM songs WHERE LOWER(artist_name) = LOWER(:artistName) ORDER BY song_title ASC")
    fun getSongsByArtist(artistName: String): Flow<List<Song>>

    // গানের ধরণ (Genre) অনুযায়ী ফিল্টার করার জন্য
    @Query("SELECT * FROM songs WHERE LOWER(genre) = LOWER(:genreName) ORDER BY song_title ASC")
    fun getSongsByGenre(genreName: String): Flow<List<Song>>

    // মোট গানের সংখ্যা পাওয়ার জন্য
    @Query("SELECT COUNT(*) FROM songs")
    fun getSongCount(): Flow<Int>

    // মোট স্বতন্ত্র শিল্পীর সংখ্যা পাওয়ার জন্য (ফাঁকা বা null বাদ দিয়ে)
    @Query("SELECT COUNT(DISTINCT artist_name) FROM songs WHERE artist_name IS NOT NULL AND artist_name != ''")
    fun getArtistCount(): Flow<Int>

    // মোট স্বতন্ত্র গীতিকারের সংখ্যা পাওয়ার জন্য (ফাঁকা বা null বাদ দিয়ে)
    @Query("SELECT COUNT(DISTINCT lyricist_name) FROM songs WHERE lyricist_name IS NOT NULL AND lyricist_name != ''")
    fun getLyricistCount(): Flow<Int>

    // মোট স্বতন্ত্র সুরকারের সংখ্যা পাওয়ার জন্য (ফাঁকা বা null বাদ দিয়ে)
    @Query("SELECT COUNT(DISTINCT composer_name) FROM songs WHERE composer_name IS NOT NULL AND composer_name != ''")
    fun getComposerCount(): Flow<Int>

    // সব স্বতন্ত্র শিল্পীর নাম পাওয়ার জন্য (সাজানো)
    @Query("SELECT DISTINCT artist_name FROM songs WHERE artist_name IS NOT NULL AND artist_name != '' ORDER BY artist_name ASC")
    fun getAllArtists(): Flow<List<String>>
}