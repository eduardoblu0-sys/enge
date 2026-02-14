package com.example.enge

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.app.AlertDialog
import com.example.enge.model.MaterialData
import com.example.enge.model.MaterialRepository
import com.example.enge.util.NumberFormatter

class MaterialManagementActivity : ComponentActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var materials: List<MaterialData> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_management)

        listView = findViewById(R.id.material_list)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, mutableListOf())
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        listView.adapter = adapter

        findViewById<Button>(R.id.button_add_material).setOnClickListener {
            showMaterialDialog(title = "Adicionar material") { material ->
                MaterialRepository.addMaterial(this, material)
                refreshMaterials()
            }
        }

        findViewById<Button>(R.id.button_edit_material).setOnClickListener {
            val selected = getSelectedMaterial() ?: return@setOnClickListener
            showMaterialDialog(title = "Editar material", existing = selected) { updated ->
                MaterialRepository.updateMaterial(this, selected.name, updated)
                refreshMaterials()
            }
        }

        findViewById<Button>(R.id.button_delete_material).setOnClickListener {
            val selected = getSelectedMaterial() ?: return@setOnClickListener
            MaterialRepository.deleteMaterial(this, selected.name)
            refreshMaterials()
        }

        refreshMaterials()
    }

    private fun refreshMaterials() {
        materials = MaterialRepository.getMaterials(this)
        adapter.clear()
        adapter.addAll(materials.map { formatLine(it) })
        adapter.notifyDataSetChanged()
        listView.clearChoices()
    }

    private fun getSelectedMaterial(): MaterialData? {
        val checkedPosition = listView.checkedItemPosition
        if (checkedPosition == ListView.INVALID_POSITION) {
            Toast.makeText(this, "Selecione um material.", Toast.LENGTH_SHORT).show()
            return null
        }
        return materials.getOrNull(checkedPosition)
    }

    private fun showMaterialDialog(
        title: String,
        existing: MaterialData? = null,
        onConfirm: (MaterialData) -> Unit
    ) {
        val content = layoutInflater.inflate(R.layout.dialog_material_form, null)
        val inputName = content.findViewById<EditText>(R.id.input_material_name)
        val inputFy = content.findViewById<EditText>(R.id.input_material_fy)
        val inputE = content.findViewById<EditText>(R.id.input_material_e)

        existing?.let {
            inputName.setText(it.name)
            inputFy.setText(NumberFormatter.format(it.fyMpa))
            inputE.setText(NumberFormatter.format(it.eGpa))
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(content)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar") { _, _ ->
                val name = inputName.text.toString().trim()
                val fy = inputFy.text.toString().trim().replace(',', '.').toDoubleOrNull()
                val e = inputE.text.toString().trim().replace(',', '.').toDoubleOrNull()

                if (name.isBlank() || fy == null || e == null || fy <= 0.0 || e <= 0.0) {
                    Toast.makeText(
                        this,
                        "Preencha nome, fy e E com valores maiores que zero.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                onConfirm(MaterialData(name = name, fyMpa = fy, eGpa = e))
            }
            .show()
    }

    private fun formatLine(material: MaterialData): String {
        return "${material.name}  |  fy=${NumberFormatter.format(material.fyMpa)} MPa  |  E=${NumberFormatter.format(material.eGpa)} GPa"
    }
}
