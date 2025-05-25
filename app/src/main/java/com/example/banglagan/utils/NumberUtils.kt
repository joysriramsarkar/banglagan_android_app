package com.example.banglagan.utils // নিশ্চিত করুন প্যাকেজের নাম সঠিক

/**
 * Integer-কে বাংলা সংখ্যা সম্বলিত String-এ রূপান্তর করে।
 */
fun Int.toBanglaString(): String {
    val numberStr = this.toString()
    val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val builder = StringBuilder()

    for (char in numberStr) {
        if (char.isDigit()) {
            builder.append(banglaDigits[Character.getNumericValue(char)])
        } else {
            builder.append(char) // যদি সংখ্যা না হয় (যেমন '-' চিহ্ন), তবে সেটি অপরিবর্তিত থাকবে
        }
    }
    return builder.toString()
}

/**
 * String-এ থাকা ইংরেজি সংখ্যাকে বাংলা সংখ্যায় রূপান্তর করে।
 */
fun String.toBanglaString(): String {
    val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val builder = StringBuilder()

    for (char in this) {
        if (char.isDigit()) {
            builder.append(banglaDigits[Character.getNumericValue(char)])
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}