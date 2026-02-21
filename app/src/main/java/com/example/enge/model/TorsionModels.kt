package com.example.enge.model

enum class TorsionForceUnit { N, KGF }
enum class TorsionLengthUnit { MM, M }
enum class TorsionModulusUnit { GPA, MPA }
enum class TorsionStressUnit { MPA }

data class TorsionInputRaw(
    val forceValue: Double,
    val forceUnit: TorsionForceUnit,
    val armValue: Double,
    val armUnit: TorsionLengthUnit,
    val phiDeg: Double = 90.0,
    val outerDiameterValue: Double,
    val outerDiameterUnit: TorsionLengthUnit,
    val isHollow: Boolean,
    val innerDiameterValue: Double? = null,
    val innerDiameterUnit: TorsionLengthUnit = TorsionLengthUnit.MM,
    val lengthValue: Double,
    val lengthUnit: TorsionLengthUnit,
    val shearModulusValue: Double,
    val shearModulusUnit: TorsionModulusUnit,
    val shearYieldValue: Double? = null,
    val shearYieldUnit: TorsionStressUnit = TorsionStressUnit.MPA,
    val fs: Double = 1.5
)

data class TorsionInputSI(
    val forceN: Double,
    val armM: Double,
    val phiRad: Double,
    val outerDiameterM: Double,
    val innerDiameterM: Double,
    val lengthM: Double,
    val shearModulusPa: Double,
    val shearYieldPa: Double?,
    val fs: Double,
    val isHollow: Boolean
)

data class TorsionOutput(
    val convertedForceN: Double,
    val convertedArmM: Double,
    val torqueNm: Double,
    val polarMomentM4: Double,
    val tauPa: Double,
    val tauMpa: Double,
    val thetaRad: Double,
    val thetaDeg: Double,
    val torsionalRigidity: Double?,
    val status: String?,
    val fsObt: Double?
)
