package com.example.banglagan.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs") // টেবিলের নাম
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // গানের ইউনিক আইডি

    @ColumnInfo(name = "song_title")
    val title: String, // গানের শিরোনাম

    @ColumnInfo(name = "artist_name")
    val artistName: String? = null, // শিল্পীর নাম

    @ColumnInfo(name = "album_name")
    val albumName: String? = null, // অ্যালবামের নাম

    @ColumnInfo(name = "lyricist_name")
    val lyricist: String? = null, // গীতিকারের নাম

    @ColumnInfo(name = "composer_name")
    val composer: String? = null, // সুরকারের নাম

    @ColumnInfo(name = "era") // নতুন ফিল্ড: যুগ (যেমন: চর্যাপদ, মধ্যযুগ, আধুনিক)
    val era: String? = null,

    @ColumnInfo(name = "genre") // গানের ধরণ (যেমন: রবীন্দ্রসঙ্গীত, নজরুলগীতি, লোকগীতি)
    val genre: String? = null,

    @ColumnInfo(name = "release_year")
    val releaseYear: Int? = null, // প্রকাশের বছর

    @ColumnInfo(name = "lyrics")
    val lyrics: String? = null, // গানের কথা

    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false, // পছন্দের তালিকায় আছে কিনা

    @ColumnInfo(name = "audio_url") // নতুন ফিল্ড: অডিও লিঙ্ক (যদি থাকে)
    val audioUrl: String? = null,

    @ColumnInfo(name = "video_url") // নতুন ফিল্ড: ভিডিও লিঙ্ক (যদি থাকে)
    val videoUrl: String? = null,

    @ColumnInfo(name = "notes") // নতুন ফিল্ড: অতিরিক্ত তথ্য বা গানের প্রেক্ষাপট
    val notes: String? = null
)

