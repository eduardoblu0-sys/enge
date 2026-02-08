package com.example.enge

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.example.enge.model.InputData
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: BeamViewModel by viewModels()

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

        val calculateButton = findViewById<Button>(R.id.button_calculate)

        calculateButton.setOnClickListener {
            val input = InputData(
                P = parseDouble(inputLoadValue.text.toString()),
                PUnit = inputLoadUnit.selectedItem?.toString().orEmpty(),
                L = parseDouble(inputLengthValue.text.toString()),
                LUnit = inputLengthUnit.selectedItem?.toString().orEmpty(),
                c = parseDouble(inputNeutralDistanceValue.text.toString()),
                cUnit = inputNeutralDistanceUnit.selectedItem?.toString().orEmpty(),
                I = parseDouble(inputInertiaValue.text.toString()),
                IUnit = inputInertiaUnit.selectedItem?.toString().orEmpty(),
                fy = parseDouble(inputYieldValue.text.toString()),
                fyUnit = inputYieldUnit.selectedItem?.toString().orEmpty(),
                E = parseDouble(inputModulusValue.text.toString()),
                EUnit = inputModulusUnit.selectedItem?.toString().orEmpty(),
                FS_adm = parseDouble(inputFsAdmValue.text.toString()),
                material = inputMaterial.selectedItem?.toString()
            )
            viewModel.calculate(input)
        }

        viewModel.output.observe(this) { output ->
            val deltaObtDisplay = convertDistance(output.delta_obt, outputDeltaObtUnit.selectedItem?.toString())
            val deltaAdmDisplay = convertDistance(output.delta_adm, outputDeltaAdmUnit.selectedItem?.toString())
            val mmaxDisplay = convertMoment(output.Mmax, outputMmaxUnit.selectedItem?.toString())
            val sigmaDisplay = convertStress(output.sigma, outputSigmaUnit.selectedItem?.toString())
            val fyAdmDisplay = convertStress(output.fy_adm, outputFyAdmUnit.selectedItem?.toString())

            outputDeltaObtValue.setText(formatNumber(deltaObtDisplay))
            outputDeltaAdmValue.setText(formatNumber(deltaAdmDisplay))
            outputMmaxValue.setText(formatNumber(mmaxDisplay))
            outputSigmaValue.setText(formatNumber(sigmaDisplay))
            outputFyAdmValue.setText(formatNumber(fyAdmDisplay))
            outputStatusDeflectionValue.setText(output.status_text_deflection)
            outputPercentualDeltaValue.setText(formatNumber(output.percentual_delta))
            outputStatusStressValue.setText(output.status_text_stress)
            outputFsObtValue.setText(formatNumber(output.FS_obtido))
        }
    }

    private fun parseDouble(value: String): Double {
        val normalized = value.replace(',', '.')
        return normalized.toDoubleOrNull() ?: 0.0
    }

    private fun formatNumber(value: Double): String {
        return String.format(Locale.US, "%.4f", value)
    }

    private fun convertDistance(valueInMeters: Double, unit: String?): Double {
        return when (unit) {
            "mm" -> valueInMeters * 1_000.0
            "cm" -> valueInMeters * 100.0
            else -> valueInMeters
        }
    }

    private fun convertMoment(valueInNm: Double, unit: String?): Double {
        return when (unit) {
            "kN m" -> valueInNm / 1_000.0
            else -> valueInNm
        }
    }

    private fun convertStress(valueInPa: Double, unit: String?): Double {
        return when (unit) {
            "GPa" -> valueInPa / 1_000_000_000.0
            else -> valueInPa / 1_000_000.0
        }
    }
}
