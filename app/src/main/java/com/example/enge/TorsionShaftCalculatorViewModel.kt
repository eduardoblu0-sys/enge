package com.example.enge

import androidx.lifecycle.ViewModel
import com.example.enge.model.TorsionCalculator
import com.example.enge.model.TorsionForceUnit
import com.example.enge.model.TorsionInputRaw
import com.example.enge.model.TorsionLengthUnit
import com.example.enge.model.TorsionModulusUnit
import com.example.enge.model.TorsionOutput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class TorsionInputs(
    val forceValue: Double? = null,
    val forceUnit: TorsionForceUnit = TorsionForceUnit.N,
    val armValue: Double? = null,
    val armUnit: TorsionLengthUnit = TorsionLengthUnit.MM,
    val phiDeg: Double? = 90.0,
    val outerDiameterValue: Double? = null,
    val outerDiameterUnit: TorsionLengthUnit = TorsionLengthUnit.MM,
    val isHollow: Boolean = false,
    val innerDiameterValue: Double? = null,
    val innerDiameterUnit: TorsionLengthUnit = TorsionLengthUnit.MM,
    val lengthValue: Double? = null,
    val lengthUnit: TorsionLengthUnit = TorsionLengthUnit.MM,
    val shearModulusValue: Double? = null,
    val shearModulusUnit: TorsionModulusUnit = TorsionModulusUnit.GPA,
    val shearYieldValue: Double? = null,
    val fs: Double? = 1.5
)

data class TorsionErrors(
    val forceValue: String? = null,
    val armValue: String? = null,
    val phiDeg: String? = null,
    val outerDiameterValue: String? = null,
    val innerDiameterValue: String? = null,
    val lengthValue: String? = null,
    val shearModulusValue: String? = null,
    val shearYieldValue: String? = null,
    val fs: String? = null
)

data class TorsionUiState(
    val inputs: TorsionInputs = TorsionInputs(),
    val errors: TorsionErrors = TorsionErrors(),
    val output: TorsionOutput? = null
)

class TorsionShaftCalculatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TorsionUiState())
    val uiState: StateFlow<TorsionUiState> = _uiState

    fun updateInputs(update: (TorsionInputs) -> TorsionInputs) {
        _uiState.update { state ->
            val inputs = update(state.inputs)
            val errors = validate(inputs)
            val output = if (errors == TorsionErrors()) {
                calculate(inputs)
            } else {
                null
            }
            state.copy(inputs = inputs, errors = errors, output = output)
        }
    }

    private fun calculate(inputs: TorsionInputs): TorsionOutput {
        val raw = TorsionInputRaw(
            forceValue = inputs.forceValue ?: 0.0,
            forceUnit = inputs.forceUnit,
            armValue = inputs.armValue ?: 0.0,
            armUnit = inputs.armUnit,
            phiDeg = inputs.phiDeg ?: 90.0,
            outerDiameterValue = inputs.outerDiameterValue ?: 0.0,
            outerDiameterUnit = inputs.outerDiameterUnit,
            isHollow = inputs.isHollow,
            innerDiameterValue = inputs.innerDiameterValue,
            innerDiameterUnit = inputs.innerDiameterUnit,
            lengthValue = inputs.lengthValue ?: 0.0,
            lengthUnit = inputs.lengthUnit,
            shearModulusValue = inputs.shearModulusValue ?: 0.0,
            shearModulusUnit = inputs.shearModulusUnit,
            shearYieldValue = inputs.shearYieldValue,
            fs = inputs.fs ?: 1.5
        )
        return TorsionCalculator.calculate(TorsionCalculator.toSI(raw))
    }

    private fun validate(inputs: TorsionInputs): TorsionErrors {
        val dMeters = inputs.outerDiameterValue?.let {
            com.example.enge.model.TorsionUnitConverters.lengthToM(it, inputs.outerDiameterUnit)
        }
        val dInnerMeters = if (inputs.isHollow) {
            inputs.innerDiameterValue?.let {
                com.example.enge.model.TorsionUnitConverters.lengthToM(it, inputs.innerDiameterUnit)
            }
        } else {
            null
        }

        val innerDiameterError = when {
            !inputs.isHollow -> null
            inputs.innerDiameterValue == null -> "Informe um valor."
            inputs.innerDiameterValue <= 0.0 -> "Use um valor maior que zero."
            dMeters == null || dInnerMeters == null -> null
            dInnerMeters >= dMeters -> "Use d menor que D."
            else -> null
        }

        return TorsionErrors(
            forceValue = validatePositive(inputs.forceValue),
            armValue = validatePositive(inputs.armValue),
            phiDeg = validateOptional(inputs.phiDeg),
            outerDiameterValue = validatePositive(inputs.outerDiameterValue),
            innerDiameterValue = innerDiameterError,
            lengthValue = validatePositive(inputs.lengthValue),
            shearModulusValue = validatePositive(inputs.shearModulusValue),
            shearYieldValue = validateOptionalPositive(inputs.shearYieldValue),
            fs = validatePositive(inputs.fs)
        )
    }

    private fun validatePositive(value: Double?): String? = when {
        value == null -> "Informe um valor."
        value <= 0.0 -> "Use um valor maior que zero."
        else -> null
    }

    private fun validateOptional(value: Double?): String? = when {
        value == null -> null
        value.isNaN() -> "Informe um valor válido."
        else -> null
    }

    private fun validateOptionalPositive(value: Double?): String? = when {
        value == null -> null
        value <= 0.0 -> "Use um valor maior que zero."
        else -> null
    }
}
