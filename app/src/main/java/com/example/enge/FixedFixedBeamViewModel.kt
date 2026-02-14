package com.example.enge

import androidx.lifecycle.ViewModel
import com.example.enge.model.FixedFixedBeamCalculator
import com.example.enge.model.FixedFixedBeamInputData
import com.example.enge.model.FixedFixedBeamOutputData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class FixedFixedBeamInputs(
    val p: Double? = null,
    val pUnit: com.example.enge.model.ForceUnit = com.example.enge.model.ForceUnit.N,
    val l: Double? = null,
    val lUnit: com.example.enge.model.LengthUnit = com.example.enge.model.LengthUnit.MM,
    val cMm: Double? = null,
    val iMm4: Double? = null,
    val fs: Double? = null,
    val material: String? = null,
    val fyMpa: Double? = null,
    val eGpa: Double? = null
)

data class FixedFixedBeamErrors(
    val p: String? = null,
    val l: String? = null,
    val cMm: String? = null,
    val iMm4: String? = null,
    val fs: String? = null,
    val fyMpa: String? = null,
    val eGpa: String? = null
)

data class FixedFixedBeamUiState(
    val inputs: FixedFixedBeamInputs = FixedFixedBeamInputs(),
    val errors: FixedFixedBeamErrors = FixedFixedBeamErrors(),
    val output: FixedFixedBeamOutputData? = null
)

class FixedFixedBeamViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FixedFixedBeamUiState())
    val uiState: StateFlow<FixedFixedBeamUiState> = _uiState

    fun updateInputs(update: (FixedFixedBeamInputs) -> FixedFixedBeamInputs) {
        _uiState.update { state ->
            val inputs = update(state.inputs)
            val errors = validate(inputs)
            val output = if (errors == FixedFixedBeamErrors()) calculate(inputs) else null
            state.copy(inputs = inputs, errors = errors, output = output)
        }
    }

    private fun validate(inputs: FixedFixedBeamInputs): FixedFixedBeamErrors {
        return FixedFixedBeamErrors(
            p = validatePositive(inputs.p),
            l = validatePositive(inputs.l),
            cMm = validatePositive(inputs.cMm),
            iMm4 = validatePositive(inputs.iMm4),
            fs = validatePositive(inputs.fs),
            fyMpa = validatePositive(inputs.fyMpa),
            eGpa = validatePositive(inputs.eGpa)
        )
    }

    private fun calculate(inputs: FixedFixedBeamInputs): FixedFixedBeamOutputData {
        val inputData = FixedFixedBeamInputData(
            p = inputs.p ?: 0.0,
            pUnit = inputs.pUnit,
            l = inputs.l ?: 0.0,
            lUnit = inputs.lUnit,
            cMm = inputs.cMm ?: 0.0,
            iMm4 = inputs.iMm4 ?: 0.0,
            fyMpa = inputs.fyMpa ?: 0.0,
            eGpa = inputs.eGpa ?: 0.0,
            fs = inputs.fs ?: 0.0,
            material = inputs.material.orEmpty()
        )
        val outputSI = FixedFixedBeamCalculator.calculate(FixedFixedBeamCalculator.toSI(inputData))
        return FixedFixedBeamCalculator.toOutputUi(outputSI)
    }

    private fun validatePositive(value: Double?): String? {
        return when {
            value == null -> "Informe um valor."
            value <= 0.0 -> "Use um valor maior que zero."
            else -> null
        }
    }
}
