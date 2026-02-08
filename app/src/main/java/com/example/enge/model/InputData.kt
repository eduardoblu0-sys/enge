package com.example.enge.model

data class InputDataRaw(
    val P: Double,
    val PUnit: ForceUnit,
    val L: Double,
    val LUnit: LengthUnit,
    val c: Double,
    val cUnit: LengthUnit,
    val I: Double,
    val IUnit: InertiaUnit,
    val fy: Double,
    val fyUnit: StressUnit,
    val E: Double,
    val EUnit: ModulusUnit,
    val FS_adm: Double
)
