package com.example.enge.model

data class OutputDataSI(
    val delta_obt: Double,
    val delta_adm: Double,
    val Mmax: Double,
    val sigma: Double,
    val fy_adm: Double,
    val check_deflection: Boolean,
    val check_stress: Boolean,
    val percentual_delta: Double,
    val FS_obtido: Double,
    val status_text_deflection: String,
    val status_text_stress: String
)
