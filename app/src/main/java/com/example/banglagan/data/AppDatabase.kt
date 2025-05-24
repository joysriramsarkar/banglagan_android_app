package com.example.banglagan.data // আপনার প্যাকেজের নামের সাথে মিলিয়ে নিন

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
// import java.util.concurrent.Executors // প্রাথমিক ডাটা যোগ করার জন্য Executor এর পরিবর্তে Coroutine ব্যবহার করা হচ্ছে

@Database(entities = [Song::class], version = 1, exportSchema = false) // এন্টিটি এবং ভার্সন উল্লেখ করুন
abstract class AppDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao // আপনার DAO এখানে ডিক্লেয়ার করুন

    companion object {
        @Volatile // এই ভেরিয়েবলের মান সব থ্রেডে একই থাকবে
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope // Application স্কোপ থেকে CoroutineScope পাস করা ভালো
        ): AppDatabase {
            // যদি INSTANCE null না হয়, তাহলে সেটাই রিটার্ন করবে,
            // যদি null হয়, তাহলে ডাটাবেস তৈরি করবে
            return INSTANCE ?: synchronized(this) { // synchronized ব্লক থ্রেড-সেফ ভাবে ইনস্ট্যান্স তৈরি নিশ্চিত করে
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bangla_song_database" // আপনার ডাটাবেস ফাইলের নাম
                )
                    // .fallbackToDestructiveMigration() // মাইগ্রেশন স্ট্র্যাটেজি, ভার্সন বাড়লে আগের ডাটা মুছে নতুন করে তৈরি করবে
                    .addCallback(DatabaseCallback(scope, context)) // ডাটাবেস তৈরি হওয়ার পর প্রাথমিক ডাটা যোগ করার জন্য কলব্যাক
                    .build()
                INSTANCE = instance
                // instance রিটার্ন করবে
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope,
            private val context: Context // প্রাথমিক গান যোগ করার জন্য context লাগতে পারে (যেমন JSON থেকে পড়লে)
        ) : RoomDatabase.Callback() {
            // ডাটাবেস প্রথমবার তৈরি হওয়ার সময় এই মেথডটি কল হবে
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) { // IO থ্রেডে ডাটাবেস অপারেশন
                        populateInitialData(database.songDao())
                    }
                }
            }

            suspend fun populateInitialData(songDao: SongDao) {
                // এখানে অ্যাপটি প্রথমবার চালু হলে কিছু ডিফল্ট গান ডাটাবেসে যোগ করা যেতে পারে।
                // Song.kt তে নতুন ফিল্ড যোগ করা হয়েছে, তাই এখানেও সেগুলো যোগ করতে হবে।
                val initialSongs = listOf(
                    Song(title = "আমার সোনার বাংলা", artistName = "বিভিন্ন শিল্পী", era = "আধুনিক", genre = "দেশাত্মবোধক", lyrics = "আমার সোনার বাংলা, আমি তোমায় ভালোবাসি...", releaseYear = 1905, notes = "বাংলাদেশের জাতীয় সঙ্গীত।"),
                    Song(title = "ধনধান্য পুষ্পভরা", artistName = "দ্বিজেন্দ্রলাল রায়", era = "আধুনিক", genre = "দেশাত্মবোধক", releaseYear = 1909),
                    Song(title = "এক সাগর রক্তের বিনিময়ে", artistName = "স্বপ্না রায় (মূল শিল্পী গোবিন্দ হালদার)", era = "আধুনিক", genre = "দেশাত্মবোধক", albumName = "চলচ্চিত্র: ওরা ১১ জন", releaseYear = 1972),
                    Song(title = "সালাম সালাম হাজার সালাম", artistName = "আব্দুল জব্বার", era = "আধুনিক", genre = "দেশাত্মবোধক", releaseYear = 1971, notes = "ভাষা আন্দোলনের অমর গান।"),
                    Song(title = "মোরা একটি ফুলকে বাঁচাবো বলে যুদ্ধ করি", artistName = "আপেল মাহমুদ", era = "আধুনিক", genre = "দেশাত্মবোধক", releaseYear = 1971, notes = "মুক্তিযুদ্ধের গান।"),
                    // রবীন্দ্রসঙ্গীত
                    Song(title = "পুরানো সেই দিনের কথা", artistName = "রবীন্দ্রনাথ ঠাকুর", era = "আধুনিক", genre = "রবীন্দ্রসঙ্গীত"),
                    Song(title = "যদি তোর ডাক শুনে কেউ না আসে", artistName = "রবীন্দ্রনাথ ঠাকুর", era = "আধুনিক", genre = "রবীন্দ্রসঙ্গীত"),
                    // নজরুলগীতি
                    Song(title = "কারার ঐ লৌহকপাট", artistName = "কাজী নজরুল ইসলাম", era = "আধুনিক", genre = "নজরুলগীতি"),
                    Song(title = "চল্‌ চল্‌ চল্‌", artistName = "কাজী নজরুল ইসলাম", era = "আধুনিক", genre = "নজরুলগীতি", notes = "বাংলাদেশের রণসঙ্গীত।"),
                    // আধুনিক বাংলা গান
                    Song(title = "কফি হাউসের সেই আড্ডাটা", artistName = "মান্না দে", era = "আধুনিক", genre = "আধুনিক বাংলা", lyricist = "গৌরীপ্রসন্ন মজুমদার", composer = "সুপর্ণকান্তি ঘোষ", releaseYear = 1983),
                    Song(title = "এই পথ যদি না শেষ হয়", artistName = "হেমন্ত মুখোপাধ্যায়, সন্ধ্যা মুখোপাধ্যায়", era = "আধুনিক", genre = "সিনেমার গান", albumName = "সপ্তপদী", releaseYear = 1961),
                    Song(title = "আমি বাংলায় গান গাই", artistName = "প্রতুল মুখোপাধ্যায়", era = "আধুনিক", genre = "জীবনমুখী", releaseYear = 1992)
                    // আপনি চাইলে একটি JSON ফাইল থেকে পড়ে আরও অনেক গান যোগ করতে পারেন
                    // উদাহরণস্বরূপ, assets ফোল্ডারে একটি songs.json ফাইল রেখে সেখান থেকে ডেটা লোড করা যেতে পারে।
                )
                songDao.insertAllSongs(initialSongs)
            }
        }
    }
}                    .build()
                INSTANCE = instance
                // instance রিটার্ন করবে
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            // ডাটাবেস প্রথমবার তৈরি হওয়ার সময় এই মেথডটি কল হবে
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) { // IO থ্রেডে ডাটাবেস অপারেশন
                        populateInitialData(database.songDao())
                    }
                }
            }

            // ডাটাবেস খোলার সময় এই মেথডটি কল হবে (যদি প্রয়োজন হয়)
            // override fun onOpen(db: SupportSQLiteDatabase) {
            //     super.onOpen(db)
            //     // 필요하다면 여기서 추가 데이터 로드 또는 작업을 수행
            // }

            suspend fun populateInitialData(songDao: SongDao) {
                // এখানে অ্যাপটি প্রথমবার চালু হলে কিছু ডিফল্ট গান ডাটাবেসে যোগ করা যেতে পারে।
                // উদাহরণস্বরূপ:
                val initialSongs = listOf(
                    Song(title = "আমার সোনার বাংলা", artistName = "বিভিন্ন শিল্পী", genre = "দেশাত্মবোধক", lyrics = "আমার সোনার বাংলা, আমি তোমায় ভালোবাসি...", releaseYear = 1905),
                    Song(title = "ধনধান্য পুষ্পভরা", artistName = "দ্বিজেন্দ্রলাল রায়", genre = "দেশাত্মবোধক", releaseYear = 1909),
                    Song(title = "এক সাগর রক্তের বিনিময়ে", artistName = "স্বপ্না রায় (মূল শিল্পী গোবিন্দ হালদার)", genre = "দেশাত্মবোধক", albumName = "চলচ্চিত্র: ওরা ১১ জন", releaseYear = 1972),
                    Song(title = "সালাম সালাম হাজার সালাম", artistName = "আব্দুল জব্বার", genre = "দেশাত্মবোধক", releaseYear = 1971),
                    Song(title = "মোরা একটি ফুলকে বাঁচাবো বলে যুদ্ধ করি", artistName = "আপেল মাহমুদ", genre = "দেশাত্মবোধক", releaseYear = 1971),
                    // রবীন্দ্রসঙ্গীত
                    Song(title = "পুরানো সেই দিনের কথা", artistName = "রবীন্দ্রনাথ ঠাকুর", genre = "রবীন্দ্রসঙ্গীত"),
                    Song(title = "যদি তোর ডাক শুনে কেউ না আসে", artistName = "রবীন্দ্রনাথ ঠাকুর", genre = "রবীন্দ্রসঙ্গীত"),
                    // নজরুলগীতি
                    Song(title = "কারার ঐ লৌহকপাট", artistName = "কাজী নজরুল ইসলাম", genre = "নজরুলগীতি"),
                    Song(title = "চল্‌ চল্‌ চল্‌", artistName = "কাজী নজরুল ইসলাম", genre = "নজরুলগীতি"),
                    // আধুনিক বাংলা গান
                    Song(title = "কফি হাউসের সেই আড্ডাটা", artistName = "মান্না দে", genre = "আধুনিক বাংলা", lyricist = "গৌরীপ্রসন্ন মজুমদার", composer = "সুপর্ণকান্তি ঘোষ", releaseYear = 1983),
                    Song(title = "এই পথ যদি না শেষ হয়", artistName = "হেমন্ত মুখোপাধ্যায়, সন্ধ্যা মুখোপাধ্যায়", genre = "সিনেমার গান", albumName = "সপ্তপদী", releaseYear = 1961),
                    Song(title = "আমি বাংলায় গান গাই", artistName = "প্রতুল মুখোপাধ্যায়", genre = "জীবনমুখী", releaseYear = 1992)
                    // আপনি চাইলে একটি JSON ফাইল থেকে পড়ে আরও অনেক গান যোগ করতে পারেন
                )
                songDao.insertAllSongs(initialSongs)
            }
        }
    }
}
