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
        val materialsItem = findViewById<LinearLayout>(R.id.menu_materials_item)
        val fixedFixedBeamItem = findViewById<LinearLayout>(R.id.menu_fixed_fixed_beam_item)
        val ucIconsItem = findViewById<LinearLayout>(R.id.menu_uc_icons_item)
        val torsionItem = findViewById<LinearLayout>(R.id.menu_torsion_item)
        fixedFixedBeamItem.setOnClickListener {
            startActivity(Intent(this, FixedFixedBeamActivity::class.java))
        }

        bucklingItem.setOnClickListener {
            startActivity(Intent(this, BucklingActivity::class.java))
        }

        materialsItem.setOnClickListener {
            startActivity(Intent(this, MaterialManagementActivity::class.java))
        }

        ucIconsItem.setOnClickListener {
            startActivity(Intent(this, UcMenuActivity::class.java))
        }

        torsionItem.setOnClickListener {
            startActivity(Intent(this, TorsionShaftCalculatorActivity::class.java))
        }
    }
}
