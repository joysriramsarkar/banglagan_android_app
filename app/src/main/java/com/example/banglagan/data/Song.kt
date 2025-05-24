package com.example.banglagan.data // আপনার প্যাকেজের সঠিক নাম দিন

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs") // টেবিলের নাম
data class Song( // লক্ষ্য করুন, ক্লাসের নাম বড় হাতের 'S' দিয়ে Song
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "song_title")
    val title: String,

    @ColumnInfo(name = "artist_name")
    val artistName: String? =null,

    @ColumnInfo(name = "album_name")
    val albumName: String? = null,

    @ColumnInfo(name = "lyricist_name")
    val lyricist: String? = null,

    @ColumnInfo(name = "composer_name")
    val composer: String? = null,

    @ColumnInfo(name = "genre")
    val genre: String? = null,

    @ColumnInfo(name = "release_year")
    val releaseYear: Int? = null,

    @ColumnInfo(name = "lyrics")
    val lyrics: String? = null,

    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false
)