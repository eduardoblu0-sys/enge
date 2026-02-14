package com.example.enge.model

data class FixedFixedBeamInputData(
    val pN: Double,
    val lMm: Double,
    val cMm: Double,
    val iMm4: Double,
    val fyMpa: Double,
    val eGpa: Double,
    val fs: Double,
    val material: String
)

data class FixedFixedBeamInputDataSI(
    val pN: Double,
    val lM: Double,
    val cM: Double,
    val iM4: Double,
    val fyPa: Double,
    val ePa: Double,
    val fs: Double,
    val material: String
)

data class FixedFixedBeamOutputDataSI(
    val deltaM: Double,
    val deltaAdmM: Double,
    val mMaxNm: Double,
    val sigmaPa: Double,
    val fyAdmPa: Double,
    val checkDeflection: Boolean,
    val checkStress: Boolean,
    val percentualDelta: Double,
    val fsObtido: Double
)

data class FixedFixedBeamOutputData(
    val deltaMm: Double,
    val deltaAdmMm: Double,
    val mMaxNm: Double,
    val sigmaMpa: Double,
    val fyAdmMpa: Double,
    val checkDeflection: Boolean,
    val checkStress: Boolean,
    val percentualDelta: Double,
    val fsObtido: Double
)
