package com.example.enge.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberFormatter {
    private val symbols = DecimalFormatSymbols(Locale.US)

    fun format(value: Double, decimals: Int = 2): String {
        val safeValue = if (kotlin.math.abs(value) < 1e-9) 0.0 else value
        val pattern = if (decimals <= 0) "0" else "0." + "#".repeat(decimals)
        return DecimalFormat(pattern, symbols).format(safeValue)
    }
}
