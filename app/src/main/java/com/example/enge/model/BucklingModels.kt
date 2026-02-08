package com.example.enge.model

data class BucklingInput(
    val lMm: Double,
    val k: Double,
    val aMm2: Double,
    val ixMm4: Double,
    val iyMm4: Double,
    val eGpa: Double,
    val fyMpa: Double,
    val gammaM: Double,
    val lambdaLim: Double,
    val nAplicadaN: Double,
    val thetaDeg: Double
)

data class BucklingOutput(
    val klMm: Double,
    val rxMm: Double,
    val ryMm: Double,
    val lambdaX: Double,
    val lambdaY: Double,
    val lambdaCrit: Double,
    val eixoCritico: String,
    val regime: String,
    val sigmaCrMpa: Double,
    val nCrN: Double,
    val nRdN: Double,
    val utilization: Double,
    val forceKgf: Double,
    val forceKgfIncl: Double,
    val status: String
)
