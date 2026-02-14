package com.example.enge

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.enge.model.ForceUnit
import com.example.enge.model.LengthUnit
import com.example.enge.model.MaterialRepository
import com.example.enge.util.DebouncedTextWatcher
import com.example.enge.util.NumberFormatter
import kotlinx.coroutines.launch
import java.util.Locale

class FixedFixedBeamActivity : ComponentActivity() {
    private val viewModel: FixedFixedBeamViewModel by viewModels()
    private val prefs by lazy { getSharedPreferences("fixed_fixed_form_state", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fixed_fixed_beam)

        val inputP = findViewById<EditText>(R.id.input_ffb_p)
        val inputPUnit = findViewById<Spinner>(R.id.input_ffb_p_unit)
        val inputL = findViewById<EditText>(R.id.input_ffb_l)
        val inputLUnit = findViewById<Spinner>(R.id.input_ffb_l_unit)
        val inputC = findViewById<EditText>(R.id.input_ffb_c)
        val inputI = findViewById<EditText>(R.id.input_ffb_i)
        val inputFs = findViewById<EditText>(R.id.input_ffb_fs)
        val inputFy = findViewById<EditText>(R.id.input_ffb_fy)
        val inputE = findViewById<EditText>(R.id.input_ffb_e)
        val inputMaterial = findViewById<Spinner>(R.id.input_ffb_material)
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

        inputMaterial.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, materialNames)

        val presetFs = 1.5
        var isApplyingPreset = false

        fun applyPresetValues() {
            isApplyingPreset = true
            inputFs.setText(NumberFormatter.format(presetFs))
            isApplyingPreset = false
        }

        fun parseDouble(text: String?): Double? = text?.trim()?.replace(',', '.')?.toDoubleOrNull()

        fun shouldUncheckPreset(): Boolean {
            val fs = parseDouble(inputFs.text?.toString())
            return fs != presetFs
        }

        fun convertForce(value: Double, from: ForceUnit, to: ForceUnit): Double {
            val inN = when (from) {
                ForceUnit.N -> value
                ForceUnit.KN -> value * 1_000.0
                ForceUnit.KGF -> value * 9.80665
            }
            return when (to) {
                ForceUnit.N -> inN
                ForceUnit.KN -> inN / 1_000.0
                ForceUnit.KGF -> inN / 9.80665
            }
        }

        fun convertLength(value: Double, from: LengthUnit, to: LengthUnit): Double {
            val inMm = when (from) {
                LengthUnit.MM -> value
                LengthUnit.M -> value * 1_000.0
            }
            return when (to) {
                LengthUnit.MM -> inMm
                LengthUnit.M -> inMm / 1_000.0
            }
        }

        fun parseForceUnit(text: String?): ForceUnit? = when (text?.trim()?.lowercase(Locale.getDefault())) {
            "n" -> ForceUnit.N
            "kn" -> ForceUnit.KN
            "kg" -> ForceUnit.KGF
            else -> null
        }

        fun parseLengthUnit(text: String?): LengthUnit? = when (text?.trim()?.lowercase(Locale.getDefault())) {
            "mm" -> LengthUnit.MM
            "m" -> LengthUnit.M
            else -> null
        }

        fun updateViewModelInputs() {
            val pValue = parseDouble(inputP.text?.toString())
            val lValue = parseDouble(inputL.text?.toString())
            val cValue = parseDouble(inputC.text?.toString())
            val iValue = parseDouble(inputI.text?.toString())
            val fsValue = parseDouble(inputFs.text?.toString())
            val fyValue = parseDouble(inputFy.text?.toString())
            val eValue = parseDouble(inputE.text?.toString())

            val pUnit = parseForceUnit(inputPUnit.selectedItem?.toString()) ?: ForceUnit.N
            val lUnit = parseLengthUnit(inputLUnit.selectedItem?.toString()) ?: LengthUnit.MM

            viewModel.updateInputs {
                it.copy(
                    p = pValue,
                    pUnit = pUnit,
                    l = lValue,
                    lUnit = lUnit,
                    cMm = cValue,
                    iMm4 = iValue,
                    fs = fsValue,
                    fyMpa = fyValue,
                    eGpa = eValue,
                    material = inputMaterial.selectedItem?.toString()
                )
            }
        }

        var isConvertingUnit = false
        var lastForceUnit = ForceUnit.N
        var lastLengthUnit = LengthUnit.MM

        val uncheckPreset = DebouncedTextWatcher {
            if (!isApplyingPreset && inputPreset.isChecked && shouldUncheckPreset()) {
                inputPreset.isChecked = false
            }
        }
        inputFs.addTextChangedListener(uncheckPreset)

