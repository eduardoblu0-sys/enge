package com.example.enge.model

import kotlin.math.PI

object TorsionUnitConverters {
    fun forceToN(value: Double, unit: TorsionForceUnit): Double = when (unit) {
        TorsionForceUnit.N -> value
        TorsionForceUnit.KGF -> value * 9.80665
    }

    fun lengthToM(value: Double, unit: TorsionLengthUnit): Double = when (unit) {
        TorsionLengthUnit.MM -> value / 1_000.0
        TorsionLengthUnit.M -> value
    }

    fun modulusToPa(value: Double, unit: TorsionModulusUnit): Double = when (unit) {
        TorsionModulusUnit.GPA -> value * 1e9
        TorsionModulusUnit.MPA -> value * 1e6
    }

    fun stressToPa(value: Double, unit: TorsionStressUnit): Double = when (unit) {
        TorsionStressUnit.MPA -> value * 1e6
    }

    fun degreesToRadians(value: Double): Double = value * PI / 180.0
}
