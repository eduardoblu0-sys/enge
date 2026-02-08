package com.example.enge.util

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher

class DebouncedTextWatcher(
    private val delayMillis: Long = 300L,
    private val onDebouncedChange: (String) -> Unit
) : TextWatcher {
    private val handler = Handler(Looper.getMainLooper())
    private var lastRunnable: Runnable? = null

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        lastRunnable?.let(handler::removeCallbacks)
        val text = s?.toString().orEmpty()
        val runnable = Runnable { onDebouncedChange(text) }
        lastRunnable = runnable
        handler.postDelayed(runnable, delayMillis)
    }
}