        inputPreset.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                applyPresetValues()
                updateViewModelInputs()
            }
        }

        inputP.addTextChangedListener(DebouncedTextWatcher { if (!isConvertingUnit) updateViewModelInputs() })
        inputL.addTextChangedListener(DebouncedTextWatcher { if (!isConvertingUnit) updateViewModelInputs() })
        inputC.addTextChangedListener(DebouncedTextWatcher { updateViewModelInputs() })
        inputI.addTextChangedListener(DebouncedTextWatcher { updateViewModelInputs() })
        inputFs.addTextChangedListener(DebouncedTextWatcher { updateViewModelInputs() })
        inputFy.addTextChangedListener(DebouncedTextWatcher { updateViewModelInputs() })
        inputE.addTextChangedListener(DebouncedTextWatcher { updateViewModelInputs() })

        inputPUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            private var initialized = false
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedUnit = parseForceUnit(parent?.getItemAtPosition(position)?.toString()) ?: return
                if (!initialized) {
                    lastForceUnit = selectedUnit
                    initialized = true
                    updateViewModelInputs()
                    return
                }
                if (selectedUnit == lastForceUnit) return

                val currentValue = parseDouble(inputP.text?.toString())
                if (currentValue != null) {
                    isConvertingUnit = true
                    inputP.setText(NumberFormatter.format(convertForce(currentValue, lastForceUnit, selectedUnit)))
                    isConvertingUnit = false
                }
                lastForceUnit = selectedUnit
                updateViewModelInputs()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        inputLUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            private var initialized = false
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedUnit = parseLengthUnit(parent?.getItemAtPosition(position)?.toString()) ?: return
                if (!initialized) {
                    lastLengthUnit = selectedUnit
                    initialized = true
                    updateViewModelInputs()
                    return
                }
                if (selectedUnit == lastLengthUnit) return

                val currentValue = parseDouble(inputL.text?.toString())
                if (currentValue != null) {
                    isConvertingUnit = true
                    inputL.setText(NumberFormatter.format(convertLength(currentValue, lastLengthUnit, selectedUnit)))
                    isConvertingUnit = false
                }
                lastLengthUnit = selectedUnit
                updateViewModelInputs()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        inputMaterial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = parent?.getItemAtPosition(position)?.toString() ?: return
                val material = materialsByName[selected] ?: return
                inputFy.setText(NumberFormatter.format(material.fyMpa))
                inputE.setText(NumberFormatter.format(material.eGpa))
                updateViewModelInputs()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        loadState(inputP, inputPUnit, inputL, inputLUnit, inputC, inputI, inputFs, inputFy, inputE, inputMaterial, inputPreset, materialNames)
        if (inputFs.text.isNullOrBlank()) {
            inputPreset.isChecked = true
            applyPresetValues()
        }
        updateViewModelInputs()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    inputP.error = state.errors.p
                    inputL.error = state.errors.l
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
                    applyValidationStyle(outputCheckDeflection, outputCheckDeflection.text?.toString())
                    outputPercent.text = output?.let { "${NumberFormatter.format(it.percentualDelta)} %" } ?: "—"
                    outputCheckStress.text = output?.let { if (it.checkStress) "OK" else "FALHOU" } ?: "—"
                    applyValidationStyle(outputCheckStress, outputCheckStress.text?.toString())
                    outputFsObtido.text = output?.let { NumberFormatter.format(it.fsObtido) } ?: "—"
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveState(
            findViewById(R.id.input_ffb_p), findViewById(R.id.input_ffb_p_unit),
            findViewById(R.id.input_ffb_l), findViewById(R.id.input_ffb_l_unit), findViewById(R.id.input_ffb_c),
            findViewById(R.id.input_ffb_i), findViewById(R.id.input_ffb_fs), findViewById(R.id.input_ffb_fy),
            findViewById(R.id.input_ffb_e), findViewById(R.id.input_ffb_material), findViewById(R.id.input_ffb_preset)
        )
    }

    private fun saveState(
        p: EditText, pUnit: Spinner, l: EditText, lUnit: Spinner, c: EditText, i: EditText,
        fs: EditText, fy: EditText, e: EditText, material: Spinner,
        preset: CheckBox
    ) {
        prefs.edit()
            .putString("p", p.text?.toString())
            .putInt("pUnit", pUnit.selectedItemPosition)
            .putString("l", l.text?.toString())
            .putInt("lUnit", lUnit.selectedItemPosition)
            .putString("c", c.text?.toString())
            .putString("i", i.text?.toString())
            .putString("fs", fs.text?.toString())
            .putString("fy", fy.text?.toString())
            .putString("e", e.text?.toString())
            .putInt("material", material.selectedItemPosition)
            .putBoolean("preset", preset.isChecked)
            .apply()
    }

    private fun loadState(
        p: EditText, pUnit: Spinner, l: EditText, lUnit: Spinner, c: EditText, i: EditText,
        fs: EditText, fy: EditText, e: EditText, material: Spinner,
        preset: CheckBox,
        materialNames: List<String>
    ) {
        p.setText(prefs.getString("p", ""))
        pUnit.setSelection(prefs.getInt("pUnit", 0))
        l.setText(prefs.getString("l", ""))
        lUnit.setSelection(prefs.getInt("lUnit", 0))
        c.setText(prefs.getString("c", ""))
        i.setText(prefs.getString("i", ""))
        fs.setText(prefs.getString("fs", ""))
        fy.setText(prefs.getString("fy", ""))
        e.setText(prefs.getString("e", ""))

        val materialSelection = try {
            prefs.getInt("material", 0)
        } catch (_: ClassCastException) {
            val previousMaterialName = prefs.getString("material", "").orEmpty()
            materialNames.indexOf(previousMaterialName).takeIf { it >= 0 } ?: 0
        }
        material.setSelection(materialSelection)

        preset.isChecked = prefs.getBoolean("preset", false)
    }

    private fun applyValidationStyle(view: TextView, text: String?) {
        val normalized = text?.lowercase(Locale.getDefault()).orEmpty()
        val isFailure = normalized.contains("falhou") || normalized.contains("não ok") || normalized.contains("nao ok")
        view.setTypeface(null, if (isFailure) Typeface.BOLD else Typeface.NORMAL)
        view.setTextColor(if (isFailure) Color.RED else Color.BLACK)
    }
}
