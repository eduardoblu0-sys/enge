package com.example.enge.model

import kotlin.math.pow

object BeamCalculator {
    fun convertToSI(input: InputData): InputDataSI {
        val load = when (input.PUnit) {
            "kN" -> input.P * 1_000.0
            else -> input.P
        }
        val length = when (input.LUnit) {
            "mm" -> input.L / 1_000.0
            "cm" -> input.L / 100.0
            else -> input.L
        }
        val distance = when (input.cUnit) {
            "mm" -> input.c / 1_000.0
            "cm" -> input.c / 100.0
            else -> input.c
        }
        val inertia = when (input.IUnit) {
            "mm^4" -> input.I / 1_000_000_000_000.0
            "cm^4" -> input.I / 100_000_000.0
            else -> input.I
        }
        val fy = when (input.fyUnit) {
            "GPa" -> input.fy * 1_000_000_000.0
            else -> input.fy * 1_000_000.0
        }
        val elasticModulus = when (input.EUnit) {
            "MPa" -> input.E * 1_000_000.0
            else -> input.E * 1_000_000_000.0
        }

        return InputDataSI(
            P = load,
            L = length,
            c = distance,
            I = inertia,
            fy = fy,
            E = elasticModulus,
            FS_adm = input.FS_adm
        )
    }

    fun calculate(input: InputData): OutputData {
        val si = convertToSI(input)
        val deltaObt = safeDiv(si.P * si.L.pow(3.0), 3.0 * si.E * si.I)
        val mMax = si.P * si.L
        val sigma = safeDiv(mMax * si.c, si.I)
        val fyAdm = safeDiv(si.fy, si.FS_adm)
        val deltaAdm = safeDiv(si.L, 400.0)
        val checkDeflection = deltaObt <= deltaAdm
        val checkStress = sigma <= fyAdm
        val percentualDelta = safeDiv(deltaObt, deltaAdm) * 100.0
        val fsObtido = safeDiv(si.fy, sigma)

        return OutputData(
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

    private fun safeDiv(numerator: Double, denominator: Double): Double {
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }
}
