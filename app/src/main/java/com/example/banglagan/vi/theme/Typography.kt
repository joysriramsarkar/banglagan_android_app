package com.example.banglagan.vi.theme // আপনার প্যাকেজের নামের সাথে মিলিয়ে নিন

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ডিফল্ট টাইপোগ্রাফি, আপনি প্রয়োজন অনুযায়ী পরিবর্তন করতে পারেন
// যেমন: বিভিন্ন ফন্ট ফ্যামিলি বা ফন্ট স্টাইল যোগ করতে পারেন
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    // আপনি Material Design এর অন্যান্য টেক্সট স্টাইলগুলোও এখানে যোগ করতে পারেন
    // যেমন: displayLarge, headlineMedium, titleMedium, bodyMedium, labelMedium ইত্যাদি
)