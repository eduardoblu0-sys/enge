package com.example.enge.util

import java.util.Locale

object NumberFormatter {
    fun format(value: Double, decimals: Int = 4): String {
        return String.format(Locale.US, "%.${decimals}f", value)
    }
}
