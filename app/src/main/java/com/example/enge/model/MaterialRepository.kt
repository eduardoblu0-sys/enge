package com.example.enge.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class MaterialData(
    val name: String,
    val fyMpa: Double,
    val eGpa: Double
)

object MaterialRepository {
    private const val PREFS_NAME = "enge_materials_prefs"
    private const val KEY_MATERIALS = "materials"

    private val defaultMaterials = listOf(
        MaterialData(name = "SAE 1045 Trefilado", fyMpa = 360.0, eGpa = 200.0),
        MaterialData(name = "SAE 1020 Laminado", fyMpa = 350.0, eGpa = 200.0),
        MaterialData(name = "SAE 1045", fyMpa = 530.0, eGpa = 200.0),
        MaterialData(name = "Inox 304", fyMpa = 215.0, eGpa = 193.0),
        MaterialData(name = "Alumínio 6061", fyMpa = 275.0, eGpa = 69.0),
        MaterialData(name = "Plástico ABS", fyMpa = 40.0, eGpa = 2.1)
    )

    fun getMaterials(context: Context): List<MaterialData> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_MATERIALS, null) ?: return defaultMaterials
        return parseMaterials(raw).ifEmpty { defaultMaterials }
    }

    fun addMaterial(context: Context, material: MaterialData) {
        val materials = getMaterials(context).toMutableList()
        materials.add(material)
        saveMaterials(context, materials)
    }

    fun updateMaterial(context: Context, oldName: String, updated: MaterialData) {
        val materials = getMaterials(context).toMutableList()
        val index = materials.indexOfFirst { it.name == oldName }
        if (index >= 0) {
            materials[index] = updated
            saveMaterials(context, materials)
        }
    }

    fun deleteMaterial(context: Context, name: String) {
        val materials = getMaterials(context).filterNot { it.name == name }
        saveMaterials(context, materials)
    }

    fun findByName(context: Context, name: String): MaterialData? {
        return getMaterials(context).firstOrNull { it.name == name }
    }

    private fun saveMaterials(context: Context, materials: List<MaterialData>) {
        val json = JSONArray()
        materials.forEach {
            json.put(
                JSONObject().apply {
                    put("name", it.name)
                    put("fyMpa", it.fyMpa)
                    put("eGpa", it.eGpa)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MATERIALS, json.toString())
            .apply()
    }

    private fun parseMaterials(raw: String): List<MaterialData> {
        return runCatching {
            val json = JSONArray(raw)
            (0 until json.length()).mapNotNull { index ->
                val item = json.optJSONObject(index) ?: return@mapNotNull null
                val name = item.optString("name")
                val fy = item.optDouble("fyMpa", Double.NaN)
                val e = item.optDouble("eGpa", Double.NaN)
                if (name.isBlank() || fy.isNaN() || e.isNaN()) {
                    null
                } else {
                    MaterialData(name = name, fyMpa = fy, eGpa = e)
                }
            }
        }.getOrDefault(emptyList())
    }
}
