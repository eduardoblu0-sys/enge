package com.example.enge

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.enge.model.BeamCalculator
import com.example.enge.model.ForceUnit
import com.example.enge.model.InertiaUnit
import com.example.enge.model.InputDataRaw
import com.example.enge.model.LengthUnit
import com.example.enge.model.ModulusUnit
import com.example.enge.model.OutputDataUi
import com.example.enge.model.StressUnit
import com.example.enge.util.NumberFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputLoadValue = findViewById<EditText>(R.id.input_load_value)
        val inputLoadUnit = findViewById<Spinner>(R.id.input_load_unit)
        val inputLengthValue = findViewById<EditText>(R.id.input_length_value)
        val inputLengthUnit = findViewById<Spinner>(R.id.input_length_unit)
        val inputNeutralDistanceValue = findViewById<EditText>(R.id.input_neutral_distance_value)
        val inputNeutralDistanceUnit = findViewById<Spinner>(R.id.input_neutral_distance_unit)
        val inputInertiaValue = findViewById<EditText>(R.id.input_inertia_value)
        val inputInertiaUnit = findViewById<Spinner>(R.id.input_inertia_unit)
        val inputYieldValue = findViewById<EditText>(R.id.input_yield_value)
        val inputYieldUnit = findViewById<Spinner>(R.id.input_yield_unit)
        val inputModulusValue = findViewById<EditText>(R.id.input_modulus_value)
        val inputModulusUnit = findViewById<Spinner>(R.id.input_modulus_unit)
        val inputFsAdmValue = findViewById<EditText>(R.id.input_fs_adm_value)
        val inputMaterial = findViewById<Spinner>(R.id.input_material)

        val outputDeltaObtValue = findViewById<EditText>(R.id.output_delta_obt_value)
        val outputDeltaObtUnit = findViewById<Spinner>(R.id.output_delta_obt_unit)
        val outputDeltaAdmValue = findViewById<EditText>(R.id.output_delta_adm_value)
        val outputDeltaAdmUnit = findViewById<Spinner>(R.id.output_delta_adm_unit)
        val outputMmaxValue = findViewById<EditText>(R.id.output_mmax_value)
        val outputMmaxUnit = findViewById<Spinner>(R.id.output_mmax_unit)
        val outputSigmaValue = findViewById<EditText>(R.id.output_sigma_value)
        val outputSigmaUnit = findViewById<Spinner>(R.id.output_sigma_unit)
        val outputFyAdmValue = findViewById<EditText>(R.id.output_fy_adm_value)
        val outputFyAdmUnit = findViewById<Spinner>(R.id.output_fy_adm_unit)
        val outputStatusDeflectionValue = findViewById<EditText>(R.id.output_status_deflection_value)
        val outputPercentualDeltaValue = findViewById<EditText>(R.id.output_percentual_delta_value)
        val outputStatusStressValue = findViewById<EditText>(R.id.output_status_stress_value)
        val outputFsObtValue = findViewById<EditText>(R.id.output_fs_obt_value)

        val inputBindings = InputBindings(
            load = InputBinding(inputLoadValue, inputLoadUnit),
            length = InputBinding(inputLengthValue, inputLengthUnit),
            neutralAxis = InputBinding(inputNeutralDistanceValue, inputNeutralDistanceUnit),
            inertia = InputBinding(inputInertiaValue, inputInertiaUnit),
            yield = InputBinding(inputYieldValue, inputYieldUnit),
            modulus = InputBinding(inputModulusValue, inputModulusUnit)
        )

        val materials = mapOf(
            "SAE 1020 Laminado" to MaterialProperties(fyMpa = 350.0, eGPa = 200.0),
            "SAE 1045" to MaterialProperties(fyMpa = 530.0, eGPa = 200.0),
            "Inox 304" to MaterialProperties(fyMpa = 215.0, eGPa = 193.0),
            "Alumínio 6061" to MaterialProperties(fyMpa = 275.0, eGPa = 69.0),
            "Plástico ABS" to MaterialProperties(fyMpa = 40.0, eGPa = 2.1)
        )

        var lastMaterial: String? = null
        inputMaterial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent?.getItemAtPosition(position)?.toString()
                if (selected != lastMaterial) {
                    materials[selected]?.let { material ->
                        val yieldUnit = parseStressUnit(readUnitLabel(inputYieldUnit))
                        val modulusUnit = parseModulusUnit(readUnitLabel(inputModulusUnit))
                        if (yieldUnit != null) {
                            val fyPa = material.fyMpa * 1e6
                            inputYieldValue.setText(NumberFormatter.format(stressFromPa(fyPa, yieldUnit)))
                        }
                        if (modulusUnit != null) {
                            val modulusPa = material.eGPa * 1e9
                            inputModulusValue.setText(NumberFormatter.format(modulusFromPa(modulusPa, modulusUnit)))
                        }
                    }
                    lastMaterial = selected
                    recalculate(
                        inputBindings = inputBindings,
                        inputFsAdmValue = inputFsAdmValue,
                        outputDeltaObtValue = outputDeltaObtValue,
                        outputDeltaAdmValue = outputDeltaAdmValue,
                        outputMmaxValue = outputMmaxValue,
                        outputSigmaValue = outputSigmaValue,
                        outputFyAdmValue = outputFyAdmValue,
                        outputStatusDeflectionValue = outputStatusDeflectionValue,
                        outputPercentualDeltaValue = outputPercentualDeltaValue,
                        outputStatusStressValue = outputStatusStressValue,
                        outputFsObtValue = outputFsObtValue,
                        outputDeltaObtUnit = outputDeltaObtUnit,
                        outputDeltaAdmUnit = outputDeltaAdmUnit,
                        outputMmaxUnit = outputMmaxUnit,
                        outputSigmaUnit = outputSigmaUnit,
                        outputFyAdmUnit = outputFyAdmUnit
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        val calculateButton = findViewById<Button>(R.id.button_calculate)

        calculateButton.setOnClickListener {
            val success = recalculate(
                inputBindings = inputBindings,
                inputFsAdmValue = inputFsAdmValue,
                outputDeltaObtValue = outputDeltaObtValue,
                outputDeltaAdmValue = outputDeltaAdmValue,
                outputMmaxValue = outputMmaxValue,
                outputSigmaValue = outputSigmaValue,
                outputFyAdmValue = outputFyAdmValue,
                outputStatusDeflectionValue = outputStatusDeflectionValue,
                outputPercentualDeltaValue = outputPercentualDeltaValue,
                outputStatusStressValue = outputStatusStressValue,
                outputFsObtValue = outputFsObtValue,
                outputDeltaObtUnit = outputDeltaObtUnit,
                outputDeltaAdmUnit = outputDeltaAdmUnit,
                outputMmaxUnit = outputMmaxUnit,
                outputSigmaUnit = outputSigmaUnit,
                outputFyAdmUnit = outputFyAdmUnit
            )
            if (!success) {
                Toast.makeText(this, "Verifique os campos destacados.", Toast.LENGTH_SHORT).show()
            }
        }

        setupUnitRecalculation(inputLoadUnit) {
            recalculate(
                inputBindings = inputBindings,
                inputFsAdmValue = inputFsAdmValue,
                outputDeltaObtValue = outputDeltaObtValue,
                outputDeltaAdmValue = outputDeltaAdmValue,
                outputMmaxValue = outputMmaxValue,
                outputSigmaValue = outputSigmaValue,
                outputFyAdmValue = outputFyAdmValue,
                outputStatusDeflectionValue = outputStatusDeflectionValue,
                outputPercentualDeltaValue = outputPercentualDeltaValue,
                outputStatusStressValue = outputStatusStressValue,
                outputFsObtValue = outputFsObtValue,
                outputDeltaObtUnit = outputDeltaObtUnit,
                outputDeltaAdmUnit = outputDeltaAdmUnit,
                outputMmaxUnit = outputMmaxUnit,
                outputSigmaUnit = outputSigmaUnit,
                outputFyAdmUnit = outputFyAdmUnit
            )
        }
        setupUnitRecalculation(inputLengthUnit) {
            recalculate(
                inputBindings = inputBindings,
                inputFsAdmValue = inputFsAdmValue,
                outputDeltaObtValue = outputDeltaObtValue,
                outputDeltaAdmValue = outputDeltaAdmValue,
                outputMmaxValue = outputMmaxValue,
                outputSigmaValue = outputSigmaValue,
                outputFyAdmValue = outputFyAdmValue,
                outputStatusDeflectionValue = outputStatusDeflectionValue,
                outputPercentualDeltaValue = outputPercentualDeltaValue,
                outputStatusStressValue = outputStatusStressValue,
                outputFsObtValue = outputFsObtValue,
                outputDeltaObtUnit = outputDeltaObtUnit,
                outputDeltaAdmUnit = outputDeltaAdmUnit,
                outputMmaxUnit = outputMmaxUnit,
                outputSigmaUnit = outputSigmaUnit,
                outputFyAdmUnit = outputFyAdmUnit
            )
        }
        setupUnitRecalculation(inputNeutralDistanceUnit) {
            recalculate(
                inputBindings = inputBindings,
                inputFsAdmValue = inputFsAdmValue,
                outputDeltaObtValue = outputDeltaObtValue,
                outputDeltaAdmValue = outputDeltaAdmValue,
                outputMmaxValue = outputMmaxValue,
                outputSigmaValue = outputSigmaValue,
                outputFyAdmValue = outputFyAdmValue,
                outputStatusDeflectionValue = outputStatusDeflectionValue,
                outputPercentualDeltaValue = outputPercentualDeltaValue,
                outputStatusStressValue = outputStatusStressValue,
                outputFsObtValue = outputFsObtValue,
                outputDeltaObtUnit = outputDeltaObtUnit,
                outputDeltaAdmUnit = outputDeltaAdmUnit,
                outputMmaxUnit = outputMmaxUnit,
                outputSigmaUnit = outputSigmaUnit,
                outputFyAdmUnit = outputFyAdmUnit
            )
        }
        setupUnitRecalculation(inputInertiaUnit) {
            recalculate(
                inputBindings = inputBindings,
                inputFsAdmValue = inputFsAdmValue,
                outputDeltaObtValue = outputDeltaObtValue,
                outputDeltaAdmValue = outputDeltaAdmValue,
                outputMmaxValue = outputMmaxValue,
                outputSigmaValue = outputSigmaValue,
                outputFyAdmValue = outputFyAdmValue,
                outputStatusDeflectionValue = outputStatusDeflectionValue,
                outputPercentualDeltaValue = outputPercentualDeltaValue,
                outputStatusStressValue = outputStatusStressValue,
                outputFsObtValue = outputFsObtValue,
                outputDeltaObtUnit = outputDeltaObtUnit,
                outputDeltaAdmUnit = outputDeltaAdmUnit,
                outputMmaxUnit = outputMmaxUnit,
                outputSigmaUnit = outputSigmaUnit,
                outputFyAdmUnit = outputFyAdmUnit
            )
        }
        setupUnitRecalculation(inputYieldUnit) {
            recalculate(
                inputBindings = inputBindings,
                inputFsAdmValue = inputFsAdmValue,
                outputDeltaObtValue = outputDeltaObtValue,
                outputDeltaAdmValue = outputDeltaAdmValue,
                outputMmaxValue = outputMmaxValue,
                outputSigmaValue = outputSigmaValue,
                outputFyAdmValue = outputFyAdmValue,
                outputStatusDeflectionValue = outputStatusDeflectionValue,
                outputPercentualDeltaValue = outputPercentualDeltaValue,
                outputStatusStressValue = outputStatusStressValue,
                outputFsObtValue = outputFsObtValue,
                outputDeltaObtUnit = outputDeltaObtUnit,
                outputDeltaAdmUnit = outputDeltaAdmUnit,
                outputMmaxUnit = outputMmaxUnit,
                outputSigmaUnit = outputSigmaUnit,
                outputFyAdmUnit = outputFyAdmUnit
            )
        }
        setupUnitRecalculation(inputModulusUnit) {
            recalculate(
                inputBindings = inputBindings,
                inputFsAdmValue = inputFsAdmValue,
                outputDeltaObtValue = outputDeltaObtValue,
                outputDeltaAdmValue = outputDeltaAdmValue,
                outputMmaxValue = outputMmaxValue,
                outputSigmaValue = outputSigmaValue,
                outputFyAdmValue = outputFyAdmValue,
                outputStatusDeflectionValue = outputStatusDeflectionValue,
                outputPercentualDeltaValue = outputPercentualDeltaValue,
                outputStatusStressValue = outputStatusStressValue,
                outputFsObtValue = outputFsObtValue,
                outputDeltaObtUnit = outputDeltaObtUnit,
                outputDeltaAdmUnit = outputDeltaAdmUnit,
                outputMmaxUnit = outputMmaxUnit,
                outputSigmaUnit = outputSigmaUnit,
                outputFyAdmUnit = outputFyAdmUnit
            )
        }
    }

    private fun recalculate(
        inputBindings: InputBindings,
        inputFsAdmValue: EditText,
        outputDeltaObtValue: EditText,
        outputDeltaAdmValue: EditText,
        outputMmaxValue: EditText,
        outputSigmaValue: EditText,
        outputFyAdmValue: EditText,
        outputStatusDeflectionValue: EditText,
        outputPercentualDeltaValue: EditText,
        outputStatusStressValue: EditText,
        outputFsObtValue: EditText,
        outputDeltaObtUnit: Spinner,
        outputDeltaAdmUnit: Spinner,
        outputMmaxUnit: Spinner,
        outputSigmaUnit: Spinner,
        outputFyAdmUnit: Spinner
    ): Boolean {
        val raw = readInput(inputBindings, inputFsAdmValue)
        if (raw == null) {
            clearOutputs(
                outputDeltaObtValue,
                outputDeltaAdmValue,
                outputMmaxValue,
                outputSigmaValue,
                outputFyAdmValue,
                outputStatusDeflectionValue,
                outputPercentualDeltaValue,
                outputStatusStressValue,
                outputFsObtValue
            )
            return false
        }

        val si = BeamCalculator.toSI(raw)
        val outputSi = BeamCalculator.calculate(si)
        val outputUi = BeamCalculator.formatForUi(outputSi)
        applyOutputUnits(
            outputDeltaObtUnit,
            outputDeltaAdmUnit,
            outputMmaxUnit,
            outputSigmaUnit,
            outputFyAdmUnit
        )
        applyOutput(outputUi, outputDeltaObtValue, outputDeltaAdmValue, outputMmaxValue, outputSigmaValue,
            outputFyAdmValue, outputStatusDeflectionValue, outputPercentualDeltaValue, outputStatusStressValue,
            outputFsObtValue)
        return true
    }

    private fun validatePositive(editText: EditText, value: Double, label: String): Boolean {
        return if (value <= 0.0) {
            editText.error = "$label deve ser maior que 0"
            false
        } else {
            editText.error = null
            true
        }
    }

    private fun clearOutputs(vararg fields: EditText) {
        fields.forEach { it.setText("") }
    }

    private fun parseDouble(value: String): Double {
        val normalized = value.replace(',', '.')
        return normalized.toDoubleOrNull() ?: 0.0
    }

    private fun readInput(inputBindings: InputBindings, inputFsAdmValue: EditText): InputDataRaw? {
        var isValid = true

        val loadValue = parseDouble(inputBindings.load.valueField.text.toString())
        val loadUnit = parseForceUnit(readUnitLabel(inputBindings.load.unitView))
        if (!validatePositive(inputBindings.load.valueField, loadValue, "P")) isValid = false
        if (loadUnit == null) {
            setUnitError(inputBindings.load.unitView, "Unidade inválida")
            isValid = false
        } else {
            clearUnitError(inputBindings.load.unitView)
        }

        val lengthValue = parseDouble(inputBindings.length.valueField.text.toString())
        val lengthUnit = parseLengthUnit(readUnitLabel(inputBindings.length.unitView))
        if (!validatePositive(inputBindings.length.valueField, lengthValue, "L")) isValid = false
        if (lengthUnit == null) {
            setUnitError(inputBindings.length.unitView, "Unidade inválida")
            isValid = false
        } else {
            clearUnitError(inputBindings.length.unitView)
        }

        val neutralValue = parseDouble(inputBindings.neutralAxis.valueField.text.toString())
        val neutralUnit = parseLengthUnit(readUnitLabel(inputBindings.neutralAxis.unitView))
        if (!validatePositive(inputBindings.neutralAxis.valueField, neutralValue, "c")) isValid = false
        if (neutralUnit == null) {
            setUnitError(inputBindings.neutralAxis.unitView, "Unidade inválida")
            isValid = false
        } else {
            clearUnitError(inputBindings.neutralAxis.unitView)
        }

        val inertiaValue = parseDouble(inputBindings.inertia.valueField.text.toString())
        val inertiaUnit = parseInertiaUnit(readUnitLabel(inputBindings.inertia.unitView))
        if (!validatePositive(inputBindings.inertia.valueField, inertiaValue, "I")) isValid = false
        if (inertiaUnit == null) {
            setUnitError(inputBindings.inertia.unitView, "Unidade inválida")
            isValid = false
        } else {
            clearUnitError(inputBindings.inertia.unitView)
            val inertiaSi = BeamCalculator.inertiaToM4(inertiaValue, inertiaUnit)
            if (inertiaValue > 0.0 && inertiaSi < 1e-18) {
                inputBindings.inertia.valueField.error = "I muito pequeno"
                isValid = false
            }
        }

        val yieldValue = parseDouble(inputBindings.yield.valueField.text.toString())
        val yieldUnit = parseStressUnit(readUnitLabel(inputBindings.yield.unitView))
        if (!validatePositive(inputBindings.yield.valueField, yieldValue, "fy")) isValid = false
        if (yieldUnit == null) {
            setUnitError(inputBindings.yield.unitView, "Unidade inválida")
            isValid = false
        } else {
            clearUnitError(inputBindings.yield.unitView)
        }

        val modulusValue = parseDouble(inputBindings.modulus.valueField.text.toString())
        val modulusUnit = parseModulusUnit(readUnitLabel(inputBindings.modulus.unitView))
        if (!validatePositive(inputBindings.modulus.valueField, modulusValue, "E")) isValid = false
        if (modulusUnit == null) {
            setUnitError(inputBindings.modulus.unitView, "Unidade inválida")
            isValid = false
        } else {
            clearUnitError(inputBindings.modulus.unitView)
        }

        val fsValue = parseDouble(inputFsAdmValue.text.toString())
        if (!validatePositive(inputFsAdmValue, fsValue, "FS_adm")) isValid = false

        if (!isValid || loadUnit == null || lengthUnit == null || neutralUnit == null ||
            inertiaUnit == null || yieldUnit == null || modulusUnit == null
        ) {
            return null
        }

        return InputDataRaw(
            P = loadValue,
            PUnit = loadUnit,
            L = lengthValue,
            LUnit = lengthUnit,
            c = neutralValue,
            cUnit = neutralUnit,
            I = inertiaValue,
            IUnit = inertiaUnit,
            fy = yieldValue,
            fyUnit = yieldUnit,
            E = modulusValue,
            EUnit = modulusUnit,
            FS_adm = fsValue
        )
    }

    private fun applyOutput(
        output: OutputDataUi,
        outputDeltaObtValue: EditText,
        outputDeltaAdmValue: EditText,
        outputMmaxValue: EditText,
        outputSigmaValue: EditText,
        outputFyAdmValue: EditText,
        outputStatusDeflectionValue: EditText,
        outputPercentualDeltaValue: EditText,
        outputStatusStressValue: EditText,
        outputFsObtValue: EditText
    ) {
        outputDeltaObtValue.setText(NumberFormatter.format(output.delta_obt))
        outputDeltaAdmValue.setText(NumberFormatter.format(output.delta_adm))
        outputMmaxValue.setText(NumberFormatter.format(output.Mmax))
        outputSigmaValue.setText(NumberFormatter.format(output.sigma))
        outputFyAdmValue.setText(NumberFormatter.format(output.fy_adm))
        outputStatusDeflectionValue.setText(output.status_text_deflection)
        outputPercentualDeltaValue.setText(NumberFormatter.format(output.percentual_delta))
        outputStatusStressValue.setText(output.status_text_stress)
        outputFsObtValue.setText(NumberFormatter.format(output.FS_obtido))
    }

    private fun applyOutputUnits(
        outputDeltaObtUnit: Spinner,
        outputDeltaAdmUnit: Spinner,
        outputMmaxUnit: Spinner,
        outputSigmaUnit: Spinner,
        outputFyAdmUnit: Spinner
    ) {
        setSpinnerSelection(outputDeltaObtUnit, "mm")
        setSpinnerSelection(outputDeltaAdmUnit, "mm")
        setSpinnerSelection(outputMmaxUnit, "N·m")
        setSpinnerSelection(outputSigmaUnit, "MPa")
        setSpinnerSelection(outputFyAdmUnit, "MPa")
    }

    private fun setSpinnerSelection(spinner: Spinner, target: String) {
        val adapter = spinner.adapter
        for (index in 0 until adapter.count) {
            if (adapter.getItem(index)?.toString() == target) {
                spinner.setSelection(index)
                break
            }
        }
    }

    private fun setupUnitRecalculation(spinner: Spinner, onRecalculate: () -> Unit) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var initialized = false

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (initialized) {
                    onRecalculate()
                } else {
                    initialized = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun readUnitLabel(view: View): String? {
        return when (view) {
            is Spinner -> view.selectedItem?.toString()
            is AutoCompleteTextView -> view.text?.toString()
            else -> null
        }
    }

    private fun normalizeUnitText(text: String?): String? {
        val normalized = text?.trim()?.lowercase(Locale.getDefault())?.replace("·", ".")?.replace(" ", "")
        return normalized?.takeIf { it.isNotEmpty() }
    }

    private fun parseForceUnit(text: String?): ForceUnit? {
        return when (normalizeUnitText(text)) {
            "n" -> ForceUnit.N
            "kn" -> ForceUnit.KN
            else -> null
        }
    }

    private fun parseLengthUnit(text: String?): LengthUnit? {
        return when (normalizeUnitText(text)) {
            "mm" -> LengthUnit.MM
            "m" -> LengthUnit.M
            else -> null
        }
    }

    private fun parseInertiaUnit(text: String?): InertiaUnit? {
        return when (normalizeUnitText(text)) {
            "mm^4", "mm4" -> InertiaUnit.MM4
            "m^4", "m4" -> InertiaUnit.M4
            else -> null
        }
    }

    private fun parseStressUnit(text: String?): StressUnit? {
        return when (normalizeUnitText(text)) {
            "mpa" -> StressUnit.MPA
            "pa" -> StressUnit.PA
            else -> null
        }
    }

    private fun parseModulusUnit(text: String?): ModulusUnit? {
        return when (normalizeUnitText(text)) {
            "gpa" -> ModulusUnit.GPA
            "mpa" -> ModulusUnit.MPA
            "pa" -> ModulusUnit.PA
            else -> null
        }
    }

    private fun stressFromPa(valuePa: Double, unit: StressUnit): Double {
        return when (unit) {
            StressUnit.MPA -> valuePa / 1e6
            StressUnit.PA -> valuePa
        }
    }

    private fun modulusFromPa(valuePa: Double, unit: ModulusUnit): Double {
        return when (unit) {
            ModulusUnit.GPA -> valuePa / 1e9
            ModulusUnit.MPA -> valuePa / 1e6
            ModulusUnit.PA -> valuePa
        }
    }

    private fun setUnitError(view: View, message: String) {
        when (view) {
            is Spinner -> {
                (view.selectedView as? TextView)?.error = message
            }
            is AutoCompleteTextView -> view.error = message
        }
    }

    private fun clearUnitError(view: View) {
        when (view) {
            is Spinner -> {
                (view.selectedView as? TextView)?.error = null
            }
            is AutoCompleteTextView -> view.error = null
        }
    }

    private data class MaterialProperties(
        val fyMpa: Double,
        val eGPa: Double
    )

    private data class InputBinding(
        val valueField: EditText,
        val unitView: View
    )

    private data class InputBindings(
        val load: InputBinding,
        val length: InputBinding,
        val neutralAxis: InputBinding,
        val inertia: InputBinding,
        val yield: InputBinding,
        val modulus: InputBinding
    )
}
