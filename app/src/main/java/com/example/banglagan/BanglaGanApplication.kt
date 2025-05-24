package com.example.banglagan // আপনার প্রধান প্যাকেজের নামের সাথে মিলিয়ে নিন

import android.app.Application
import com.example.banglagan.data.AppDatabase // আপনার AppDatabase ক্লাসের পাথ
import com.example.banglagan.data.SongRepository // আপনার SongRepository ক্লাসের পাথ
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BanglaGanApplication : Application() {

    // অ্যাপ্লিকেশনের জন্য একটি CoroutineScope তৈরি করা হচ্ছে
    // SupervisorJob ব্যবহার করা হচ্ছে যাতে কোনো একটি চাইল্ড জব ফেইল করলে অন্যগুলো চলতে থাকে
    private val applicationScope = CoroutineScope(SupervisorJob())

    // ডাটাবেসের lazy ইনিশিয়ালাইজেশন। এটি প্রথমবার ব্যবহারের সময় তৈরি হবে।
    // 'applicationScope' পাস করা হচ্ছে DatabaseCallback এর জন্য
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this, applicationScope) }

    // রিপোজিটরির lazy ইনিশিয়ালাইজেশন। এটি database.songDao() তৈরি হওয়ার পর তৈরি হবে।
    val repository: SongRepository by lazy { SongRepository(database.songDao()) }

    // onCreate() মেথডটি অ্যাপ্লিকেশন তৈরি হওয়ার সময় একবার কল হয়
    override fun onCreate() {
        super.onCreate()
        // আপনি চাইলে এখানে অ্যাপ্লিকেশনের জন্য অন্যান্য প্রাথমিক কাজ করতে পারেন
        // যেমন: কোনো অ্যানালিটিক্স লাইব্রেরি ইনিশিয়ালাইজ করা
    }
}