package com.example.enge.model

import kotlin.math.pow

object BeamCalculator {
    fun toSI(input: InputDataRaw): InputDataSI {
        return InputDataSI(
            P = forceToN(input.P, input.PUnit),
            L = lengthToM(input.L, input.LUnit),
            c = lengthToM(input.c, input.cUnit),
            I = inertiaToM4(input.I, input.IUnit),
            fy = stressToPa(input.fy, input.fyUnit),
            E = modulusToPa(input.E, input.EUnit),
            FS_adm = input.FS_adm
        )
    }

    fun calculate(input: InputDataSI): OutputDataSI {
        val deltaObt = safeDiv(input.P * input.L.pow(3.0), 3.0 * input.E * input.I)
        val mMax = input.P * input.L
        val sigma = safeDiv(mMax * input.c, input.I)
        val fyAdm = safeDiv(input.fy, input.FS_adm)
        val deltaAdm = safeDiv(input.L, 400.0)
        val checkDeflection = deltaObt <= deltaAdm
        val checkStress = sigma <= fyAdm
        val percentualDelta = safeDiv(deltaObt, deltaAdm) * 100.0
        val fsObtido = safeDiv(input.fy, sigma)

        return OutputDataSI(
            delta_obt = deltaObt,
            delta_adm = deltaAdm,
            Mmax = mMax,
            sigma = sigma,
            fy_adm = fyAdm,
            check_deflection = checkDeflection,
            check_stress = checkStress,
            percentual_delta = percentualDelta,
            FS_obtido = fsObtido,
            status_text_deflection = if (checkDeflection) "OK" else "FALHOU",
            status_text_stress = if (checkStress) "OK" else "FALHOU"
        )
    }

    fun formatForUi(output: OutputDataSI): OutputDataUi {
        return OutputDataUi(
            delta_obt = metersToMillimeters(output.delta_obt),
            delta_adm = metersToMillimeters(output.delta_adm),
            Mmax = output.Mmax,
            sigma = pascalToMegaPascal(output.sigma),
            fy_adm = pascalToMegaPascal(output.fy_adm),
            check_deflection = output.check_deflection,
            check_stress = output.check_stress,
            percentual_delta = output.percentual_delta,
            FS_obtido = output.FS_obtido,
            status_text_deflection = output.status_text_deflection,
            status_text_stress = output.status_text_stress
        )
    }

    fun forceToN(value: Double, unit: ForceUnit): Double {
        return when (unit) {
            ForceUnit.N -> value
            ForceUnit.KN -> value * 1_000.0
        }
    }

    fun lengthToM(value: Double, unit: LengthUnit): Double {
        return when (unit) {
            LengthUnit.MM -> value / 1_000.0
            LengthUnit.M -> value
        }
    }

    fun inertiaToM4(value: Double, unit: InertiaUnit): Double {
        return when (unit) {
            InertiaUnit.MM4 -> value * 1e-12
            InertiaUnit.M4 -> value
        }
    }

    fun stressToPa(value: Double, unit: StressUnit): Double {
        return when (unit) {
            StressUnit.MPA -> value * 1e6
            StressUnit.PA -> value
        }
    }

    fun modulusToPa(value: Double, unit: ModulusUnit): Double {
        return when (unit) {
            ModulusUnit.GPA -> value * 1e9
            ModulusUnit.MPA -> value * 1e6
            ModulusUnit.PA -> value
        }
    }

    fun metersToMillimeters(value: Double): Double {
        return value * 1_000.0
    }

    fun pascalToMegaPascal(value: Double): Double {
        return value / 1e6
    }

    private fun safeDiv(numerator: Double, denominator: Double): Double {
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }
}
