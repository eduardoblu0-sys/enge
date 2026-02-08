package com.example.enge

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.enge.util.DebouncedTextWatcher
import com.example.enge.util.NumberFormatter
import kotlinx.coroutines.launch

class BucklingActivity : ComponentActivity() {
    private val viewModel: BucklingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buckling)

        val inputLength = findViewById<EditText>(R.id.input_buckling_length)
        val inputKFactor = findViewById<Spinner>(R.id.input_buckling_k_factor)
        val inputArea = findViewById<EditText>(R.id.input_buckling_area)
        val inputInertiaX = findViewById<EditText>(R.id.input_buckling_inertia_x)
        val inputInertiaY = findViewById<EditText>(R.id.input_buckling_inertia_y)
        val inputModulus = findViewById<EditText>(R.id.input_buckling_modulus)
        val inputYield = findViewById<EditText>(R.id.input_buckling_yield)
        val inputGamma = findViewById<EditText>(R.id.input_buckling_gamma)
        val inputLambdaLim = findViewById<EditText>(R.id.input_buckling_lambda_limit)
        val inputLoadApplied = findViewById<EditText>(R.id.input_buckling_load_applied)
        val inputAngle = findViewById<EditText>(R.id.input_buckling_angle)

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

        val kValues = listOf(2.0, 0.7, 1.0, 0.5)
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
                viewModel.updateInputs { it.copy(gammaM = parseDouble(text)) }
            }
        )
        inputLambdaLim.addTextChangedListener(
            DebouncedTextWatcher { text ->
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
                viewModel.updateInputs { it.copy(thetaDeg = parseDouble(text)) }
            }
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

                    outputKl.text = formatOutput(state.outputs.klMm)
                    outputRx.text = formatOutput(state.outputs.rxMm)
                    outputRy.text = formatOutput(state.outputs.ryMm)
                    outputLambdaX.text = formatOutput(state.outputs.lambdaX)
                    outputLambdaY.text = formatOutput(state.outputs.lambdaY)
                    outputLambdaCrit.text = formatOutput(state.outputs.lambdaCrit)
                    outputAxis.text = state.outputs.eixoCritico ?: "—"
                    outputRegime.text = state.outputs.regime ?: "—"
                    outputSigmaCr.text = formatOutput(state.outputs.sigmaCrMpa)
                    outputNCr.text = formatOutput(state.outputs.nCrN)
                    outputNRd.text = formatOutput(state.outputs.nRdN)
                    outputUtilization.text = formatOutput(state.outputs.utilization)
                    outputKgf.text = formatOutput(state.outputs.forceKgf)
                    outputKgfIncl.text = formatOutput(state.outputs.forceKgfIncl)
                    outputStatus.text = state.outputs.status ?: "—"
                }
            }
        }
    }

    private fun parseDouble(value: String): Double? {
        val normalized = value.trim().replace(',', '.')
        return normalized.toDoubleOrNull()
    }

    private fun formatOutput(value: Double?): String {
        return value?.let { NumberFormatter.format(it) } ?: "—"
    }
}
