package com.example.enge

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
import com.example.enge.model.MaterialRepository
import com.example.enge.util.DebouncedTextWatcher
import com.example.enge.util.NumberFormatter
import kotlinx.coroutines.launch

class BucklingActivity : ComponentActivity() {
    private val viewModel: BucklingViewModel by viewModels()
    private val prefs by lazy { getSharedPreferences("buckling_form_state", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buckling)

        val inputLength = findViewById<EditText>(R.id.input_buckling_length)
        val inputKFactor = findViewById<Spinner>(R.id.input_buckling_k_factor)
        val inputArea = findViewById<EditText>(R.id.input_buckling_area)
        val inputInertiaX = findViewById<EditText>(R.id.input_buckling_inertia_x)
        val inputInertiaY = findViewById<EditText>(R.id.input_buckling_inertia_y)
        val inputLoadApplied = findViewById<EditText>(R.id.input_buckling_load_applied)
        val inputMaterial = findViewById<Spinner>(R.id.input_buckling_material)
        val inputModulus = findViewById<EditText>(R.id.input_buckling_modulus)
        val inputYield = findViewById<EditText>(R.id.input_buckling_yield)
        val inputGamma = findViewById<EditText>(R.id.input_buckling_gamma)
        val inputLambdaLim = findViewById<EditText>(R.id.input_buckling_lambda_limit)
        val inputAngle = findViewById<EditText>(R.id.input_buckling_angle)
        val inputPreset = findViewById<CheckBox>(R.id.input_buckling_preset)

        val outputKl = findViewById<TextView>(R.id.output_buckling_kl)
        val outputRx = findViewById<TextView>(R.id.output_buckling_rx)
        val outputRy = findViewById<TextView>(R.id.output_buckling_ry)
        val outputLambdaX = findViewById<TextView>(R.id.output_buckling_lambda_x)
        val outputLambdaY = findViewById<TextView>(R.id.output_buckling_lambda_y)
        val outputLambdaCrit = findViewById<TextView>(R.id.output_buckling_lambda_crit)
        val outputAxis = findViewById<TextView>(R.id.output_buckling_axis)
        val outputRegime = findViewById<TextView>(R.id.output_buckling_regime)
        val outputSigmaCr = findViewById<TextView>(R.id.output_buckling_sigma_cr)
        val outputNCr = findViewById<TextView>(R.id.output_buckling_n_cr)
        val outputNRd = findViewById<TextView>(R.id.output_buckling_n_rd)
        val outputUtilization = findViewById<TextView>(R.id.output_buckling_utilization)
        val outputKgf = findViewById<TextView>(R.id.output_buckling_kgf)
        val outputKgfIncl = findViewById<TextView>(R.id.output_buckling_kgf_inclination)
        val outputStatus = findViewById<TextView>(R.id.output_buckling_status)

        val defaultAngle = 3.0
        val defaultLambdaLimit = 160.0
        val defaultGamma = 1.5
        val kValues = listOf(0.65, 2.1, 0.8, 1.2, 1.2, 1.2, 1.0, 1.2, 2.1, 2.0)
        var isApplyingPreset = false

        fun applyPresetValues() {
            isApplyingPreset = true
            inputAngle.setText(defaultAngle.toInt().toString())
            inputLambdaLim.setText(defaultLambdaLimit.toInt().toString())
            inputGamma.setText(NumberFormatter.format(defaultGamma))
            isApplyingPreset = false
        }

        val materials = MaterialRepository.getMaterials(this)
        val materialsByName = materials.associateBy { it.name }
        inputMaterial.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            materials.map { it.name }
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
                    materialsByName[selected]?.let { material ->
                        inputYield.setText(NumberFormatter.format(material.fyMpa))
                        inputModulus.setText(NumberFormatter.format(material.eGpa))
                    }
                    lastMaterial = selected
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        inputPreset.isChecked = true
        inputKFactor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.updateInputs { it.copy(k = kValues.getOrNull(position)) }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        inputLength.addTextChangedListener(
            DebouncedTextWatcher { text ->
                viewModel.updateInputs { it.copy(lMm = parseDouble(text)) }
            }
        )
        inputArea.addTextChangedListener(
            DebouncedTextWatcher { text ->
                viewModel.updateInputs { it.copy(aMm2 = parseDouble(text)) }
            }
        )
        inputInertiaX.addTextChangedListener(
            DebouncedTextWatcher { text ->
                viewModel.updateInputs { it.copy(ixMm4 = parseDouble(text)) }
            }
        )
        inputInertiaY.addTextChangedListener(
            DebouncedTextWatcher { text ->
                viewModel.updateInputs { it.copy(iyMm4 = parseDouble(text)) }
            }
        )
        inputModulus.addTextChangedListener(
            DebouncedTextWatcher { text ->
                viewModel.updateInputs { it.copy(eGpa = parseDouble(text)) }
            }
        )
        inputYield.addTextChangedListener(
            DebouncedTextWatcher { text ->
                viewModel.updateInputs { it.copy(fyMpa = parseDouble(text)) }
            }
        )
        inputGamma.addTextChangedListener(
            DebouncedTextWatcher { text ->
                if (!isApplyingPreset && parseDouble(text) != defaultGamma) {
                    inputPreset.isChecked = false
                }
                viewModel.updateInputs { it.copy(gammaM = parseDouble(text)) }
            }
        )
        inputLambdaLim.addTextChangedListener(
            DebouncedTextWatcher { text ->
                if (!isApplyingPreset && parseDouble(text) != defaultLambdaLimit) {
                    inputPreset.isChecked = false
                }
                viewModel.updateInputs { it.copy(lambdaLim = parseDouble(text)) }
            }
        )
        inputLoadApplied.addTextChangedListener(
            DebouncedTextWatcher { text ->
                viewModel.updateInputs { it.copy(nAplicadaN = parseDouble(text)) }
            }
        )
        inputAngle.addTextChangedListener(
            DebouncedTextWatcher { text ->
                if (!isApplyingPreset && parseDouble(text) != defaultAngle) {
                    inputPreset.isChecked = false
                }
                viewModel.updateInputs { it.copy(thetaDeg = parseDouble(text)) }
            }
        )
        inputPreset.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyPresetValues()
            }
        }
        applyPresetValues()
        loadState(
            inputLength, inputKFactor, inputArea, inputInertiaX, inputInertiaY, inputLoadApplied,
            inputMaterial, inputModulus, inputYield, inputGamma, inputLambdaLim, inputAngle, inputPreset
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    inputLength.error = state.errors.lMm
                    inputArea.error = state.errors.aMm2
                    inputInertiaX.error = state.errors.ixMm4
                    inputInertiaY.error = state.errors.iyMm4
                    inputModulus.error = state.errors.eGpa
                    inputYield.error = state.errors.fyMpa
                    inputGamma.error = state.errors.gammaM
                    inputLambdaLim.error = state.errors.lambdaLim
                    inputLoadApplied.error = state.errors.nAplicadaN
                    inputAngle.error = state.errors.thetaDeg

                    outputKl.text = formatOutput(state.outputs.klMm, "mm")
                    outputRx.text = formatOutput(state.outputs.rxMm, "mm")
                    outputRy.text = formatOutput(state.outputs.ryMm, "mm")
                    outputLambdaX.text = formatOutput(state.outputs.lambdaX, "-")
                    outputLambdaY.text = formatOutput(state.outputs.lambdaY, "-")
                    outputLambdaCrit.text = formatOutput(state.outputs.lambdaCrit, "-")
                    outputAxis.text = state.outputs.eixoCritico ?: "—"
                    outputRegime.text = state.outputs.regime ?: "—"
                    outputSigmaCr.text = formatOutput(state.outputs.sigmaCrMpa, "MPa")
                    outputNCr.text = formatOutput(state.outputs.nCrN, "N")
                    outputNRd.text = formatOutput(state.outputs.nRdN, "N")
                    outputUtilization.text = formatOutput(state.outputs.utilization, "-")
                    outputKgf.text = formatOutput(state.outputs.forceKgf, "kgf")
                    outputKgfIncl.text = formatOutput(state.outputs.forceKgfIncl, "kgf")
                    outputStatus.text = state.outputs.status ?: "—"
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        saveState(
            findViewById(R.id.input_buckling_length), findViewById(R.id.input_buckling_k_factor),
            findViewById(R.id.input_buckling_area), findViewById(R.id.input_buckling_inertia_x),
            findViewById(R.id.input_buckling_inertia_y), findViewById(R.id.input_buckling_load_applied),
            findViewById(R.id.input_buckling_material), findViewById(R.id.input_buckling_modulus),
            findViewById(R.id.input_buckling_yield), findViewById(R.id.input_buckling_gamma),
            findViewById(R.id.input_buckling_lambda_limit), findViewById(R.id.input_buckling_angle),
            findViewById(R.id.input_buckling_preset)
        )
    }

    private fun saveState(
        inputLength: EditText, inputKFactor: Spinner, inputArea: EditText, inputInertiaX: EditText,
        inputInertiaY: EditText, inputLoadApplied: EditText, inputMaterial: Spinner, inputModulus: EditText,
        inputYield: EditText, inputGamma: EditText, inputLambdaLim: EditText, inputAngle: EditText, inputPreset: CheckBox
    ) {
        prefs.edit()
            .putString("length", inputLength.text.toString())
            .putInt("k", inputKFactor.selectedItemPosition)
            .putString("area", inputArea.text.toString())
            .putString("ix", inputInertiaX.text.toString())
            .putString("iy", inputInertiaY.text.toString())
            .putString("load", inputLoadApplied.text.toString())
            .putInt("material", inputMaterial.selectedItemPosition)
            .putString("modulus", inputModulus.text.toString())
            .putString("yield", inputYield.text.toString())
            .putString("gamma", inputGamma.text.toString())
            .putString("lambda", inputLambdaLim.text.toString())
            .putString("angle", inputAngle.text.toString())
            .putBoolean("preset", inputPreset.isChecked)
            .apply()
    }

    private fun loadState(
        inputLength: EditText, inputKFactor: Spinner, inputArea: EditText, inputInertiaX: EditText,
        inputInertiaY: EditText, inputLoadApplied: EditText, inputMaterial: Spinner, inputModulus: EditText,
        inputYield: EditText, inputGamma: EditText, inputLambdaLim: EditText, inputAngle: EditText, inputPreset: CheckBox
    ) {
        if (!prefs.contains("length")) return
        inputLength.setText(prefs.getString("length", ""))
        inputKFactor.setSelection(prefs.getInt("k", 0))
        inputArea.setText(prefs.getString("area", ""))
        inputInertiaX.setText(prefs.getString("ix", ""))
        inputInertiaY.setText(prefs.getString("iy", ""))
        inputLoadApplied.setText(prefs.getString("load", ""))
        inputMaterial.setSelection(prefs.getInt("material", 0))
        inputModulus.setText(prefs.getString("modulus", ""))
        inputYield.setText(prefs.getString("yield", ""))
        inputGamma.setText(prefs.getString("gamma", ""))
        inputLambdaLim.setText(prefs.getString("lambda", ""))
        inputAngle.setText(prefs.getString("angle", ""))
        inputPreset.isChecked = prefs.getBoolean("preset", false)
    }

    private fun parseDouble(value: String): Double? {
        val normalized = value.trim().replace(',', '.')
        return normalized.toDoubleOrNull()
    }

    private fun formatOutput(value: Double?, unit: String): String {
        return value?.let { "${NumberFormatter.format(it)} $unit" } ?: "—"
    }
}
