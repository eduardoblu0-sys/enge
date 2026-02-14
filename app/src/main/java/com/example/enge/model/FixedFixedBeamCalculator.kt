package com.example.enge.model

import kotlin.math.pow

object FixedFixedBeamCalculator {
    fun toSI(input: FixedFixedBeamInputData): FixedFixedBeamInputDataSI {
        return FixedFixedBeamInputDataSI(
            pN = input.pN,
            lM = input.lMm / 1_000.0,
            cM = input.cMm / 1_000.0,
            iM4 = input.iMm4 * 1e-12,
            fyPa = input.fyMpa * 1e6,
            ePa = input.eGpa * 1e9,
            fs = input.fs,
            material = input.material
        )
    }

    fun calculate(inputSI: FixedFixedBeamInputDataSI): FixedFixedBeamOutputDataSI {
        val deltaM = (inputSI.pN * inputSI.lM.pow(3.0)) / (192.0 * inputSI.ePa * inputSI.iM4)
        val mMaxNm = (inputSI.pN * inputSI.lM) / 8.0
        val sigmaPa = (mMaxNm * inputSI.cM) / inputSI.iM4
        val fyAdmPa = inputSI.fyPa / inputSI.fs
        val deltaAdmM = inputSI.lM / 400.0
        val checkDeflection = deltaM <= deltaAdmM
        val checkStress = sigmaPa <= fyAdmPa
        val percentualDelta = (deltaM / deltaAdmM) * 100.0
        val fsObtido = inputSI.fyPa / sigmaPa

        return FixedFixedBeamOutputDataSI(
            deltaM = deltaM,
            deltaAdmM = deltaAdmM,
            mMaxNm = mMaxNm,
            sigmaPa = sigmaPa,
            fyAdmPa = fyAdmPa,
            checkDeflection = checkDeflection,
            checkStress = checkStress,
            percentualDelta = percentualDelta,
            fsObtido = fsObtido
        )
    }

    fun toOutputUi(outputSI: FixedFixedBeamOutputDataSI): FixedFixedBeamOutputData {
        return FixedFixedBeamOutputData(
            deltaMm = outputSI.deltaM * 1_000.0,
            deltaAdmMm = outputSI.deltaAdmM * 1_000.0,
            mMaxNm = outputSI.mMaxNm,
            sigmaMpa = outputSI.sigmaPa / 1e6,
            fyAdmMpa = outputSI.fyAdmPa / 1e6,
            checkDeflection = outputSI.checkDeflection,
            checkStress = outputSI.checkStress,
            percentualDelta = outputSI.percentualDelta,
            fsObtido = outputSI.fsObtido
        )
    }
}
