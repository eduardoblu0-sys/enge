package com.example.enge.model

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

object TorsionCalculator {
    fun toSI(raw: TorsionInputRaw): TorsionInputSI {
        return TorsionInputSI(
            forceN = TorsionUnitConverters.forceToN(raw.forceValue, raw.forceUnit),
            armM = TorsionUnitConverters.lengthToM(raw.armValue, raw.armUnit),
            phiRad = TorsionUnitConverters.degreesToRadians(raw.phiDeg),
            outerDiameterM = TorsionUnitConverters.lengthToM(raw.outerDiameterValue, raw.outerDiameterUnit),
            innerDiameterM = if (raw.isHollow) {
                TorsionUnitConverters.lengthToM(raw.innerDiameterValue ?: 0.0, raw.innerDiameterUnit)
            } else {
                0.0
            },
            lengthM = TorsionUnitConverters.lengthToM(raw.lengthValue, raw.lengthUnit),
            shearModulusPa = TorsionUnitConverters.modulusToPa(raw.shearModulusValue, raw.shearModulusUnit),
            shearYieldPa = raw.shearYieldValue?.let {
                TorsionUnitConverters.stressToPa(it, raw.shearYieldUnit)
            },
            fs = raw.fs,
            isHollow = raw.isHollow
        )
    }

    fun calculate(input: TorsionInputSI): TorsionOutput {
        val torqueNm = input.forceN * input.armM * sin(input.phiRad)
        val d4 = input.outerDiameterM.pow(4)
        val inner4 = input.innerDiameterM.pow(4)
        val polarMomentM4 = if (input.isHollow) PI * (d4 - inner4) / 32.0 else PI * d4 / 32.0
        val c = input.outerDiameterM / 2.0
        val tauPa = (torqueNm * c) / polarMomentM4
        val thetaRad = (torqueNm * input.lengthM) / (polarMomentM4 * input.shearModulusPa)
        val thetaDeg = thetaRad * 180.0 / PI
        val tauMpa = tauPa / 1e6

        val hasYield = input.shearYieldPa != null
        val status = if (hasYield) {
            val tauAdm = input.shearYieldPa!! / input.fs
            if (tauPa <= tauAdm) "OK" else "FALHOU"
        } else {
            null
        }

        val fsObt = if (hasYield && tauPa > 0.0) input.shearYieldPa!! / tauPa else null
        val torsionalRigidity = if (thetaRad != 0.0) torqueNm / thetaRad else null

        return TorsionOutput(
            convertedForceN = input.forceN,
            convertedArmM = input.armM,
            torqueNm = torqueNm,
            polarMomentM4 = polarMomentM4,
            tauPa = tauPa,
            tauMpa = tauMpa,
            thetaRad = thetaRad,
            thetaDeg = thetaDeg,
            torsionalRigidity = torsionalRigidity,
            status = status,
            fsObt = fsObt
        )
    }
}
