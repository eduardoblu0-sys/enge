package com.example.enge.model

import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

object BucklingCalculator {
    fun calculate(input: BucklingInput): BucklingOutput {
        val klMm = input.k * input.lMm
        val rxMm = sqrt(input.ixMm4 / input.aMm2)
        val ryMm = sqrt(input.iyMm4 / input.aMm2)
        val lambdaX = safeDiv(klMm, rxMm)
        val lambdaY = safeDiv(klMm, ryMm)
        val lambdaCrit = max(lambdaX, lambdaY)
        val eixoCritico = if (lambdaX >= lambdaY) "x" else "y"
        val regime = if (lambdaCrit <= input.lambdaLim) {
            "Baixa esbeltez - ruptura por escoamento"
        } else {
            "Alta esbeltez - ruptura por flambagem elástica"
        }
        val eMpa = input.eGpa * 1000.0
        val sigmaCrMpa = safeDiv(PI * PI * eMpa, lambdaCrit * lambdaCrit)
        val nCrN = sigmaCrMpa * input.aMm2
        val nRdN = if (lambdaCrit <= input.lambdaLim) {
            safeDiv(input.aMm2 * input.fyMpa, input.gammaM)
        } else {
            safeDiv(nCrN, input.gammaM)
        }
        val utilization = if (input.nAplicadaN <= 0.0) {
            0.0
        } else {
            safeDiv(input.nAplicadaN, nRdN)
        }
        val forceKgf = safeDiv(nRdN, 9.81)
        val rCritMm = safeDiv(klMm, lambdaCrit)
        val thetaRad = input.thetaDeg * PI / 180.0
        val ratio = if (rCritMm == 0.0) 0.0 else (sin(thetaRad) * input.lMm / rCritMm)
        val forceKgfIncl = safeDiv(forceKgf, ratio + 1.0)
        val status = if (utilization <= 1.0) "OK" else "FALHOU"

        return BucklingOutput(
            klMm = klMm,
            rxMm = rxMm,
            ryMm = ryMm,
            lambdaX = lambdaX,
            lambdaY = lambdaY,
            lambdaCrit = lambdaCrit,
            eixoCritico = eixoCritico,
            regime = regime,
            sigmaCrMpa = sigmaCrMpa,
            nCrN = nCrN,
            nRdN = nRdN,
            utilization = utilization,
            forceKgf = forceKgf,
            forceKgfIncl = forceKgfIncl,
            status = status
        )
    }

    private fun safeDiv(numerator: Double, denominator: Double): Double {
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }
}
