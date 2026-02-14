package com.example.enge

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.enge.model.MaterialRepository
import com.example.enge.util.DebouncedTextWatcher
import com.example.enge.util.NumberFormatter
import android.widget.EditText
import kotlinx.coroutines.launch

class FixedFixedBeamActivity : ComponentActivity() {
    private val viewModel: FixedFixedBeamViewModel by viewModels()
    private val prefs by lazy { getSharedPreferences("fixed_fixed_form_state", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fixed_fixed_beam)

        val inputP = findViewById<EditText>(R.id.input_ffb_p)
        val inputL = findViewById<EditText>(R.id.input_ffb_l)
        val inputC = findViewById<EditText>(R.id.input_ffb_c)
        val inputI = findViewById<EditText>(R.id.input_ffb_i)
        val inputFs = findViewById<EditText>(R.id.input_ffb_fs)
        val inputFy = findViewById<EditText>(R.id.input_ffb_fy)
        val inputE = findViewById<EditText>(R.id.input_ffb_e)
        val inputMaterial = findViewById<AutoCompleteTextView>(R.id.input_ffb_material)
        val inputPreset = findViewById<CheckBox>(R.id.input_ffb_preset)

                                                        
        val outputDelta = findViewById<TextView>(R.id.output_ffb_delta)
        val outputDeltaAdm = findViewById<TextView>(R.id.output_ffb_delta_adm)
        val outputMmax = findViewById<TextView>(R.id.output_ffb_mmax)
        val outputSigma = findViewById<TextView>(R.id.output_ffb_sigma)
        val outputFyAdm = findViewById<TextView>(R.id.output_ffb_fy_adm)
        val outputCheckDeflection = findViewById<TextView>(R.id.output_ffb_check_deflection)
        val outputPercent = findViewById<TextView>(R.id.output_ffb_percentual)
        val outputCheckStress = findViewById<TextView>(R.id.output_ffb_check_stress)
        val outputFsObtido = findViewById<TextView>(R.id.output_ffb_fs_obtido)

        val materials = MaterialRepository.getMaterials(this)
        val materialNames = materials.map { it.name }
        val materialsByName = materials.associateBy { it.name }

        inputMaterial.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, materialNames))
        inputMaterial.setOnItemClickListener { _, _, position, _ ->
            val selected = materialNames.getOrNull(position) ?: return@setOnItemClickListener
            val material = materialsByName[selected] ?: return@setOnItemClickListener
            inputFy.setText(NumberFormatter.format(material.fyMpa))
            inputE.setText(NumberFormatter.format(material.eGpa))
            viewModel.updateInputs { it.copy(material = selected, fyMpa = material.fyMpa, eGpa = material.eGpa) }
        }

        fun applyPresetValues() {
            inputP.setText("1000")
            inputL.setText("2000")
            inputC.setText("50")
            inputI.setText("8000000")
            inputFs.setText("2")
        }

        val uncheckPreset = DebouncedTextWatcher {
            if (inputPreset.isChecked) inputPreset.isChecked = false
        }
        inputP.addTextChangedListener(uncheckPreset)
        inputL.addTextChangedListener(uncheckPreset)
        inputC.addTextChangedListener(uncheckPreset)
        inputI.addTextChangedListener(uncheckPreset)
        inputFs.addTextChangedListener(uncheckPreset)

        inputPreset.setOnCheckedChangeListener { _, checked -> if (checked) applyPresetValues() }

        inputP.addTextChangedListener(DebouncedTextWatcher { text -> viewModel.updateInputs { it.copy(pN = parseDouble(text)) } })
        inputL.addTextChangedListener(DebouncedTextWatcher { text -> viewModel.updateInputs { it.copy(lMm = parseDouble(text)) } })
        inputC.addTextChangedListener(DebouncedTextWatcher { text -> viewModel.updateInputs { it.copy(cMm = parseDouble(text)) } })
        inputI.addTextChangedListener(DebouncedTextWatcher { text -> viewModel.updateInputs { it.copy(iMm4 = parseDouble(text)) } })
        inputFs.addTextChangedListener(DebouncedTextWatcher { text -> viewModel.updateInputs { it.copy(fs = parseDouble(text)) } })
        inputFy.addTextChangedListener(DebouncedTextWatcher { text -> viewModel.updateInputs { it.copy(fyMpa = parseDouble(text)) } })
        inputE.addTextChangedListener(DebouncedTextWatcher { text -> viewModel.updateInputs { it.copy(eGpa = parseDouble(text)) } })

        loadState(inputP, inputL, inputC, inputI, inputFs, inputFy, inputE, inputMaterial, inputPreset)
        if (inputP.text.isNullOrBlank()) {
            inputPreset.isChecked = true
            applyPresetValues()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    inputP.error = state.errors.pN
                    inputL.error = state.errors.lMm
                    inputC.error = state.errors.cMm
                    inputI.error = state.errors.iMm4
                    inputFs.error = state.errors.fs
                    inputFy.error = state.errors.fyMpa
                    inputE.error = state.errors.eGpa

                    val output = state.output
                    outputDelta.text = output?.let { "${NumberFormatter.format(it.deltaMm)} mm" } ?: "—"
                    outputDeltaAdm.text = output?.let { "${NumberFormatter.format(it.deltaAdmMm)} mm" } ?: "—"
                    outputMmax.text = output?.let { "${NumberFormatter.format(it.mMaxNm)} N·m" } ?: "—"
                    outputSigma.text = output?.let { "${NumberFormatter.format(it.sigmaMpa)} MPa" } ?: "—"
                    outputFyAdm.text = output?.let { "${NumberFormatter.format(it.fyAdmMpa)} MPa" } ?: "—"
                    outputCheckDeflection.text = output?.let { if (it.checkDeflection) "OK" else "FALHOU" } ?: "—"
                    outputPercent.text = output?.let { "${NumberFormatter.format(it.percentualDelta)} %" } ?: "—"
                    outputCheckStress.text = output?.let { if (it.checkStress) "OK" else "FALHOU" } ?: "—"
                    outputFsObtido.text = output?.let { NumberFormatter.format(it.fsObtido) } ?: "—"
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveState(
            findViewById(R.id.input_ffb_p), findViewById(R.id.input_ffb_l), findViewById(R.id.input_ffb_c),
            findViewById(R.id.input_ffb_i), findViewById(R.id.input_ffb_fs), findViewById(R.id.input_ffb_fy),
            findViewById(R.id.input_ffb_e), findViewById(R.id.input_ffb_material), findViewById(R.id.input_ffb_preset)
        )
    }

    private fun saveState(
        p: EditText, l: EditText, c: EditText, i: EditText,
        fs: EditText, fy: EditText, e: EditText, material: AutoCompleteTextView,
        preset: CheckBox
    ) {
        prefs.edit()
            .putString("p", p.text?.toString())
            .putString("l", l.text?.toString())
            .putString("c", c.text?.toString())
            .putString("i", i.text?.toString())
            .putString("fs", fs.text?.toString())
            .putString("fy", fy.text?.toString())
            .putString("e", e.text?.toString())
            .putString("material", material.text?.toString())
            .putBoolean("preset", preset.isChecked)
            .apply()
    }

    private fun loadState(
        p: EditText, l: EditText, c: EditText, i: EditText,
        fs: EditText, fy: EditText, e: EditText, material: AutoCompleteTextView,
        preset: CheckBox
    ) {
        p.setText(prefs.getString("p", ""))
        l.setText(prefs.getString("l", ""))
        c.setText(prefs.getString("c", ""))
        i.setText(prefs.getString("i", ""))
        fs.setText(prefs.getString("fs", ""))
        fy.setText(prefs.getString("fy", ""))
        e.setText(prefs.getString("e", ""))
        material.setText(prefs.getString("material", ""), false)
        preset.isChecked = prefs.getBoolean("preset", false)
    }

    private fun parseDouble(value: String): Double? {
        val normalized = value.trim().replace(',', '.')
        return normalized.toDoubleOrNull()
    }
}
