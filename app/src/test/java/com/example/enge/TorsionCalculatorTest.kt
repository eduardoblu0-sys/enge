package com.example.enge

import com.example.enge.model.TorsionCalculator
import com.example.enge.model.TorsionForceUnit
import com.example.enge.model.TorsionInputRaw
import com.example.enge.model.TorsionLengthUnit
import com.example.enge.model.TorsionModulusUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TorsionCalculatorTest {
    @Test
    fun `converte para SI corretamente`() {
        val raw = TorsionInputRaw(
            forceValue = 10.0,
            forceUnit = TorsionForceUnit.KGF,
            armValue = 500.0,
            armUnit = TorsionLengthUnit.MM,
            phiDeg = 90.0,
            outerDiameterValue = 50.0,
            outerDiameterUnit = TorsionLengthUnit.MM,
            isHollow = false,
            lengthValue = 1000.0,
            lengthUnit = TorsionLengthUnit.MM,
            shearModulusValue = 80.0,
            shearModulusUnit = TorsionModulusUnit.GPA,
            shearYieldValue = 200.0,
            fs = 2.0
        )

        val si = TorsionCalculator.toSI(raw)

        assertEquals(98.0665, si.forceN, 1e-6)
        assertEquals(0.5, si.armM, 1e-9)
        assertEquals(0.05, si.outerDiameterM, 1e-9)
        assertEquals(1.0, si.lengthM, 1e-9)
        assertEquals(80e9, si.shearModulusPa, 1e3)
        assertEquals(200e6, si.shearYieldPa!!, 1e-3)
    }

    @Test
    fun `calcula torsao para eixo macico`() {
        val output = TorsionCalculator.calculate(
            TorsionCalculator.toSI(
                TorsionInputRaw(
                    forceValue = 100.0,
                    forceUnit = TorsionForceUnit.N,
                    armValue = 1.0,
                    armUnit = TorsionLengthUnit.M,
                    phiDeg = 90.0,
                    outerDiameterValue = 0.05,
                    outerDiameterUnit = TorsionLengthUnit.M,
                    isHollow = false,
                    lengthValue = 1.0,
                    lengthUnit = TorsionLengthUnit.M,
                    shearModulusValue = 79.0,
                    shearModulusUnit = TorsionModulusUnit.GPA,
                    shearYieldValue = 300.0,
                    fs = 1.5
                )
            )
        )

        assertTrue(output.torqueNm > 99.9)
        assertTrue(output.polarMomentM4 > 0.0)
        assertTrue(output.tauMpa > 0.0)
        assertTrue(output.thetaDeg > 0.0)
        assertEquals("OK", output.status)
        assertTrue(output.fsObt!! > 1.0)
    }

    @Test
    fun `calcula torsao para eixo oco`() {
        val output = TorsionCalculator.calculate(
            TorsionCalculator.toSI(
                TorsionInputRaw(
                    forceValue = 500.0,
                    forceUnit = TorsionForceUnit.N,
                    armValue = 0.2,
                    armUnit = TorsionLengthUnit.M,
                    phiDeg = 90.0,
                    outerDiameterValue = 60.0,
                    outerDiameterUnit = TorsionLengthUnit.MM,
                    isHollow = true,
                    innerDiameterValue = 30.0,
                    innerDiameterUnit = TorsionLengthUnit.MM,
                    lengthValue = 2.0,
                    lengthUnit = TorsionLengthUnit.M,
                    shearModulusValue = 27_000.0,
                    shearModulusUnit = TorsionModulusUnit.MPA,
                    shearYieldValue = null
                )
            )
        )

        assertTrue(output.polarMomentM4 > 0.0)
        assertTrue(output.status == null)
        assertTrue(output.fsObt == null)
    }
}
