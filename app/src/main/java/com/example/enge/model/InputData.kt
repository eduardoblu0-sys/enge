package com.example.enge.model

data class InputData(
    val P: Double,
    val PUnit: String,
    val L: Double,
    val LUnit: String,
    val c: Double,
    val cUnit: String,
    val I: Double,
    val IUnit: String,
    val fy: Double,
    val fyUnit: String,
    val E: Double,
    val EUnit: String,
    val FS_adm: Double,
    val material: String? = null
)
