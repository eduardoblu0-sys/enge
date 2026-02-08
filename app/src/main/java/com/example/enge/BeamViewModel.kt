package com.example.enge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.enge.model.BeamCalculator
import com.example.enge.model.InputData
import com.example.enge.model.OutputData

class BeamViewModel : ViewModel() {
    private val _output = MutableLiveData<OutputData>()
    val output: LiveData<OutputData> = _output

    fun calculate(input: InputData) {
        _output.value = BeamCalculator.calculate(input)
    }
}
