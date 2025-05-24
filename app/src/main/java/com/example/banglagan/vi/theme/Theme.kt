package com.example.banglagan.vi.theme // আপনার প্যাকেজের নামের সাথে মিলিয়ে নিন

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// আপনার অ্যাপের জন্য ডার্ক থিমের কালার স্কিম
// এই কালারগুলো ui/theme/Color.kt ফাইলে ডিফাইন করা থাকবে
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
    // আপনি চাইলে এখানে আরও কালার (background, surface, error ইত্যাদি) ওভাররাইড করতে পারেন
    // উদাহরন:
    // background = Color(0xFF1C1B1F),
    // surface = Color(0xFF1C1B1F),
    // onPrimary = Color.Black,
    // onSecondary = Color.Black,
    // onTertiary = Color.Black,
    // onBackground = Color(0xFFE6E1E5),
    // onSurface = Color(0xFFE6E1E5),
)

// আপনার অ্যাপের জন্য লাইট থিমের কালার স্কিম
// এই কালারগুলো ui/theme/Color.kt ফাইলে ডিফাইন করা থাকবে
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    // আপনি চাইলে এখানে আরও কালার (background, surface, error ইত্যাদি) ওভাররাইড করতে পারেন
    // উদাহরন:
    // background = Color(0xFFFFFBFE),
    // surface = Color(0xFFFFFBFE),
    // onPrimary = Color.White,
    // onSecondary = Color.White,
    // onTertiary = Color.White,
    // onBackground = Color(0xFF1C1B1F),
    // onSurface = Color(0xFF1C1B1F),
)

@Composable
fun BanglaGanTheme( // এই নামটি MainActivity.kt তে ব্যবহৃত নামের সাথে মিলতে হবে
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic Color শুধুমাত্র Android 12 (API 31) এবং তার উপরের ভার্সনে কাজ করে
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) { // isInEditMode প্রিভিউতে true হয়, ডিভাইসে false
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // স্ট্যাটাস বারের কালার সেট করে
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme // স্ট্যাটাস বারের আইকন কালার (ডার্ক/লাইট)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Typography ui/theme/Typography.kt থেকে আসবে
        content = content
    )
}