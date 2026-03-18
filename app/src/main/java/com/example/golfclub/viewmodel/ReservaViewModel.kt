package com.example.golfclub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.golfclub.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReservaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).reservaDao()

    val reservas: StateFlow<List<Reserva>> = dao.getAll()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun insertar(reserva: Reserva) {
        viewModelScope.launch {
            dao.insert(reserva)
        }
    }

    fun actualizar(reserva: Reserva) {
        viewModelScope.launch {
            dao.update(reserva)
        }
    }

    fun eliminar(reserva: Reserva) {
        viewModelScope.launch {
            dao.delete(reserva)
        }
    }
}