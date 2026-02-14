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
import com.example.enge.model.BeamCalculator
import com.example.enge.model.ForceUnit
import com.example.enge.model.InertiaUnit
import com.example.enge.model.InputDataRaw
import com.example.enge.model.LengthUnit
import com.example.enge.model.MaterialRepository
import com.example.enge.model.ModulusUnit
import com.example.enge.model.OutputDataUi
import com.example.enge.model.StressUnit
import com.example.enge.util.DebouncedTextWatcher
import com.example.enge.util.NumberFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("main_form_state", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputLoadValue = findViewById<EditText>(R.id.input_load_value)
        val inputLoadUnit = findViewById<Spinner>(R.id.input_load_unit)
        val inputLengthValue = findViewById<EditText>(R.id.input_length_value)
        val inputLengthUnit = findViewById<Spinner>(R.id.input_length_unit)
        val inputNeutralDistanceValue = findViewById<EditText>(R.id.input_neutral_distance_value)
        val inputInertiaValue = findViewById<EditText>(R.id.input_inertia_value)
        val inputYieldValue = findViewById<EditText>(R.id.input_yield_value)
        val inputYieldUnit = findViewById<Spinner>(R.id.input_yield_unit)
        val inputModulusValue = findViewById<EditText>(R.id.input_modulus_value)
        val inputModulusUnit = findViewById<Spinner>(R.id.input_modulus_unit)
        val inputFsAdmValue = findViewById<EditText>(R.id.input_fs_adm_value)
        val inputMaterial = findViewById<Spinner>(R.id.input_material)
        val inputPreset = findViewById<CheckBox>(R.id.input_main_preset)

        val outputDeltaObtValue = findViewById<TextView>(R.id.output_delta_obt_value)
        val outputDeltaAdmValue = findViewById<TextView>(R.id.output_delta_adm_value)
        val outputMmaxValue = findViewById<TextView>(R.id.output_mmax_value)
        val outputSigmaValue = findViewById<TextView>(R.id.output_sigma_value)
        val outputFyAdmValue = findViewById<TextView>(R.id.output_fy_adm_value)
        val outputStatusDeflectionValue = findViewById<TextView>(R.id.output_status_deflection_value)
        val outputPercentualDeltaValue = findViewById<TextView>(R.id.output_percentual_delta_value)
        val outputStatusStressValue = findViewById<TextView>(R.id.output_status_stress_value)
        val outputFsObtValue = findViewById<TextView>(R.id.output_fs_obt_value)

        val materials = MaterialRepository.getMaterials(this)
        val materialsByName = materials.associateBy { it.name }
        inputMaterial.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, materials.map { it.name })

        val defaultFsPreset = 1.5
        var isApplyingPreset = false

        fun applyPresetValues() {
            isApplyingPreset = true
            inputFsAdmValue.setText(NumberFormatter.format(defaultFsPreset))
            isApplyingPreset = false
        }

        fun recalculate(): Boolean {
            val raw = readInput(
                inputLoadValue, inputLoadUnit, inputLengthValue, inputLengthUnit,
                inputNeutralDistanceValue, inputInertiaValue,
                inputYieldValue, inputYieldUnit, inputModulusValue, inputModulusUnit, inputFsAdmValue
            ) ?: run {
                clearOutputs(
                    outputDeltaObtValue, outputDeltaAdmValue, outputMmaxValue, outputSigmaValue,
                    outputFyAdmValue, outputStatusDeflectionValue, outputPercentualDeltaValue,
                    outputStatusStressValue, outputFsObtValue
                )
                applyValidationStyle(outputStatusDeflectionValue, null)
                applyValidationStyle(outputStatusStressValue, null)
                return false
            }

            val outputUi = BeamCalculator.formatForUi(BeamCalculator.calculate(BeamCalculator.toSI(raw)))
            applyOutput(
                outputUi, outputDeltaObtValue, outputDeltaAdmValue, outputMmaxValue, outputSigmaValue,
                outputFyAdmValue, outputStatusDeflectionValue, outputPercentualDeltaValue,
                outputStatusStressValue, outputFsObtValue
            )
            return true
        }

        var lastMaterial: String? = null
        inputMaterial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = parent?.getItemAtPosition(position)?.toString()
                if (selected != lastMaterial) {
                    materialsByName[selected]?.let { material ->
                        val yieldUnit = parseStressUnit(inputYieldUnit.selectedItem?.toString())
                        val modulusUnit = parseModulusUnit(inputModulusUnit.selectedItem?.toString())
                        if (yieldUnit != null) inputYieldValue.setText(NumberFormatter.format(stressFromPa(material.fyMpa * 1e6, yieldUnit)))
                        if (modulusUnit != null) inputModulusValue.setText(NumberFormatter.format(modulusFromPa(material.eGpa * 1e9, modulusUnit)))
                    }
                    lastMaterial = selected
                    recalculate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        var lastLoadUnit = ForceUnit.N
        inputLoadUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            private var initialized = false
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedUnit = parseForceUnit(parent?.getItemAtPosition(position)?.toString()) ?: return
                if (!initialized) {
                    lastLoadUnit = selectedUnit
                    initialized = true
                    recalculate()
                    return
                }
                if (selectedUnit == lastLoadUnit) return
                val currentValue = parseDouble(inputLoadValue.text?.toString().orEmpty())
                if (currentValue > 0.0) {
                    inputLoadValue.setText(NumberFormatter.format(convertForce(currentValue, lastLoadUnit, selectedUnit)))
                }
                lastLoadUnit = selectedUnit
                recalculate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        var lastLengthUnit = LengthUnit.MM
        inputLengthUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            private var initialized = false
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedUnit = parseLengthUnit(parent?.getItemAtPosition(position)?.toString()) ?: return
                if (!initialized) {
                    lastLengthUnit = selectedUnit
                    initialized = true
                    recalculate()
                    return
                }
                if (selectedUnit == lastLengthUnit) return
                val currentValue = parseDouble(inputLengthValue.text?.toString().orEmpty())
                if (currentValue > 0.0) {
                    inputLengthValue.setText(NumberFormatter.format(convertLength(currentValue, lastLengthUnit, selectedUnit)))
                }
                lastLengthUnit = selectedUnit
                recalculate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        setupUnitRecalculation(inputYieldUnit) { recalculate() }
        setupUnitRecalculation(inputModulusUnit) { recalculate() }

        val recalcWatcher = DebouncedTextWatcher { recalculate() }
        inputLoadValue.addTextChangedListener(recalcWatcher)
        inputLengthValue.addTextChangedListener(recalcWatcher)
        inputNeutralDistanceValue.addTextChangedListener(recalcWatcher)
        inputInertiaValue.addTextChangedListener(recalcWatcher)
        inputYieldValue.addTextChangedListener(recalcWatcher)
        inputModulusValue.addTextChangedListener(recalcWatcher)

        inputFsAdmValue.addTextChangedListener(DebouncedTextWatcher { text ->
            val fsValue = parseDouble(text)
            if (!isApplyingPreset && fsValue != defaultFsPreset && inputPreset.isChecked) {
                inputPreset.isChecked = false
            }
            recalculate()
        })

        inputPreset.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyPresetValues()
                recalculate()
            }
        }

        if (!loadState(
                inputLoadValue, inputLoadUnit, inputLengthValue, inputLengthUnit, inputNeutralDistanceValue,
                inputInertiaValue, inputYieldValue, inputYieldUnit,
                inputModulusValue, inputModulusUnit, inputFsAdmValue, inputMaterial, inputPreset
            )) {
            inputPreset.isChecked = true
            applyPresetValues()
        }
        recalculate()
    }

    override fun onPause() {
        super.onPause()
        saveState(
            findViewById(R.id.input_load_value), findViewById(R.id.input_load_unit),
            findViewById(R.id.input_length_value), findViewById(R.id.input_length_unit),
            findViewById(R.id.input_neutral_distance_value),
            findViewById(R.id.input_inertia_value),
            findViewById(R.id.input_yield_value), findViewById(R.id.input_yield_unit),
            findViewById(R.id.input_modulus_value), findViewById(R.id.input_modulus_unit),
            findViewById(R.id.input_fs_adm_value), findViewById(R.id.input_material), findViewById(R.id.input_main_preset)
        )
    }

    private fun saveState(
        inputLoadValue: EditText, inputLoadUnit: Spinner,
        inputLengthValue: EditText, inputLengthUnit: Spinner,
        inputNeutralDistanceValue: EditText,
        inputInertiaValue: EditText,
        inputYieldValue: EditText, inputYieldUnit: Spinner,
        inputModulusValue: EditText, inputModulusUnit: Spinner,
        inputFsAdmValue: EditText, inputMaterial: Spinner, inputPreset: CheckBox
    ) {
        prefs.edit()
            .putString("load", inputLoadValue.text?.toString())
            .putInt("loadUnit", inputLoadUnit.selectedItemPosition)
            .putString("length", inputLengthValue.text?.toString())
            .putInt("lengthUnit", inputLengthUnit.selectedItemPosition)
            .putString("neutral", inputNeutralDistanceValue.text?.toString())
            .putString("inertia", inputInertiaValue.text?.toString())
            .putString("yield", inputYieldValue.text?.toString())
            .putInt("yieldUnit", inputYieldUnit.selectedItemPosition)
            .putString("modulus", inputModulusValue.text?.toString())
            .putInt("modulusUnit", inputModulusUnit.selectedItemPosition)
            .putString("fs", inputFsAdmValue.text?.toString())
            .putInt("material", inputMaterial.selectedItemPosition)
            .putBoolean("preset", inputPreset.isChecked)
            .apply()
    }

    private fun loadState(
        inputLoadValue: EditText, inputLoadUnit: Spinner,
        inputLengthValue: EditText, inputLengthUnit: Spinner,
        inputNeutralDistanceValue: EditText,
        inputInertiaValue: EditText,
        inputYieldValue: EditText, inputYieldUnit: Spinner,
        inputModulusValue: EditText, inputModulusUnit: Spinner,
        inputFsAdmValue: EditText, inputMaterial: Spinner, inputPreset: CheckBox
    ): Boolean {
        val hasState = prefs.contains("load")
        if (!hasState) return false
        inputLoadValue.setText(prefs.getString("load", ""))
        inputLoadUnit.setSelection(prefs.getInt("loadUnit", 0))
        inputLengthValue.setText(prefs.getString("length", ""))
        inputLengthUnit.setSelection(prefs.getInt("lengthUnit", 0))
        inputNeutralDistanceValue.setText(prefs.getString("neutral", ""))
        inputInertiaValue.setText(prefs.getString("inertia", ""))
        inputYieldValue.setText(prefs.getString("yield", ""))
        inputYieldUnit.setSelection(prefs.getInt("yieldUnit", 0))
        inputModulusValue.setText(prefs.getString("modulus", ""))
        inputModulusUnit.setSelection(prefs.getInt("modulusUnit", 0))
        inputFsAdmValue.setText(prefs.getString("fs", ""))
        inputMaterial.setSelection(prefs.getInt("material", 0))
        inputPreset.isChecked = prefs.getBoolean("preset", false)
        return true
    }

    private fun readInput(
        inputLoadValue: EditText, inputLoadUnit: Spinner,
        inputLengthValue: EditText, inputLengthUnit: Spinner,
        inputNeutralDistanceValue: EditText,
        inputInertiaValue: EditText,
        inputYieldValue: EditText, inputYieldUnit: Spinner,
        inputModulusValue: EditText, inputModulusUnit: Spinner,
        inputFsAdmValue: EditText
    ): InputDataRaw? {
        val loadValue = parseDouble(inputLoadValue.text?.toString().orEmpty())
        val lengthValue = parseDouble(inputLengthValue.text?.toString().orEmpty())
        val neutralValue = parseDouble(inputNeutralDistanceValue.text?.toString().orEmpty())
        val inertiaValue = parseDouble(inputInertiaValue.text?.toString().orEmpty())
        val yieldValue = parseDouble(inputYieldValue.text?.toString().orEmpty())
        val modulusValue = parseDouble(inputModulusValue.text?.toString().orEmpty())
        val fsValue = parseDouble(inputFsAdmValue.text?.toString().orEmpty())

        var isValid = true
        if (!validatePositive(inputLoadValue, loadValue, "P")) isValid = false
        if (!validatePositive(inputLengthValue, lengthValue, "L")) isValid = false
        if (!validatePositive(inputNeutralDistanceValue, neutralValue, "c")) isValid = false
        if (!validatePositive(inputInertiaValue, inertiaValue, "I")) isValid = false
        if (!validatePositive(inputYieldValue, yieldValue, "fy")) isValid = false
        if (!validatePositive(inputModulusValue, modulusValue, "E")) isValid = false
        if (!validatePositive(inputFsAdmValue, fsValue, "FS")) isValid = false

        val loadUnit = parseForceUnit(inputLoadUnit.selectedItem?.toString())
        val lengthUnit = parseLengthUnit(inputLengthUnit.selectedItem?.toString())
        val yieldUnit = parseStressUnit(inputYieldUnit.selectedItem?.toString())
        val modulusUnit = parseModulusUnit(inputModulusUnit.selectedItem?.toString())

        if (!isValid || loadUnit == null || lengthUnit == null || yieldUnit == null || modulusUnit == null) {
            return null
        }

        return InputDataRaw(
            P = loadValue,
            PUnit = loadUnit,
            L = lengthValue,
            LUnit = lengthUnit,
            c = neutralValue,
            cUnit = LengthUnit.MM,
            I = inertiaValue,
            IUnit = InertiaUnit.MM4,
            fy = yieldValue,
            fyUnit = yieldUnit,
            E = modulusValue,
            EUnit = modulusUnit,
            FS_adm = fsValue
        )
    }

    private fun applyOutput(
        output: OutputDataUi,
        outputDeltaObtValue: TextView,
        outputDeltaAdmValue: TextView,
        outputMmaxValue: TextView,
        outputSigmaValue: TextView,
        outputFyAdmValue: TextView,
        outputStatusDeflectionValue: TextView,
        outputPercentualDeltaValue: TextView,
        outputStatusStressValue: TextView,
        outputFsObtValue: TextView
    ) {
        outputDeltaObtValue.text = "${NumberFormatter.format(output.delta_obt)} mm"
        outputDeltaAdmValue.text = "${NumberFormatter.format(output.delta_adm)} mm"
        outputMmaxValue.text = "${NumberFormatter.format(output.Mmax)} N·m"
        outputSigmaValue.text = "${NumberFormatter.format(output.sigma)} MPa"
        outputFyAdmValue.text = "${NumberFormatter.format(output.fy_adm)} MPa"
        outputStatusDeflectionValue.text = output.status_text_deflection
        applyValidationStyle(outputStatusDeflectionValue, output.status_text_deflection)
        outputPercentualDeltaValue.text = "${NumberFormatter.format(output.percentual_delta)} %"
        outputStatusStressValue.text = output.status_text_stress
        applyValidationStyle(outputStatusStressValue, output.status_text_stress)
        outputFsObtValue.text = NumberFormatter.format(output.FS_obtido)
    }

    private fun applyValidationStyle(view: TextView, text: String?) {
        val normalized = text?.lowercase(Locale.getDefault()).orEmpty()
        val isFailure = normalized.contains("falhou") || normalized.contains("não ok") || normalized.contains("nao ok")
        view.setTypeface(null, if (isFailure) Typeface.BOLD else Typeface.NORMAL)
        view.setTextColor(if (isFailure) Color.RED else Color.BLACK)
    }

    private fun clearOutputs(vararg fields: TextView) {
        fields.forEach { it.text = "—" }
    }

    private fun validatePositive(editText: TextView, value: Double, label: String): Boolean {
        return if (value <= 0.0) {
            editText.error = "$label deve ser maior que 0"
            false
        } else {
            editText.error = null
            true
        }
    }

    private fun parseDouble(value: String): Double = value.trim().replace(',', '.').toDoubleOrNull() ?: 0.0

    private fun setupUnitRecalculation(spinner: Spinner, onRecalculate: () -> Unit) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var initialized = false
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (initialized) onRecalculate() else initialized = true
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun normalizeUnitText(text: String?): String? =
        text?.trim()?.lowercase(Locale.getDefault())?.replace("·", ".")?.replace(" ", "")?.takeIf { it.isNotEmpty() }

    private fun parseForceUnit(text: String?) = when (normalizeUnitText(text)) { "n" -> ForceUnit.N; "kn" -> ForceUnit.KN; "kg" -> ForceUnit.KGF; else -> null }
    private fun parseLengthUnit(text: String?) = when (normalizeUnitText(text)) { "mm" -> LengthUnit.MM; "m" -> LengthUnit.M; else -> null }
    private fun parseStressUnit(text: String?) = when (normalizeUnitText(text)) { "mpa" -> StressUnit.MPA; "pa" -> StressUnit.PA; else -> null }
    private fun parseModulusUnit(text: String?) = when (normalizeUnitText(text)) { "gpa" -> ModulusUnit.GPA; "mpa" -> ModulusUnit.MPA; "pa" -> ModulusUnit.PA; else -> null }

    private fun convertForce(value: Double, from: ForceUnit, to: ForceUnit): Double {
        val n = when (from) {
            ForceUnit.N -> value
            ForceUnit.KN -> value * 1_000.0
            ForceUnit.KGF -> value * 9.80665
        }
        return when (to) {
            ForceUnit.N -> n
            ForceUnit.KN -> n / 1_000.0
            ForceUnit.KGF -> n / 9.80665
        }
    }

    private fun convertLength(value: Double, from: LengthUnit, to: LengthUnit): Double {
        val mm = when (from) {
            LengthUnit.MM -> value
            LengthUnit.M -> value * 1_000.0
        }
        return when (to) {
            LengthUnit.MM -> mm
            LengthUnit.M -> mm / 1_000.0
        }
    }

    private fun stressFromPa(valuePa: Double, unit: StressUnit): Double = when (unit) { StressUnit.MPA -> valuePa / 1e6; StressUnit.PA -> valuePa }
    private fun modulusFromPa(valuePa: Double, unit: ModulusUnit): Double = when (unit) { ModulusUnit.GPA -> valuePa / 1e9; ModulusUnit.MPA -> valuePa / 1e6; ModulusUnit.PA -> valuePa }
}
