package com.bctags.bcstocks.ui.testThreads.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bctags.bcstocks.ui.testThreads.data.SuscribeteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val suscribeteRepository = SuscribeteRepository()

    private val _uiState = MutableStateFlow<TestThreadsUIState>(TestThreadsUIState.Loading)
    val uiState: StateFlow<TestThreadsUIState> = _uiState
    fun example() {
        viewModelScope.launch {
            suscribeteRepository.counter
                .map { it.toString() }
                .collect { bombitas ->
                    Log.i("curso", bombitas)
                }
        }
    }

    fun example2() {
        viewModelScope.launch {
            suscribeteRepository.counter
                .map { it.toString() }
                .onEach { save(it) }
                .catch { error ->
                    Log.i("curso", "Error: ${error.message}")
                }
                .collect { bombitas ->
                    Log.i("curso", bombitas)
                }
        }
    }

    fun example3() {
        viewModelScope.launch {
            suscribeteRepository.counter
                .catch { _uiState.value = TestThreadsUIState.Error(it.message.orEmpty()) }
                .flowOn(Dispatchers.IO)
                .collect {
                    _uiState.value = TestThreadsUIState.Success(it)
                }
        }
    }

    private fun save(it: String) {

    }

//MAP TRANSOFRMA Y LO PASA PARA ABAJO
    //ONEACH ACCIONES SECUNDARIAS SIN MODIFICAR DATOS
    //STATEFLOW EL FLOW ENCARGADO DE MODIFICAR LA VISTA DE LA UI
    //SHAREDFLOW DISEÑADO PARA QUE HAYA UN PRODUCTOR(EL SHAREDFLOW) Y VARIOS RECEPTORES
}