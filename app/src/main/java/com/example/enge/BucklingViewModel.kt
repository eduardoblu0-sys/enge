package com.example.enge

import androidx.lifecycle.ViewModel
import com.example.enge.model.BucklingCalculator
import com.example.enge.model.BucklingInput
import com.example.enge.model.BucklingOutput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class BucklingInputs(
    val lMm: Double? = null,
    val k: Double? = null,
    val aMm2: Double? = null,
    val ixMm4: Double? = null,
    val iyMm4: Double? = null,
    val eGpa: Double? = null,
    val fyMpa: Double? = null,
    val gammaM: Double? = null,
    val lambdaLim: Double? = null,
    val nAplicadaN: Double? = null,
    val thetaDeg: Double? = null
)

data class BucklingErrors(
    val lMm: String? = null,
    val k: String? = null,
    val aMm2: String? = null,
    val ixMm4: String? = null,
    val iyMm4: String? = null,
    val eGpa: String? = null,
    val fyMpa: String? = null,
    val gammaM: String? = null,
    val lambdaLim: String? = null,
    val nAplicadaN: String? = null,
    val thetaDeg: String? = null
)

data class BucklingOutputs(
    val klMm: Double? = null,
    val rxMm: Double? = null,
    val ryMm: Double? = null,
    val lambdaX: Double? = null,
    val lambdaY: Double? = null,
    val lambdaCrit: Double? = null,
    val eixoCritico: String? = null,
    val regime: String? = null,
    val sigmaCrMpa: Double? = null,
    val nCrN: Double? = null,
    val nRdN: Double? = null,
    val utilization: Double? = null,
    val forceKgf: Double? = null,
    val forceKgfIncl: Double? = null,
    val status: String? = null
)

data class BucklingUiState(
    val inputs: BucklingInputs = BucklingInputs(),
    val outputs: BucklingOutputs = BucklingOutputs(),
    val errors: BucklingErrors = BucklingErrors()
)

class BucklingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BucklingUiState())
    val uiState: StateFlow<BucklingUiState> = _uiState

    fun updateInputs(update: (BucklingInputs) -> BucklingInputs) {
        _uiState.update { state ->
            val newInputs = update(state.inputs)
            val errors = validateInputs(newInputs)
            val outputs = if (errors == BucklingErrors()) {
                calculateOutputs(newInputs)
            } else {
                BucklingOutputs()
            }
            state.copy(inputs = newInputs, errors = errors, outputs = outputs)
        }
    }

    private fun calculateOutputs(inputs: BucklingInputs): BucklingOutputs {
        val input = BucklingInput(
            lMm = inputs.lMm ?: 0.0,
            k = inputs.k ?: 0.0,
            aMm2 = inputs.aMm2 ?: 0.0,
            ixMm4 = inputs.ixMm4 ?: 0.0,
            iyMm4 = inputs.iyMm4 ?: 0.0,
            eGpa = inputs.eGpa ?: 0.0,
            fyMpa = inputs.fyMpa ?: 0.0,
            gammaM = inputs.gammaM ?: 0.0,
            lambdaLim = inputs.lambdaLim ?: 0.0,
            nAplicadaN = inputs.nAplicadaN ?: 0.0,
            thetaDeg = inputs.thetaDeg ?: 0.0
        )
        val output: BucklingOutput = BucklingCalculator.calculate(input)
        return BucklingOutputs(
            klMm = output.klMm,
            rxMm = output.rxMm,
            ryMm = output.ryMm,
            lambdaX = output.lambdaX,
            lambdaY = output.lambdaY,
            lambdaCrit = output.lambdaCrit,
            eixoCritico = output.eixoCritico,
            regime = output.regime,
            sigmaCrMpa = output.sigmaCrMpa,
            nCrN = output.nCrN,
            nRdN = output.nRdN,
            utilization = output.utilization,
            forceKgf = output.forceKgf,
            forceKgfIncl = output.forceKgfIncl,
            status = output.status
        )
    }

    private fun validateInputs(inputs: BucklingInputs): BucklingErrors {
        return BucklingErrors(
            lMm = validatePositive(inputs.lMm),
            k = validatePositive(inputs.k),
            aMm2 = validatePositive(inputs.aMm2),
            ixMm4 = validatePositive(inputs.ixMm4),
            iyMm4 = validatePositive(inputs.iyMm4),
            eGpa = validatePositive(inputs.eGpa),
            fyMpa = validatePositive(inputs.fyMpa),
            gammaM = validatePositive(inputs.gammaM),
            lambdaLim = validatePositive(inputs.lambdaLim),
            nAplicadaN = validateNonNegative(inputs.nAplicadaN),
            thetaDeg = validateOptional(inputs.thetaDeg)
        )
    }

    private fun validatePositive(value: Double?): String? {
        return when {
            value == null -> "Informe um valor."
            value <= 0.0 -> "Use um valor maior que zero."
            else -> null
        }
    }

    private fun validateNonNegative(value: Double?): String? {
        return when {
            value == null -> "Informe um valor."
            value < 0.0 -> "Use um valor maior ou igual a zero."
            else -> null
        }
    }

    private fun validateOptional(value: Double?): String? {
        return when {
            value == null -> null
            value.isNaN() -> "Informe um valor válido."
            else -> null
        }
    }
}
