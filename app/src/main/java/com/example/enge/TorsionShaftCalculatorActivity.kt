package com.example.enge

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.enge.model.TorsionForceUnit
import com.example.enge.model.TorsionLengthUnit
import com.example.enge.model.TorsionModulusUnit
import com.example.enge.util.DebouncedTextWatcher
import com.example.enge.util.NumberFormatter
import kotlinx.coroutines.launch

class TorsionShaftCalculatorActivity : ComponentActivity() {
    private val viewModel: TorsionShaftCalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_torsion_shaft_calculator)

        val inputForce = findViewById<EditText>(R.id.input_torsion_force)
        val inputForceUnit = findViewById<Spinner>(R.id.input_torsion_force_unit)
        val inputArm = findViewById<EditText>(R.id.input_torsion_arm)
        val inputArmUnit = findViewById<Spinner>(R.id.input_torsion_arm_unit)
        val inputPhi = findViewById<EditText>(R.id.input_torsion_phi)
        val inputOuterD = findViewById<EditText>(R.id.input_torsion_outer_diameter)
        val inputOuterDUnit = findViewById<Spinner>(R.id.input_torsion_outer_diameter_unit)
        val inputIsHollow = findViewById<Switch>(R.id.input_torsion_is_hollow)
        val hollowContainer = findViewById<View>(R.id.container_torsion_inner_diameter)
        val inputInnerD = findViewById<EditText>(R.id.input_torsion_inner_diameter)
        val inputInnerDUnit = findViewById<Spinner>(R.id.input_torsion_inner_diameter_unit)
        val inputLength = findViewById<EditText>(R.id.input_torsion_length)
        val inputLengthUnit = findViewById<Spinner>(R.id.input_torsion_length_unit)
        val inputG = findViewById<EditText>(R.id.input_torsion_g)
        val inputGUnit = findViewById<Spinner>(R.id.input_torsion_g_unit)
        val inputTauY = findViewById<EditText>(R.id.input_torsion_tau_y)
        val inputTauYUnit = findViewById<Spinner>(R.id.input_torsion_tau_y_unit)
        val inputFs = findViewById<EditText>(R.id.input_torsion_fs)

        val outputForceN = findViewById<TextView>(R.id.output_torsion_force_n)
        val outputArmM = findViewById<TextView>(R.id.output_torsion_arm_m)
        val outputTorque = findViewById<TextView>(R.id.output_torsion_torque)
        val outputJ = findViewById<TextView>(R.id.output_torsion_j)
        val outputTau = findViewById<TextView>(R.id.output_torsion_tau)
        val outputTheta = findViewById<TextView>(R.id.output_torsion_theta)
        val outputK = findViewById<TextView>(R.id.output_torsion_k)
        val outputStatus = findViewById<TextView>(R.id.output_torsion_status)
        val outputFsObt = findViewById<TextView>(R.id.output_torsion_fs_obt)

        inputPhi.setText("90")
        inputFs.setText("1.5")

        inputForceUnit.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("N", "kgf"))
        inputArmUnit.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("mm", "m"))
        inputOuterDUnit.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("mm", "m"))
        inputInnerDUnit.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("mm", "m"))
        inputLengthUnit.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("mm", "m"))
        inputGUnit.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("GPa", "MPa"))
        inputTauYUnit.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("MPa"))

        fun parseDouble(text: String?): Double? = text?.trim()?.replace(',', '.')?.takeIf { it.isNotEmpty() }?.toDoubleOrNull()

        fun parseForceUnit(text: String?): TorsionForceUnit =
            if (text?.trim()?.equals("kgf", ignoreCase = true) == true) TorsionForceUnit.KGF else TorsionForceUnit.N

        fun parseLengthUnit(text: String?): TorsionLengthUnit =
            if (text?.trim()?.equals("m", ignoreCase = true) == true) TorsionLengthUnit.M else TorsionLengthUnit.MM

        fun parseModulusUnit(text: String?): TorsionModulusUnit =
            if (text?.trim()?.equals("MPa", ignoreCase = true) == true) TorsionModulusUnit.MPA else TorsionModulusUnit.GPA

        fun updateInputs() {
            viewModel.updateInputs {
                it.copy(
                    forceValue = parseDouble(inputForce.text?.toString()),
                    forceUnit = parseForceUnit(inputForceUnit.selectedItem?.toString()),
                    armValue = parseDouble(inputArm.text?.toString()),
                    armUnit = parseLengthUnit(inputArmUnit.selectedItem?.toString()),
                    phiDeg = parseDouble(inputPhi.text?.toString()) ?: 90.0,
                    outerDiameterValue = parseDouble(inputOuterD.text?.toString()),
                    outerDiameterUnit = parseLengthUnit(inputOuterDUnit.selectedItem?.toString()),
                    isHollow = inputIsHollow.isChecked,
                    innerDiameterValue = parseDouble(inputInnerD.text?.toString()),
                    innerDiameterUnit = parseLengthUnit(inputInnerDUnit.selectedItem?.toString()),
                    lengthValue = parseDouble(inputLength.text?.toString()),
                    lengthUnit = parseLengthUnit(inputLengthUnit.selectedItem?.toString()),
                    shearModulusValue = parseDouble(inputG.text?.toString()),
                    shearModulusUnit = parseModulusUnit(inputGUnit.selectedItem?.toString()),
                    shearYieldValue = parseDouble(inputTauY.text?.toString()),
                    fs = parseDouble(inputFs.text?.toString()) ?: 1.5
                )
            }
        }

        inputForce.addTextChangedListener(DebouncedTextWatcher { updateInputs() })
        inputArm.addTextChangedListener(DebouncedTextWatcher { updateInputs() })
        inputPhi.addTextChangedListener(DebouncedTextWatcher { updateInputs() })
        inputOuterD.addTextChangedListener(DebouncedTextWatcher { updateInputs() })
        inputInnerD.addTextChangedListener(DebouncedTextWatcher { updateInputs() })
        inputLength.addTextChangedListener(DebouncedTextWatcher { updateInputs() })
        inputG.addTextChangedListener(DebouncedTextWatcher { updateInputs() })
        inputTauY.addTextChangedListener(DebouncedTextWatcher { updateInputs() })
        inputFs.addTextChangedListener(DebouncedTextWatcher { updateInputs() })

        val spinnerListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = updateInputs()
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        inputForceUnit.onItemSelectedListener = spinnerListener
        inputArmUnit.onItemSelectedListener = spinnerListener
        inputOuterDUnit.onItemSelectedListener = spinnerListener
        inputInnerDUnit.onItemSelectedListener = spinnerListener
        inputLengthUnit.onItemSelectedListener = spinnerListener
        inputGUnit.onItemSelectedListener = spinnerListener
        inputTauYUnit.onItemSelectedListener = spinnerListener

        inputIsHollow.setOnCheckedChangeListener { _, checked ->
            hollowContainer.visibility = if (checked) View.VISIBLE else View.GONE
            updateInputs()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    inputForce.error = state.errors.forceValue
                    inputArm.error = state.errors.armValue
                    inputPhi.error = state.errors.phiDeg
                    inputOuterD.error = state.errors.outerDiameterValue
                    inputInnerD.error = state.errors.innerDiameterValue
                    inputLength.error = state.errors.lengthValue
                    inputG.error = state.errors.shearModulusValue
                    inputTauY.error = state.errors.shearYieldValue
                    inputFs.error = state.errors.fs

                    val output = state.output
                    outputForceN.text = output?.let { "${NumberFormatter.format(it.convertedForceN, 2)} N" } ?: "—"
                    outputArmM.text = output?.let { "${NumberFormatter.format(it.convertedArmM, 4)} m" } ?: "—"
                    outputTorque.text = output?.let { "${NumberFormatter.format(it.torqueNm, 2)} N·m" } ?: "—"
                    outputJ.text = output?.let { "%.6e m⁴".format(it.polarMomentM4) } ?: "—"
                    outputTau.text = output?.let { "${NumberFormatter.format(it.tauMpa, 2)} MPa" } ?: "—"
                    outputTheta.text = output?.let { "${NumberFormatter.format(it.thetaDeg, 2)} °" } ?: "—"
                    outputK.text = output?.torsionalRigidity?.let { "${NumberFormatter.format(it, 2)} N·m/rad" } ?: "—"
                    outputStatus.text = output?.status ?: "—"
                    outputFsObt.text = output?.fsObt?.let { NumberFormatter.format(it, 2) } ?: "—"
                    applyValidationStyle(outputStatus, outputStatus.text.toString())
                }
            }
        }

        updateInputs()
    }

    private fun applyValidationStyle(view: TextView, status: String?) {
        when (status) {
            "OK" -> {
                view.setTextColor(Color.parseColor("#2E7D32"))
                view.setTypeface(null, Typeface.BOLD)
            }
            "FALHOU" -> {
                view.setTextColor(Color.parseColor("#C62828"))
                view.setTypeface(null, Typeface.BOLD)
            }
            else -> {
                view.setTextColor(Color.parseColor("#5F6368"))
                view.setTypeface(null, Typeface.NORMAL)
            }
        }
    }
}
