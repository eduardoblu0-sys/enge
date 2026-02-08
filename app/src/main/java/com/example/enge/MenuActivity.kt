package com.example.enge

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val beamDeflectionItem = findViewById<LinearLayout>(R.id.menu_beam_deflection_item)
        beamDeflectionItem.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val bucklingItem = findViewById<LinearLayout>(R.id.menu_buckling_item)
        bucklingItem.setOnClickListener {
            startActivity(Intent(this, BucklingActivity::class.java))
        }
    }
}
