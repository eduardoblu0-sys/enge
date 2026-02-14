package com.example.enge.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FixedFixedBeamCalculatorTest {
    @Test
    fun calculate_usesFixedFixedFormulasAndConversions() {
        val input = FixedFixedBeamInputData(
            pN = 1_000.0,
            lMm = 2_000.0,
            cMm = 50.0,
            iMm4 = 8_000_000.0,
            fyMpa = 360.0,
            eGpa = 200.0,
            fs = 2.0,
            material = "SAE 1045 Trefilado"
        )

        val output = FixedFixedBeamCalculator.toOutputUi(
            FixedFixedBeamCalculator.calculate(FixedFixedBeamCalculator.toSI(input))
        )

        assertEquals(3.2552083333, output.deltaMm, 1e-6)
        assertEquals(5.0, output.deltaAdmMm, 1e-9)
        assertEquals(250.0, output.mMaxNm, 1e-9)
        assertEquals(1.5625, output.sigmaMpa, 1e-9)
        assertEquals(180.0, output.fyAdmMpa, 1e-9)
        assertTrue(output.checkDeflection)
        assertTrue(output.checkStress)
        assertEquals(65.1041666666, output.percentualDelta, 1e-6)
        assertEquals(230.4, output.fsObtido, 1e-9)
        assertFalse(output.percentualDelta > 100.0)
    }
}
